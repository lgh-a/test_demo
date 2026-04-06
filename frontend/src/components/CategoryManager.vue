<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="cardTitle">
      <template #actions>
        <n-button v-if="store.hasPerm(addPermission)" type="primary" @click="openCreateModal">
          {{ createButtonText }}
        </n-button>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-[320px]">
          <n-input
            v-model:value="filters.keyword"
            :placeholder="t('categoryManager.keywordPlaceholder')"
            @keyup.enter="handleQuery"
          />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">{{ isConsole ? '主机管理' : '系列管理' }}</span>
        <span class="admin-stat-chip admin-stat-chip--info rounded-2xl px-3 py-1">总数 {{ pagination.total }}</span>
        <span class="admin-stat-chip admin-stat-chip--success rounded-2xl px-3 py-1">当前 {{ items.length }}</span>
        <template #actions>
          <n-button quaternary :loading="loading" @click="fetchItems">{{ t('common.refresh') }}</n-button>
        </template>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="items"
        :loading="loading"
        :pagination="false"
        :row-key="(row) => row.id"
        :scroll-x="tableScrollX"
      />
    </AdminDataCard>

    <AdminPaginationBar>
      <n-pagination
        v-model:page="pagination.current"
        :item-count="pagination.total"
        :page-size="pagination.size"
        :page-sizes="pageSizes"
        show-size-picker
        @update:page="fetchItems"
        @update:page-size="handlePageSizeChange"
      />
    </AdminPaginationBar>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="admin-form-modal w-[640px] max-w-[calc(100vw-2rem)]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="110">
        <n-form-item :label="t('categoryManager.name')" path="name">
          <n-input v-model:value="formModel.name" :placeholder="t('categoryManager.namePlaceholder')" />
        </n-form-item>
        <n-form-item v-if="isConsole" :label="t('categoryManager.fullName')" path="fullName">
          <n-input v-model:value="formModel.fullName" :placeholder="t('categoryManager.fullNamePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('categoryManager.iconUrl')" path="iconUrl">
          <n-input v-model:value="formModel.iconUrl" :placeholder="t('categoryManager.iconUrlPlaceholder')" />
        </n-form-item>
        <n-form-item v-if="!isConsole" :label="t('categoryManager.description')" path="description">
          <n-input
            v-model:value="formModel.description"
            type="textarea"
            :rows="4"
            :placeholder="t('categoryManager.descriptionPlaceholder')"
          />
        </n-form-item>
      </n-form>

      <div class="admin-form-actions">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="saving" @click="handleSubmit">{{ t('common.save') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPagination,
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

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['console', 'series'].includes(value)
  }
})

const { t } = useI18n()
const message = useMessage()
const store = useAppStore()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const items = ref([])
const pageSizes = [10, 20, 50]
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})
const filters = ref({
  keyword: ''
})

const isConsole = computed(() => props.mode === 'console')
const apiBase = computed(() => (isConsole.value ? '/admin/categories/consoles' : '/admin/categories/series'))
const addPermission = computed(() => (isConsole.value ? 'console:manage:add' : 'series:manage:add'))
const updatePermission = computed(() => (isConsole.value ? 'console:manage:update' : 'series:manage:update'))
const deletePermission = computed(() => (isConsole.value ? 'console:manage:delete' : 'series:manage:delete'))
const cardTitle = computed(() => (isConsole.value ? t('categoryManager.consolesTitle') : t('categoryManager.seriesTitle')))
const createButtonText = computed(() => (isConsole.value ? t('categoryManager.addConsole') : t('categoryManager.addSeries')))
const modalTitle = computed(() => {
  if (isConsole.value) {
    return isEdit.value ? t('categoryManager.editConsole') : t('categoryManager.addConsole')
  }
  return isEdit.value ? t('categoryManager.editSeries') : t('categoryManager.addSeries')
})

const defaultForm = computed(() =>
  isConsole.value
    ? {
        name: '',
        fullName: '',
        iconUrl: ''
      }
    : {
        name: '',
        iconUrl: '',
        description: ''
      }
)
const formModel = ref({ ...defaultForm.value })

const rules = computed(() => {
  const baseRules = {
    name: { required: true, message: t('categoryManager.enterName'), trigger: 'blur' }
  }
  if (isConsole.value) {
    baseRules.fullName = { required: true, message: t('categoryManager.enterFullName'), trigger: 'blur' }
  }
  return baseRules
})

const columns = computed(() => {
  const baseColumns = [
    { title: 'ID', key: 'id', width: 80 },
    { title: t('categoryManager.name'), key: 'name', minWidth: 180, ellipsis: { tooltip: true } }
  ]

  if (isConsole.value) {
    baseColumns.push(
      { title: t('categoryManager.fullName'), key: 'fullName', minWidth: 220, ellipsis: { tooltip: true } },
      { title: t('categoryManager.iconUrl'), key: 'iconUrl', minWidth: 220, ellipsis: { tooltip: true }, render: (row) => row.iconUrl || '-' }
    )
  } else {
    baseColumns.push(
      { title: t('categoryManager.iconUrl'), key: 'iconUrl', minWidth: 220, ellipsis: { tooltip: true }, render: (row) => row.iconUrl || '-' },
      { title: t('categoryManager.description'), key: 'description', minWidth: 260, ellipsis: { tooltip: true }, render: (row) => row.description || '-' }
    )
  }

  baseColumns.push({
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
            style: store.hasPerm(updatePermission.value) ? '' : 'display:none',
            onClick: () => openEditModal(row)
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            style: store.hasPerm(deletePermission.value) ? '' : 'display:none',
            onClick: () => handleDelete(row.id)
          },
          () => t('common.delete')
        )
      ])
  })

  return baseColumns
})

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const resetForm = () => {
  formModel.value = { ...defaultForm.value }
}

const fetchItems = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      current: String(pagination.value.current),
      size: String(pagination.value.size)
    })
    if (filters.value.keyword.trim()) {
      params.append('keyword', filters.value.keyword.trim())
    }
    const data = await request(`${apiBase.value}/page?${params.toString()}`)
    items.value = Array.isArray(data.records) ? data.records : []
    pagination.value.total = Number(data.total || 0)
  } catch (error) {
    message.error(error.message || t('categoryManager.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  pagination.value.current = 1
  await fetchItems()
}

const handleReset = async () => {
  filters.value.keyword = ''
  pagination.value.current = 1
  pagination.value.size = 10
  await fetchItems()
}

const handlePageSizeChange = async (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  await fetchItems()
}

const openCreateModal = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

const openEditModal = (row) => {
  isEdit.value = true
  editingId.value = row.id
  formModel.value = isConsole.value
    ? {
        name: row.name || '',
        fullName: row.fullName || '',
        iconUrl: row.iconUrl || ''
      }
    : {
        name: row.name || '',
        iconUrl: row.iconUrl || '',
        description: row.description || ''
      }
  showModal.value = true
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    saving.value = true
    try {
      const payload = isConsole.value
        ? {
            name: formModel.value.name,
            fullName: formModel.value.fullName,
            iconUrl: formModel.value.iconUrl || null
          }
        : {
            name: formModel.value.name,
            iconUrl: formModel.value.iconUrl || null,
            description: formModel.value.description || null
          }

      if (isEdit.value) {
        await request(`${apiBase.value}/${editingId.value}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        })
        message.success(t('categoryManager.updateSuccess'))
      } else {
        await request(apiBase.value, {
          method: 'POST',
          body: JSON.stringify(payload)
        })
        message.success(t('categoryManager.createSuccess'))
      }

      showModal.value = false
      resetForm()
      await fetchItems()
    } catch (error) {
      message.error(error.message || t('categoryManager.saveFailed'))
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id) => {
  const confirmed = window.confirm(
    isConsole.value ? t('categoryManager.deleteConsoleConfirm') : t('categoryManager.deleteSeriesConfirm')
  )
  if (!confirmed) {
    return
  }

  try {
    await request(`${apiBase.value}/${id}`, { method: 'DELETE' })
    message.success(t('categoryManager.deleteSuccess'))
    if (items.value.length === 1 && pagination.value.current > 1) {
      pagination.value.current -= 1
    }
    await fetchItems()
  } catch (error) {
    message.error(error.message || t('categoryManager.deleteFailed'))
  }
}

onMounted(async () => {
  resetForm()
  await fetchItems()
})
</script>
