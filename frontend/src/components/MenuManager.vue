<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:menu:add'" @click="openAddModal">{{ t('menuManager.addMenu') }}</n-button>
    </div>

    <n-data-table :columns="columns" :data="menuTree" :row-key="(row) => row.id" :default-expand-all="true" />

    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="w-[500px]">
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
          <n-input-number v-model:value="formModel.sort" :min="0" :placeholder="t('menuManager.sortPlaceholder')" class="w-full" />
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
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NDataTable,
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
import { useI18n } from '../i18n'

const { t } = useI18n()
const message = useMessage()
const menus = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const loading = ref(false)
const formRef = ref(null)

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

const menuTree = computed(() => buildTree(menus.value, 0))

const parentOptions = computed(() => {
  const options = buildTree(
    menus.value.filter((menu) => menu.type !== 3),
    0
  )
  return [{ id: 0, name: t('menuManager.topLevelMenu'), children: options }]
})

const modalTitle = computed(() => (isEdit.value ? t('menuManager.editMenu') : t('menuManager.addMenu')))

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: t('menuManager.name'), key: 'name', width: 150 },
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
    title: t('menuManager.permissionKey'),
    key: 'perms',
    width: 180,
    render: (row) => row.perms || '-'
  },
  {
    title: t('menuManager.routePath'),
    key: 'path',
    width: 120,
    render: (row) => row.path || '-'
  },
  { title: t('menuManager.sort'), key: 'sort', width: 80 },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 180,
    render: (row) =>
      h('div', { class: 'flex gap-2' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => openEditModal(row)
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row)
          },
          () => t('common.delete')
        )
      ])
  }
])

const fetchMenus = async () => {
  try {
    menus.value = await request('/menus/list')
  } catch (error) {
    message.error(t('menuManager.fetchMenusFailed'))
  }
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    loading.value = true
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
      await fetchMenus()
    } catch (error) {
      message.error(isEdit.value ? t('menuManager.updateFailed') : t('menuManager.addFailed'))
    } finally {
      loading.value = false
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
    await fetchMenus()
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

onMounted(fetchMenus)
</script>
