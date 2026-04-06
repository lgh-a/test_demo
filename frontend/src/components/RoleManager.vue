<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="t('admin.roles')">
      <template #actions>
        <n-button type="primary" v-auth="'sys:role:add'" @click="showAdd = true">{{ t('roleManager.addRole') }}</n-button>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-[240px]">
          <n-input v-model:value="filters.keyword" placeholder="Search role name" @keyup.enter="handleQuery" />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
          <n-button quaternary :loading="tableLoading" @click="fetchRoles">{{ t('common.refresh') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">总角色 {{ pagination.total }}</span>
        <span class="admin-stat-chip admin-stat-chip--info rounded-2xl px-3 py-1">当前页 {{ roles.length }}</span>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="roles"
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
        @update:page="fetchRoles"
        @update:page-size="handlePageSizeChange"
      />
    </AdminPaginationBar>

    <n-modal v-model:show="showAdd" preset="card" :title="t('roleManager.addRole')" class="admin-form-modal w-[500px] max-w-[calc(100vw-2rem)]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item :label="t('roleManager.roleName')" path="name">
          <n-input v-model:value="formModel.name" :placeholder="t('roleManager.roleNamePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('roleManager.remark')" path="remark">
          <n-input v-model:value="formModel.remark" :placeholder="t('roleManager.remarkPlaceholder')" />
        </n-form-item>
      </n-form>
      <div class="admin-form-actions">
        <n-button @click="showAdd = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleAdd">{{ t('common.confirm') }}</n-button>
      </div>
    </n-modal>

    <n-modal
      v-model:show="showAssign"
      preset="card"
      :title="t('roleManager.assignTitle', { name: currentRole?.name || '' })"
      class="admin-form-modal w-[500px] max-w-[calc(100vw-2rem)]"
    >
      <div v-if="menuTree.length" class="max-h-[400px] overflow-auto">
        <n-tree
          v-model:checked-keys="checkedMenuIds"
          v-model:indeterminate-keys="indeterminateMenuIds"
          block-line
          cascade
          checkable
          :data="menuTree"
          key-field="id"
          label-field="name"
          children-field="children"
          default-expand-all
        />
      </div>
      <div class="admin-form-actions">
        <n-button @click="showAssign = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleAssign">{{ t('roleManager.saveAssignment') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { NButton, NForm, NFormItem, NInput, NModal, NPagination, NTree, useMessage } from 'naive-ui'
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
const roles = ref([])
const allMenus = ref([])
const showAdd = ref(false)
const formRef = ref(null)
const formModel = ref({ name: '', remark: '' })
const rules = {
  name: { required: true, message: t('roleManager.enterRoleName'), trigger: 'blur' }
}
const loading = ref(false)
const tableLoading = ref(false)
const showAssign = ref(false)
const checkedMenuIds = ref([])
const indeterminateMenuIds = ref([])
const currentRole = ref(null)
const pageSizes = [10, 20, 50]
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})
const filters = ref({
  keyword: ''
})

const buildMenuTree = (list, parentId = 0) => {
  return list
    .filter((item) => (item.parentId || 0) === parentId)
    .map((item) => ({
      ...item,
      children: buildMenuTree(list, item.id)
    }))
    .filter((item) => item.children.length > 0 || item.type !== 1)
}

const menuTree = computed(() => buildMenuTree(allMenus.value))

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 90 },
  { title: t('roleManager.roleName'), key: 'name', minWidth: 180, ellipsis: { tooltip: true } },
  { title: t('roleManager.remark'), key: 'remark', minWidth: 260, ellipsis: { tooltip: true }, render: (row) => row.remark || '-' },
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
            style: store.hasPerm('sys:role:add') ? '' : 'display:none',
            onClick: () => openAssignModal(row)
          },
          () => t('roleManager.assignPermissions')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            style: store.hasPerm('sys:role:delete') ? '' : 'display:none',
            onClick: () => handleDelete(row.id)
          },
          () => t('common.delete')
        )
      ])
  }
])

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const fetchRoles = async () => {
  tableLoading.value = true
  try {
    const params = new URLSearchParams({
      current: String(pagination.value.current),
      size: String(pagination.value.size)
    })
    if (filters.value.keyword.trim()) {
      params.append('keyword', filters.value.keyword.trim())
    }

    const data = await request(`/roles/page?${params.toString()}`)
    roles.value = Array.isArray(data.records) ? data.records : []
    pagination.value.total = Number(data.total || 0)
  } catch (error) {
    message.error(error.message || t('roleManager.loadPermissionDataFailed'))
  } finally {
    tableLoading.value = false
  }
}

const fetchAllMenus = async () => {
  allMenus.value = await request('/menus/list')
}

const handleQuery = async () => {
  pagination.value.current = 1
  await fetchRoles()
}

const handleReset = async () => {
  filters.value.keyword = ''
  pagination.value.current = 1
  pagination.value.size = 10
  await fetchRoles()
}

const handlePageSizeChange = async (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  await fetchRoles()
}

const handleAdd = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    loading.value = true
    try {
      await request('/roles/add', {
        method: 'POST',
        body: JSON.stringify(formModel.value)
      })
      message.success(t('roleManager.addRoleSuccess'))
      showAdd.value = false
      formModel.value = { name: '', remark: '' }
      await fetchRoles()
    } catch (error) {
      message.error(error.message || t('roleManager.loadPermissionDataFailed'))
    } finally {
      loading.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await request(`/roles/${id}`, { method: 'DELETE' })
    message.success(t('roleManager.deleteSuccess'))
    if (roles.value.length === 1 && pagination.value.current > 1) {
      pagination.value.current -= 1
    }
    await fetchRoles()
  } catch (error) {
    message.error(error.message || t('roleManager.loadPermissionDataFailed'))
  }
}

const openAssignModal = async (role) => {
  currentRole.value = role
  loading.value = true
  try {
    if (allMenus.value.length === 0) {
      await fetchAllMenus()
    }

    const roleMenus = await request(`/roles/${role.id}/menus`)
    const parentNodeIds = new Set(allMenus.value.map((menu) => menu.parentId).filter((id) => id))
    checkedMenuIds.value = roleMenus.filter((id) => !parentNodeIds.has(id))
    indeterminateMenuIds.value = []
    showAssign.value = true
  } catch (error) {
    message.error(t('roleManager.loadPermissionDataFailed'))
  } finally {
    loading.value = false
  }
}

const handleAssign = async () => {
  if (!currentRole.value) return

  loading.value = true
  try {
    const finalMenuIds = [...new Set([...checkedMenuIds.value, ...indeterminateMenuIds.value])]
    await request(`/roles/${currentRole.value.id}/menus`, {
      method: 'POST',
      body: JSON.stringify({ menuIds: finalMenuIds })
    })
    message.success(t('roleManager.assignSuccess'))
    showAssign.value = false
  } catch (error) {
    message.error(t('roleManager.loadPermissionDataFailed'))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchAllMenus()])
})
</script>
