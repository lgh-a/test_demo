<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:menu:add'" @click="openAddModal">新增菜单/权限</n-button>
    </div>
    
    <n-table :bordered="false" :single-line="false">
      <thead>
        <tr>
          <th>ID</th>
          <th>权限名称</th>
          <th>权限标识</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="menu in menus" :key="menu.id">
          <td>{{ menu.id }}</td>
          <td>{{ menu.name }}</td>
          <td>
            <n-tag :type="menu.type === 1 ? 'info' : 'warning'">{{ menu.perms || '目录' }}</n-tag>
          </td>
          <td>
            <div class="flex gap-2">
              <n-button size="small" v-auth="'sys:menu:update'" @click="openEditModal(menu)">修改</n-button>
              <n-button size="small" type="error" v-auth="'sys:menu:delete'" @click="handleDelete(menu.id)">删除</n-button>
            </div>
          </td>
        </tr>
      </tbody>
    </n-table>

    <!-- Modal for Add/Edit -->
    <n-modal v-model:show="showAdd" preset="card" :title="isEdit ? '修改菜单/权限' : '新增菜单/权限'" class="w-[500px]">
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
        <n-form-item label="父级ID" path="parentId">
          <n-input-number v-model:value="formModel.parentId" placeholder="0 表示顶级" />
        </n-form-item>
        <!-- Adding path and component purely for demonstration, backend may ignore if not defined in entity -->
        <n-form-item label="路由地址" path="path" v-if="formModel.type !== 3">
          <n-input v-model:value="formModel.path" placeholder="如：/sys/user" />
        </n-form-item>
      </n-form>
      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showAdd = false">取消</n-button>
        <n-button type="primary" @click="handleAdd" :loading="loading">确认</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '../api/request'
import { NButton, NTable, NTag, NModal, NForm, NFormItem, NInput, NInputNumber, NRadioGroup, NRadio, NSpace, useMessage } from 'naive-ui'

const menus = ref([])
const showAdd = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const loading = ref(false)
const message = useMessage()

const formRef = ref(null)
const defaultForm = {
  name: '',
  parentId: 0,
  type: 2,
  path: '',
  component: '',
  perms: '',
  icon: ''
}
const formModel = ref({ ...defaultForm })

const rules = {
  name: { required: true, message: '请输入名称', trigger: 'blur' },
  type: { required: true, type: 'number', message: '请选择类型', trigger: 'change' }
}

const fetchMenus = async () => {
  menus.value = await request('/menus/list')
}

const handleAdd = () => {
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
        showAdd.value = false
        formModel.value = { ...defaultForm }
        fetchMenus()
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
    type: Number(menu.type) || 2 // Ensure type is a number for n-radio binding
  }
  showAdd.value = true
}

const handleDelete = async (id) => {
  try {
    await request(`/menus/${id}`, { method: 'DELETE' })
    message.success('删除成功')
    fetchMenus()
  } catch (error) {
    message.error('删除失败')
  }
}

// override original open trigger to reset state
const openAddModal = () => {
  isEdit.value = false
  editingId.value = null
  formModel.value = { ...defaultForm }
  showAdd.value = true
}

onMounted(fetchMenus)
</script>
