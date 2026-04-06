<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="t('admin.menus')">
      <template #actions>
        <n-button type="primary" v-auth="'sys:menu:add'" @click="openAddModal">{{ t('menuManager.addMenu') }}</n-button>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-[240px]">
          <n-input v-model:value="filters.keyword" placeholder="Search name / path / permission" @keyup.enter="handleQuery" />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
          <n-button quaternary :loading="tableLoading" @click="refreshMenus">{{ t('common.refresh') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">总数 {{ allMenus.length }}</span>
        <span class="admin-stat-chip admin-stat-chip--info rounded-2xl px-3 py-1">当前展示 {{ filteredMenuCount }}</span>
        <span class="admin-stat-chip admin-stat-chip--info rounded-2xl px-3 py-1">目录 {{ menuStats.directory }}</span>
        <span class="admin-stat-chip admin-stat-chip--success rounded-2xl px-3 py-1">菜单 {{ menuStats.menu }}</span>
        <span class="admin-stat-chip admin-stat-chip--warning rounded-2xl px-3 py-1">按钮 {{ menuStats.button }}</span>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="displayMenus"
        :loading="tableLoading"
        :pagination="false"
        :row-key="(row) => row.id"
        :scroll-x="tableScrollX"
        children-key="children"
        default-expand-all
      />
    </AdminDataCard>

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="admin-form-modal w-[500px] max-w-[calc(100vw-2rem)]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item :label="t('menuManager.type')" path="type">
          <n-radio-group v-model:value="formModel.type" name="menu-type">
            <n-space>
              <n-radio :value="1">{{ t('menuManager.directory') }}</n-radio>
              <n-radio :value="2">{{ t('menuManager.menu') }}</n-radio>
              <n-radio :value="3">{{ t('menuManager.button') }}</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item :label="t('menuManager.name')" path="name">
          <n-input v-model:value="formModel.name" :placeholder="t('menuManager.namePlaceholder')" />
        </n-form-item>
        <n-form-item v-if="formModel.type !== 1" :label="t('menuManager.permissionKey')" path="perms">
          <n-input v-model:value="formModel.perms" placeholder="sys:user:list" />
        </n-form-item>
        <n-form-item :label="t('menuManager.parentMenu')" path="parentId">
          <n-tree-select
            v-model:value="formModel.parentId"
            :options="parentOptions"
            :placeholder="t('menuManager.parentMenuPlaceholder')"
            clearable
            key-field="id"
            label-field="name"
            children-field="children"
          />
        </n-form-item>
        <n-form-item v-if="formModel.type !== 3" :label="t('menuManager.routePath')" path="path">
          <n-input v-model:value="formModel.path" placeholder="/sys/user" />
        </n-form-item>
        <n-form-item v-if="formModel.type !== 3" :label="t('menuManager.icon')" path="icon">
          <n-input v-model:value="formModel.icon" :placeholder="t('menuManager.iconPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('menuManager.sort')" path="sort">
          <n-input-number
            v-model:value="formModel.sort"
            :min="0"
            :placeholder="t('menuManager.sortPlaceholder')"
            class="w-full"
          />
        </n-form-item>
      </n-form>
      <div class="admin-form-actions">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="saving" @click="handleSubmit">{{ t('common.confirm') }}</n-button>
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
  NInputNumber,
  NModal,
  NRadio,
  NRadioGroup,
  NSpace,
  NTag,
  NTreeSelect,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'
import AdminDataCard from './admin/AdminDataCard.vue'
import AdminDataTable from './admin/AdminDataTable.vue'
import AdminFilterBar from './admin/AdminFilterBar.vue'
import AdminPageCard from './admin/AdminPageCard.vue'
import AdminStatsBar from './admin/AdminStatsBar.vue'

const { t } = useI18n()
const message = useMessage()
const store = useAppStore()
const allMenus = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const saving = ref(false)
const tableLoading = ref(false)
const formRef = ref(null)
const filters = ref({
  keyword: ''
})

const defaultForm = {
  name: '',
  parentId: 0,
  type: 2,
  path: '',
  icon: '',
  perms: '',
  sort: 0
}
const formModel = ref({ ...defaultForm })

const rules = computed(() => ({
  name: { required: true, message: t('menuManager.enterName'), trigger: 'blur' },
  type: { required: true, type: 'number', message: t('menuManager.selectType'), trigger: 'change' }
}))

const typeMap = computed(() => ({
  1: { label: t('menuManager.directory'), type: 'default' },
  2: { label: t('menuManager.menu'), type: 'info' },
  3: { label: t('menuManager.button'), type: 'warning' }
}))

const buildTree = (list, parentId) => {
  return list
    .filter((item) => (item.parentId || 0) === parentId)
    .map((item) => {
      const children = buildTree(list, item.id)
      return {
        ...item,
        children: children.length > 0 ? children : undefined
      }
    })
    .sort((a, b) => (a.sort || 0) - (b.sort || 0))
}

const normalizedKeyword = computed(() => filters.value.keyword.trim().toLowerCase())

const filteredMenus = computed(() => {
  const keyword = normalizedKeyword.value
  if (!keyword) {
    return allMenus.value
  }

  const menuMap = new Map(allMenus.value.map((item) => [item.id, item]))
  const includedIds = new Set()

  allMenus.value.forEach((item) => {
    const candidates = [item.name, item.path, item.perms]
    const matched = candidates.some((value) => String(value || '').toLowerCase().includes(keyword))
    if (!matched) {
      return
    }

    let current = item
    while (current) {
      includedIds.add(current.id)
      const parentId = current.parentId || 0
      if (!parentId) {
        break
      }
      current = menuMap.get(parentId)
    }
  })

  return allMenus.value.filter((item) => includedIds.has(item.id))
})

const displayMenus = computed(() => buildTree(filteredMenus.value, 0))
const filteredMenuCount = computed(() => filteredMenus.value.length)

const parentOptions = computed(() => {
  const options = buildTree(
    allMenus.value.filter((menu) => Number(menu.type) !== 3),
    0
  )
  return [{ id: 0, name: t('menuManager.topLevelMenu'), children: options }]
})

const modalTitle = computed(() => (isEdit.value ? t('menuManager.editMenu') : t('menuManager.addMenu')))
const menuStats = computed(() => ({
  directory: allMenus.value.filter((item) => Number(item.type) === 1).length,
  menu: allMenus.value.filter((item) => Number(item.type) === 2).length,
  button: allMenus.value.filter((item) => Number(item.type) === 3).length
}))
const parentNameMap = computed(() => Object.fromEntries(allMenus.value.map((item) => [item.id, item.name])))

const columns = computed(() => [
  { title: t('menuManager.name'), key: 'name', minWidth: 240, ellipsis: { tooltip: true } },
  { title: 'ID', key: 'id', width: 80 },
  {
    title: t('menuManager.type'),
    key: 'type',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { type: typeMap.value[row.type]?.type || 'default', size: 'small' },
        () => typeMap.value[row.type]?.label || t('common.unknown')
      )
  },
  {
    title: t('menuManager.parentMenu'),
    key: 'parentId',
    width: 160,
    ellipsis: { tooltip: true },
    render: (row) => (row.parentId ? parentNameMap.value[row.parentId] || row.parentId : t('menuManager.topLevelMenu'))
  },
  {
    title: t('menuManager.permissionKey'),
    key: 'perms',
    width: 220,
    ellipsis: { tooltip: true },
    render: (row) => row.perms || '-'
  },
  {
    title: t('menuManager.routePath'),
    key: 'path',
    width: 180,
    ellipsis: { tooltip: true },
    render: (row) => row.path || '-'
  },
  { title: t('menuManager.sort'), key: 'sort', width: 80 },
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
            style: store.hasPerm('sys:menu:update') ? '' : 'display:none',
            onClick: () => openEditModal(row)
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            style: store.hasPerm('sys:menu:delete') ? '' : 'display:none',
            onClick: () => handleDelete(row)
          },
          () => t('common.delete')
        )
      ])
  }
])

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const fetchAllMenus = async () => {
  tableLoading.value = true
  try {
    allMenus.value = await request('/menus/list')
  } catch (error) {
    message.error(t('menuManager.fetchMenusFailed'))
  } finally {
    tableLoading.value = false
  }
}

const refreshMenus = async () => {
  await fetchAllMenus()
}

const handleQuery = async () => {
  await refreshMenus()
}

const handleReset = async () => {
  filters.value.keyword = ''
  await refreshMenus()
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    saving.value = true
    try {
      if (isEdit.value) {
        await request(`/menus/${editingId.value}`, {
          method: 'PUT',
          body: JSON.stringify(formModel.value)
        })
        message.success(t('menuManager.updateMenuSuccess'))
      } else {
        await request('/menus/add', {
          method: 'POST',
          body: JSON.stringify(formModel.value)
        })
        message.success(t('menuManager.addMenuSuccess'))
      }
      showModal.value = false
      formModel.value = { ...defaultForm }
      await refreshMenus()
    } catch (error) {
      message.error(isEdit.value ? t('menuManager.updateFailed') : t('menuManager.addFailed'))
    } finally {
      saving.value = false
    }
  })
}

const openEditModal = (menu) => {
  isEdit.value = true
  editingId.value = menu.id
  formModel.value = {
    ...defaultForm,
    ...menu,
    type: Number(menu.type) || 2,
    parentId: menu.parentId || 0
  }
  showModal.value = true
}

const handleDelete = async (menu) => {
  try {
    await request(`/menus/${menu.id}`, { method: 'DELETE' })
    message.success(t('menuManager.deleteSuccess'))
    await refreshMenus()
  } catch (error) {
    message.error(error.message || t('menuManager.deleteFailed'))
  }
}

const openAddModal = () => {
  isEdit.value = false
  editingId.value = null
  formModel.value = { ...defaultForm }
  showModal.value = true
}

onMounted(async () => {
  await refreshMenus()
})
</script>
