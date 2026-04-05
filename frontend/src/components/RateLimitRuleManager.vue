<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div class="flex gap-2">
        <n-input v-model:value="searchConfigKey" :placeholder="t('rateLimit.searchConfigKey')" clearable style="width: 200px" />
        <n-select v-model:value="searchStatus" :options="statusOptions" :placeholder="t('rateLimit.filterStatus')" clearable style="width: 120px" />
        <n-button @click="fetchRules">{{ t('common.query') }}</n-button>
      </div>
      <div class="flex gap-2">
        <n-button type="warning" v-auth="'rate-limit:rule:refresh'" :loading="refreshing" @click="handleRefreshCache">
          {{ t('rateLimit.refreshCache') }}
        </n-button>
        <n-button type="primary" v-auth="'rate-limit:rule:add'" @click="openAddModal">{{ t('rateLimit.addRule') }}</n-button>
      </div>
    </div>

    <n-table :bordered="false" :single-line="false">
      <thead>
        <tr>
          <th>ID</th>
          <th>{{ t('rateLimit.configKey') }}</th>
          <th>{{ t('rateLimit.name') }}</th>
          <th>{{ t('rateLimit.strategy') }}</th>
          <th>{{ t('rateLimit.loggedInUser') }}</th>
          <th>{{ t('rateLimit.guest') }}</th>
          <th>{{ t('common.status') }}</th>
          <th>{{ t('rateLimit.createdAt') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="rule in rules" :key="rule.id">
          <td>{{ rule.id }}</td>
          <td><n-tag type="info">{{ rule.configKey }}</n-tag></td>
          <td>{{ rule.name }}</td>
          <td>
            <n-tag :type="rule.limitType === 'TOKEN_BUCKET' ? 'success' : 'warning'">
              {{ rule.limitType === 'TOKEN_BUCKET' ? t('rateLimit.tokenBucket') : t('rateLimit.flexibleDaily') }}
            </n-tag>
          </td>
          <td>
            <span class="text-xs">{{ t('rateLimit.capacity') }}: {{ rule.capacity }} / {{ t('rateLimit.rate') }}: {{ rule.rate }}</span>
          </td>
          <td>
            <span class="text-xs">
              {{ t('rateLimit.capacity') }}: {{ rule.guestCapacity }} / {{ t('rateLimit.rate') }}: {{ rule.guestRate }}
            </span>
          </td>
          <td>
            <n-tag :type="rule.status === 1 ? 'success' : 'default'">
              {{ rule.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </n-tag>
          </td>
          <td>{{ formatTime(rule.createTime) }}</td>
          <td>
            <div class="flex gap-2">
              <n-button size="small" v-auth="'rate-limit:rule:edit'" @click="openEditModal(rule)">{{ t('common.edit') }}</n-button>
              <n-button size="small" type="error" v-auth="'rate-limit:rule:delete'" @click="handleDelete(rule.id)">{{ t('common.delete') }}</n-button>
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
        show-size-picker
        :page-sizes="[10, 20, 50]"
        @update:page="fetchRules"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="w-[600px]">
      <n-form ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="120">
        <n-form-item :label="t('rateLimit.configKey')" path="configKey">
          <n-input v-model:value="formModel.configKey" :placeholder="t('rateLimit.configKeyPlaceholder')" :disabled="isEdit" />
        </n-form-item>
        <n-form-item :label="t('rateLimit.name')" path="name">
          <n-input v-model:value="formModel.name" :placeholder="t('rateLimit.namePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('rateLimit.description')" path="description">
          <n-input v-model:value="formModel.description" :placeholder="t('rateLimit.descriptionPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('rateLimit.strategy')" path="limitType">
          <n-select v-model:value="formModel.limitType" :options="limitTypeOptions" />
        </n-form-item>
        <n-divider>{{ t('rateLimit.loggedInConfig') }}</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item :label="t('rateLimit.capacity')" path="capacity">
              <n-input-number v-model:value="formModel.capacity" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item :label="t('rateLimit.rate')" path="rate">
              <n-input-number v-model:value="formModel.rate" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-divider>{{ t('rateLimit.guestConfig') }}</n-divider>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item :label="t('rateLimit.capacity')" path="guestCapacity">
              <n-input-number v-model:value="formModel.guestCapacity" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item :label="t('rateLimit.rate')" path="guestRate">
              <n-input-number v-model:value="formModel.guestRate" :min="1" class="w-full" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item :label="t('common.status')" path="status">
          <n-switch v-model:value="formModel.status" :checked-value="1" :unchecked-value="0">
            <template #checked>{{ t('common.enabled') }}</template>
            <template #unchecked>{{ t('common.disabled') }}</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <div class="mt-4 flex justify-end gap-2">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.confirm') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NDivider,
  NForm,
  NFormItem,
  NGi,
  NGrid,
  NInput,
  NInputNumber,
  NModal,
  NPagination,
  NSelect,
  NSwitch,
  NTable,
  NTag,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useI18n } from '../i18n'

const { t } = useI18n()
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

const statusOptions = computed(() => [
  { label: t('common.enabled'), value: 1 },
  { label: t('common.disabled'), value: 0 }
])

const limitTypeOptions = computed(() => [
  { label: `${t('rateLimit.tokenBucket')} (TOKEN_BUCKET)`, value: 'TOKEN_BUCKET' },
  { label: `${t('rateLimit.flexibleDaily')} (FLEXIBLE)`, value: 'FLEXIBLE' }
])

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

const formRules = computed(() => ({
  configKey: { required: true, message: t('rateLimit.enterConfigKey'), trigger: 'blur' },
  name: { required: true, message: t('rateLimit.enterRuleName'), trigger: 'blur' },
  limitType: { required: true, message: t('rateLimit.selectStrategy'), trigger: 'change' },
  capacity: { required: true, type: 'number', message: t('rateLimit.enterCapacity'), trigger: 'blur' },
  rate: { required: true, type: 'number', message: t('rateLimit.enterRate'), trigger: 'blur' },
  guestCapacity: { required: true, type: 'number', message: t('rateLimit.enterGuestCapacity'), trigger: 'blur' },
  guestRate: { required: true, type: 'number', message: t('rateLimit.enterGuestRate'), trigger: 'blur' }
}))

const modalTitle = computed(() => (isEdit.value ? t('rateLimit.editRule') : t('rateLimit.addRuleTitle')))

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchRules = async () => {
  try {
    const params = new URLSearchParams({
      current: String(pagination.current),
      size: String(pagination.size)
    })
    if (searchConfigKey.value) params.append('configKey', searchConfigKey.value)
    if (searchStatus.value !== null) params.append('status', String(searchStatus.value))

    const data = await request(`/rate-limit/rules/page?${params.toString()}`)
    rules.value = data.records
    pagination.pages = data.pages
    pagination.total = data.total
  } catch (error) {
    message.error(t('rateLimit.fetchRulesFailed'))
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
  } catch (error) {
    message.error(t('rateLimit.fetchRuleDetailFailed'))
  }
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    loading.value = true
    try {
      if (isEdit.value) {
        await request('/rate-limit/rules', {
          method: 'PUT',
          body: JSON.stringify(formModel.value)
        })
        message.success(t('rateLimit.updateRuleSuccess'))
      } else {
        await request('/rate-limit/rules', {
          method: 'POST',
          body: JSON.stringify(formModel.value)
        })
        message.success(t('rateLimit.addRuleSuccess'))
      }
      showModal.value = false
      await fetchRules()
    } catch (error) {
      message.error(isEdit.value ? t('rateLimit.updateRuleFailed') : t('rateLimit.addRuleFailed'))
    } finally {
      loading.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await request(`/rate-limit/rules/${id}`, { method: 'DELETE' })
    message.success(t('rateLimit.deleteSuccess'))
    await fetchRules()
  } catch (error) {
    message.error(t('rateLimit.deleteFailed'))
  }
}

const handleRefreshCache = async () => {
  refreshing.value = true
  try {
    const msg = await request('/rate-limit/rules/refresh-cache', { method: 'POST' })
    message.success(msg || t('rateLimit.refreshCacheSuccess'))
  } catch (error) {
    message.error(t('rateLimit.refreshCacheFailed'))
  } finally {
    refreshing.value = false
  }
}

onMounted(fetchRules)
</script>
