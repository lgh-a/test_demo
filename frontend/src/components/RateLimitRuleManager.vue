<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="t('admin.rateLimit')">
      <template #actions>
        <div class="flex gap-2">
          <n-button type="warning" v-auth="'rate-limit:rule:refresh'" :loading="refreshing" @click="handleRefreshCache">
            {{ t('rateLimit.refreshCache') }}
          </n-button>
          <n-button type="primary" v-auth="'rate-limit:rule:add'" @click="openAddModal">{{ t('rateLimit.addRule') }}</n-button>
        </div>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-[220px_140px]">
          <n-input v-model:value="searchConfigKey" :placeholder="t('rateLimit.searchConfigKey')" clearable />
          <n-select v-model:value="searchStatus" :options="statusOptions" :placeholder="t('rateLimit.filterStatus')" clearable />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">总规则 {{ pagination.total }}</span>
        <span class="admin-stat-chip admin-stat-chip--success rounded-2xl px-3 py-1">当前启用 {{ enabledCount }}</span>
        <span class="admin-stat-chip admin-stat-chip--danger rounded-2xl px-3 py-1">当前停用 {{ disabledCount }}</span>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="ruleList"
        :loading="loadingTable"
        :pagination="false"
        :row-key="(row) => row.id"
        :scroll-x="tableScrollX"
      />
    </AdminDataCard>

    <AdminPaginationBar>
      <n-pagination
        v-model:page="pagination.current"
        :page-count="pagination.pages"
        :page-size="pagination.size"
        show-size-picker
        :page-sizes="[10, 20, 50]"
        @update:page="fetchRules"
        @update:page-size="handlePageSizeChange"
      />
    </AdminPaginationBar>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="admin-form-modal w-[600px] max-w-[calc(100vw-2rem)]">
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
      <div class="admin-form-actions">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.confirm') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref } from 'vue'
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
  NTag,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'
import AdminDataCard from './admin/AdminDataCard.vue'
import AdminDataTable from './admin/AdminDataTable.vue'
import AdminFilterBar from './admin/AdminFilterBar.vue'
import AdminPageCard from './admin/AdminPageCard.vue'
import AdminPaginationBar from './admin/AdminPaginationBar.vue'
import AdminStatsBar from './admin/AdminStatsBar.vue'

const { t } = useI18n()
const message = useMessage()
const store = useAppStore()
const ruleList = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const loading = ref(false)
const loadingTable = ref(false)
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
const enabledCount = computed(() => ruleList.value.filter((item) => item.status === 1).length)
const disabledCount = computed(() => ruleList.value.filter((item) => item.status !== 1).length)

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: t('rateLimit.configKey'), key: 'configKey', width: 180, ellipsis: { tooltip: true } },
  { title: t('rateLimit.name'), key: 'name', width: 180, ellipsis: { tooltip: true } },
  {
    title: t('rateLimit.strategy'),
    key: 'limitType',
    width: 150,
    render: (row) =>
      h(
        NTag,
        { type: row.limitType === 'TOKEN_BUCKET' ? 'success' : 'warning', size: 'small' },
        () => (row.limitType === 'TOKEN_BUCKET' ? t('rateLimit.tokenBucket') : t('rateLimit.flexibleDaily'))
      )
  },
  {
    title: t('rateLimit.loggedInUser'),
    key: 'loggedInConfig',
    width: 220,
    ellipsis: { tooltip: true },
    render: (row) => `${t('rateLimit.capacity')}: ${row.capacity} / ${t('rateLimit.rate')}: ${row.rate}`
  },
  {
    title: t('rateLimit.guest'),
    key: 'guestConfig',
    width: 220,
    ellipsis: { tooltip: true },
    render: (row) => `${t('rateLimit.capacity')}: ${row.guestCapacity} / ${t('rateLimit.rate')}: ${row.guestRate}`
  },
  {
    title: t('common.status'),
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'default', size: 'small' },
        () => (row.status === 1 ? t('common.enabled') : t('common.disabled'))
      )
  },
  { title: t('rateLimit.createdAt'), key: 'createTime', width: 180, ellipsis: { tooltip: true }, render: (row) => formatTime(row.createTime) },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 160,
    align: 'center',
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'admin-table-actions' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => openEditModal(row),
            style: store.hasPerm('rate-limit:rule:edit') ? '' : 'display:none'
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id),
            style: store.hasPerm('rate-limit:rule:delete') ? '' : 'display:none'
          },
          () => t('common.delete')
        )
      ])
  }
])

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchRules = async () => {
  loadingTable.value = true
  try {
    const params = new URLSearchParams({
      current: String(pagination.current),
      size: String(pagination.size)
    })
    if (searchConfigKey.value) params.append('configKey', searchConfigKey.value)
    if (searchStatus.value !== null) params.append('status', String(searchStatus.value))

    const data = await request(`/rate-limit/rules/page?${params.toString()}`)
    ruleList.value = Array.isArray(data.records) ? data.records : []
    pagination.pages = Number(data.pages || 1)
    pagination.total = Number(data.total || 0)
  } catch (error) {
    message.error(t('rateLimit.fetchRulesFailed'))
  } finally {
    loadingTable.value = false
  }
}

const handleQuery = () => {
  pagination.current = 1
  fetchRules()
}

const handleReset = () => {
  searchConfigKey.value = ''
  searchStatus.value = null
  pagination.current = 1
  fetchRules()
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
    if (ruleList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
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
