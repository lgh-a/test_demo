<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <div class="flex gap-2">
        <n-input v-model:value="searchConfigKey" placeholder="配置键关键词" clearable style="width: 200px" />
        <n-select v-model:value="searchStatus" :options="statusOptions" placeholder="状态筛选" clearable style="width: 120px" />
        <n-button @click="fetchRules">查询</n-button>
      </div>
      <div class="flex gap-2">
        <n-button type="warning" v-auth="'rate-limit:rule:refresh'" @click="handleRefreshCache" :loading="refreshing">
          刷新缓存
        </n-button>
        <n-button type="primary" v-auth="'rate-limit:rule:add'" @click="openAddModal">新增规则</n-button>
      </div>
    </div>
    
    <n-table :bordered="false" :single-line="false">
      <thead>
        <tr>
          <th>ID</th>
          <th>配置键</th>
          <th>名称</th>
          <th>限流策略</th>
          <th>登录用户</th>
          <th>游客</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="rule in rules" :key="rule.id">
          <td>{{ rule.id }}</td>
          <td><n-tag type="info">{{ rule.configKey }}</n-tag></td>
          <td>{{ rule.name }}</td>
          <td>
            <n-tag :type="rule.limitType === 'TOKEN_BUCKET' ? 'success' : 'warning'">
              {{ rule.limitType === 'TOKEN_BUCKET' ? '令牌桶' : '固定窗口' }}
            </n-tag>
          </td>
          <td>
            <span class="text-xs">
              容量: {{ rule.capacity }} / 速率: {{ rule.rate }}
            </span>
          </td>
          <td>
            <span class="text-xs">
              容量: {{ rule.guestCapacity }} / 速率: {{ rule.guestRate }}
            </span>
          </td>
          <td>
            <n-tag :type="rule.status === 1 ? 'success' : 'default'">
              {{ rule.status === 1 ? '启用' : '禁用' }}
            </n-tag>
          </td>
          <td>{{ formatTime(rule.createTime) }}</td>
          <td>
            <div class="flex gap-2">
              <n-button size="small" v-auth="'rate-limit:rule:edit'" @click="openEditModal(rule)">编辑</n-button>
              <n-button size="small" type="error" v-auth="'rate-limit:rule:delete'" @click="handleDelete(rule.id)">删除</n-button>
            </div>
          </td>
        </tr>
      </tbody>
    </n-table>

    <div class="flex justify-end">
      <n-pagination
        v-model:page="pagination.current"
        :page-count="pagination.pages"
        :page-size="pagination.size"
        @update:page="fetchRules"
        show-size-picker
        :page-sizes="[10, 20, 50]"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑限流规则' : '新增限流规则'" class="w-[600px]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="120">
        <n-form-item label="配置键" path="configKey">
          <n-input v-model:value="formModel.configKey" placeholder="如：game:play" :disabled="isEdit" />
        </n-form-item>
        <n-form-item label="规则名称" path="name">
          <n-input v-model:value="formModel.name" placeholder="如：游戏播放限流" />
        </n-form-item>
        <n-form-item label="规则描述" path="description">
          <n-input v-model:value="formModel.description" placeholder="规则描述信息" />
        </n-form-item>
        <n-form-item label="限流策略" path="limitType">
          <n-select v-model:value="formModel.limitType" :options="limitTypeOptions" />
        </n-form-item>
        <n-divider>登录用户配置</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="桶容量" path="capacity">
              <n-input-number v-model:value="formModel.capacity" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="生成速率" path="rate">
              <n-input-number v-model:value="formModel.rate" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-divider>游客配置</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="桶容量" path="guestCapacity">
              <n-input-number v-model:value="formModel.guestCapacity" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="生成速率" path="guestRate">
              <n-input-number v-model:value="formModel.guestRate" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="formModel.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showModal = false">取消</n-button>
        <n-button type="primary" @click="handleSubmit" :loading="loading">确认</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { request } from '../api/request'
import { 
  NButton, NTable, NTag, NModal, NForm, NFormItem, NInput, NInputNumber, 
  NSelect, NSwitch, NPagination, NDivider, NGrid, NGi, useMessage 
} from 'naive-ui'

const message = useMessage()
const rules = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const loading = ref(false)
const refreshing = ref(false)
const formRef = ref(null)

const searchConfigKey = ref('')
const searchStatus = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  pages: 1,
  total: 0
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const limitTypeOptions = [
  { label: '令牌桶 (TOKEN_BUCKET)', value: 'TOKEN_BUCKET' },
  { label: '固定窗口每日 (FLEXIBLE)', value: 'FLEXIBLE' }
]

const defaultForm = {
  configKey: '',
  name: '',
  description: '',
  limitType: 'TOKEN_BUCKET',
  capacity: 10,
  rate: 1,
  guestCapacity: 5,
  guestRate: 1,
  status: 1
}

const formModel = ref({ ...defaultForm })

const formRules = {
  configKey: { required: true, message: '请输入配置键', trigger: 'blur' },
  name: { required: true, message: '请输入规则名称', trigger: 'blur' },
  limitType: { required: true, message: '请选择限流策略', trigger: 'change' },
  capacity: { required: true, type: 'number', message: '请输入桶容量', trigger: 'blur' },
  rate: { required: true, type: 'number', message: '请输入生成速率', trigger: 'blur' },
  guestCapacity: { required: true, type: 'number', message: '请输入游客桶容量', trigger: 'blur' },
  guestRate: { required: true, type: 'number', message: '请输入游客生成速率', trigger: 'blur' }
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchRules = async () => {
  try {
    const params = new URLSearchParams({
      current: pagination.current,
      size: pagination.size
    })
    if (searchConfigKey.value) params.append('configKey', searchConfigKey.value)
    if (searchStatus.value !== null) params.append('status', searchStatus.value)
    
    const data = await request(`/rate-limit/rules/page?${params.toString()}`)
    rules.value = data.records
    pagination.pages = data.pages
    pagination.total = data.total
  } catch (e) {
    message.error('获取规则列表失败')
  }
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchRules()
}

const openAddModal = () => {
  isEdit.value = false
  editingId.value = null
  formModel.value = { ...defaultForm }
  showModal.value = true
}

const openEditModal = async (rule) => {
  isEdit.value = true
  editingId.value = rule.id
  try {
    const data = await request(`/rate-limit/rules/${rule.id}`)
    formModel.value = { ...defaultForm, ...data }
    showModal.value = true
  } catch (e) {
    message.error('获取规则详情失败')
  }
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      try {
        if (isEdit.value) {
          await request('/rate-limit/rules', {
            method: 'PUT',
            body: JSON.stringify(formModel.value)
          })
          message.success('更新规则成功')
        } else {
          await request('/rate-limit/rules', {
            method: 'POST',
            body: JSON.stringify(formModel.value)
          })
          message.success('新增规则成功')
        }
        showModal.value = false
        fetchRules()
      } catch (e) {
        message.error(isEdit.value ? '更新失败' : '新增失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await request(`/rate-limit/rules/${id}`, { method: 'DELETE' })
    message.success('删除成功')
    fetchRules()
  } catch (e) {
    message.error('删除失败')
  }
}

const handleRefreshCache = async () => {
  refreshing.value = true
  try {
    const msg = await request('/rate-limit/rules/refresh-cache', { method: 'POST' })
    message.success(msg || '缓存刷新成功')
  } catch (e) {
    message.error('刷新缓存失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(fetchRules)
</script>
