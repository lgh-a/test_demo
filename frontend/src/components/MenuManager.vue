<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:menu:add'" @click="openAddModal">新增菜单/权限</n-button>
    </div>
    
    <n-data-table
      :columns="columns"
      :data="menuTree"
      :row-key="row => row.id"
      :default-expand-all="true"
    />

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '修改菜单/权限' : '新增菜单/权限'" class="w-[500px]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="类型" path="type">
          <n-radio-group v-model:value="formModel.type" name="radiogroup">
            <n-space>
              <n-radio :value="1">目录</n-radio>
              <n-radio :value="2">菜单</n-radio>
              <n-radio :value="3">按钮/权限</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="名称" path="name">
          <n-input v-model:value="formModel.name" placeholder="如：用户管理、添加用户" />
        </n-form-item>
        <n-form-item label="权限标识" path="perms" v-if="formModel.type !== 1">
          <n-input v-model:value="formModel.perms" placeholder="如：sys:user:list" />
        </n-form-item>
        <n-form-item label="父级菜单" path="parentId">
          <n-tree-select
            v-model:value="formModel.parentId"
            :options="parentOptions"
            placeholder="选择父级菜单"
            clearable
            key-field="id"
            label-field="name"
            children-field="children"
          />
        </n-form-item>
        <n-form-item label="路由地址" path="path" v-if="formModel.type !== 3">
          <n-input v-model:value="formModel.path" placeholder="如：/sys/user" />
        </n-form-item>
        <n-form-item label="图标" path="icon" v-if="formModel.type !== 3">
          <n-input v-model:value="formModel.icon" placeholder="图标名称" />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formModel.sort" :min="0" placeholder="数值越小越靠前" class="w-full" />
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
import { ref, h, computed, onMounted } from 'vue'
import { request } from '../api/request'
import { 
  NButton, NDataTable, NTag, NModal, NForm, NFormItem, NInput, NInputNumber, 
  NRadioGroup, NRadio, NSpace, NTreeSelect, useMessage 
} from 'naive-ui'

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

const rules = {
  name: { required: true, message: '请输入名称', trigger: 'blur' },
  type: { required: true, type: 'number', message: '请选择类型', trigger: 'change' }
}

const typeMap = {
  1: { label: '目录', type: 'default' },
  2: { label: '菜单', type: 'info' },
  3: { label: '按钮', type: 'warning' }
}

const menuTree = computed(() => buildTree(menus.value, 0))

const parentOptions = computed(() => {
  const options = buildTree(menus.value.filter(m => m.type !== 3), 0)
  return [{ id: 0, name: '顶级菜单', children: options }]
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', width: 150 },
  { 
    title: '类型', 
    key: 'type', 
    width: 100,
    render: (row) => h(NTag, { type: typeMap[row.type]?.type || 'default', size: 'small' }, 
      () => typeMap[row.type]?.label || '未知')
  },
  { title: '权限标识', key: 'perms', width: 180,
    render: (row) => row.perms || '-'
  },
  { title: '路由', key: 'path', width: 120,
    render: (row) => row.path || '-'
  },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => h('div', { class: 'flex gap-2' }, [
      h(NButton, { 
        size: 'small', 
        vAuth: "'sys:menu:update'",
        onClick: () => openEditModal(row) 
      }, () => '修改'),
      h(NButton, { 
        size: 'small', 
        type: 'error',
        vAuth: "'sys:menu:delete'",
        onClick: () => handleDelete(row) 
      }, () => '删除')
    ])
  }
]

const buildTree = (list, parentId) => {
  return list
    .filter(item => (item.parentId || 0) === parentId)
    .map(item => ({
      ...item,
      children: buildTree(list, item.id).length > 0 ? buildTree(list, item.id) : undefined
    }))
    .sort((a, b) => (a.sort || 0) - (b.sort || 0))
}

const fetchMenus = async () => {
  try {
    menus.value = await request('/menus/list')
  } catch (e) {
    message.error('获取菜单列表失败')
  }
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      try {
        if (isEdit.value) {
          await request(`/menus/${editingId.value}`, {
            method: 'PUT',
            body: JSON.stringify(formModel.value)
          })
          message.success('修改菜单/权限成功')
        } else {
          await request('/menus/add', {
            method: 'POST',
            body: JSON.stringify(formModel.value)
          })
          message.success('新增菜单/权限成功')
        }
        showModal.value = false
        formModel.value = { ...defaultForm }
        fetchMenus()
      } catch (e) {
        message.error(isEdit.value ? '修改失败' : '新增失败')
      } finally {
        loading.value = false
      }
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
    message.success('删除成功')
    fetchMenus()
  } catch (error) {
    message.error(error.message || '删除失败')
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
