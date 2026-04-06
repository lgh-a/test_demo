<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="t('admin.users')">
      <template #actions>
        <n-button type="primary" v-auth="'sys:user:add'" @click="openCreateModal">{{ t('userManager.addUser') }}</n-button>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-[minmax(0,220px)_160px]">
          <n-input v-model:value="filters.keyword" placeholder="Search username" @keyup.enter="handleQuery" />
          <n-select v-model:value="filters.status" clearable :options="statusOptions" :placeholder="t('userManager.status')" />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
          <n-button quaternary :loading="tableLoading" @click="fetchUsers">{{ t('common.refresh') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">总用户 {{ pagination.total }}</span>
        <span class="admin-stat-chip admin-stat-chip--success rounded-2xl px-3 py-1">当前页启用 {{ enabledCount }}</span>
        <span class="admin-stat-chip admin-stat-chip--danger rounded-2xl px-3 py-1">当前页停用 {{ disabledCount }}</span>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="users"
        :loading="tableLoading"
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
        @update:page="fetchUsers"
        @update:page-size="handlePageSizeChange"
      />
    </AdminPaginationBar>

    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="isEdit ? t('userManager.editUser') : t('userManager.addUser')"
      class="admin-form-modal w-[520px] max-w-[calc(100vw-2rem)]"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="120">
        <n-form-item :label="t('userManager.username')" path="username">
          <n-input v-model:value="formModel.username" :disabled="isEdit" :placeholder="t('userManager.enterUsername')" />
        </n-form-item>
        <n-form-item v-if="!isEdit" :label="t('userManager.password')" path="password">
          <n-input v-model:value="formModel.password" type="password" :placeholder="t('userManager.enterPassword')" />
        </n-form-item>
        <n-form-item :label="t('userManager.status')" path="status">
          <n-select v-model:value="formModel.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item :label="t('userManager.role')" path="roleId">
          <n-select v-model:value="formModel.roleId" :options="roleOptions" />
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
  NSelect,
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
const users = ref([])
const roles = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const tableLoading = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const pageSizes = [10, 20, 50]
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})
const filters = ref({
  keyword: '',
  status: null
})

const defaultForm = {
  username: '',
  password: '',
  status: 1,
  roleId: null
}
const formModel = ref({ ...defaultForm })

const statusOptions = computed(() => [
  { label: t('common.enabled'), value: 1 },
  { label: t('common.disabled'), value: 0 }
])

const roleOptions = ref([])
const enabledCount = computed(() => users.value.filter((user) => user.status === 1).length)
const disabledCount = computed(() => users.value.filter((user) => user.status !== 1).length)

const rules = {
  username: { required: true, message: t('userManager.enterUsername'), trigger: 'blur' },
  password: { required: true, message: t('userManager.enterPassword'), trigger: 'blur' },
  status: { required: true, type: 'number', message: t('userManager.selectStatus'), trigger: 'change' },
  roleId: { required: true, type: 'number', message: t('userManager.selectRole'), trigger: 'change' }
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: t('userManager.username'), key: 'username', width: 180, ellipsis: { tooltip: true } },
  {
    title: t('userManager.status'),
    key: 'status',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        () => (row.status === 1 ? t('common.enabled') : t('common.disabled'))
      )
  },
  { title: t('userManager.role'), key: 'roleName', width: 160, ellipsis: { tooltip: true }, render: (row) => row.roleName || '-' },
  { title: t('userManager.createdAt'), key: 'createTime', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 220,
    align: 'center',
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'admin-table-actions' }, [
        h(
          NButton,
          {
            size: 'small',
            style: store.hasPerm('sys:user:update') ? '' : 'display:none',
            onClick: () => openEditModal(row)
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'warning',
            style: store.hasPerm('sys:user:reset-password') ? '' : 'display:none',
            onClick: () => handleResetPassword(row.id)
          },
          () => t('userManager.resetPassword')
        )
      ])
  }
])

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const fetchUsers = async () => {
  tableLoading.value = true
  try {
    const params = new URLSearchParams({
      current: String(pagination.value.current),
      size: String(pagination.value.size)
    })
    if (filters.value.keyword.trim()) {
      params.append('keyword', filters.value.keyword.trim())
    }
    if (filters.value.status !== null) {
      params.append('status', String(filters.value.status))
    }

    const data = await request(`/users/page?${params.toString()}`)
    users.value = Array.isArray(data.records) ? data.records : []
    pagination.value.total = Number(data.total || 0)
  } catch (error) {
    message.error(error.message || t('userManager.operationFailed'))
  } finally {
    tableLoading.value = false
  }
}

const fetchRoles = async () => {
  try {
    roles.value = await request('/roles/list')
    roleOptions.value = roles.value.map((role) => ({
      label: role.name,
      value: role.id
    }))
  } catch (error) {
    message.error(error.message || t('userManager.operationFailed'))
  }
}

const handleQuery = async () => {
  pagination.value.current = 1
  await fetchUsers()
}

const handleReset = async () => {
  filters.value = {
    keyword: '',
    status: null
  }
  pagination.value.current = 1
  pagination.value.size = 10
  await fetchUsers()
}

const handlePageSizeChange = async (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  await fetchUsers()
}

const openCreateModal = () => {
  isEdit.value = false
  editingId.value = null
  formModel.value = { ...defaultForm }
  showModal.value = true
}

const openEditModal = (user) => {
  isEdit.value = true
  editingId.value = user.id
  formModel.value = {
    username: user.username,
    password: '',
    status: user.status,
    roleId: user.roleId
  }
  showModal.value = true
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    saving.value = true
    try {
      if (isEdit.value) {
        await request(`/users/${editingId.value}`, {
          method: 'PUT',
          body: JSON.stringify({
            status: formModel.value.status,
            roleId: formModel.value.roleId
          })
        })
        message.success(t('userManager.userUpdated'))
      } else {
        await request('/users', {
          method: 'POST',
          body: JSON.stringify(formModel.value)
        })
        message.success(t('userManager.userCreated'))
      }
      showModal.value = false
      formModel.value = { ...defaultForm }
      await fetchUsers()
    } catch (error) {
      message.error(error.message || t('userManager.operationFailed'))
    } finally {
      saving.value = false
    }
  })
}

const handleResetPassword = async (id) => {
  try {
    await request(`/users/${id}/reset-password`, { method: 'POST' })
    message.success(t('userManager.passwordResetToDefault'))
  } catch (error) {
    message.error(error.message || t('userManager.passwordResetFailed'))
  }
}

onMounted(async () => {
  await Promise.all([fetchUsers(), fetchRoles()])
})
</script>
