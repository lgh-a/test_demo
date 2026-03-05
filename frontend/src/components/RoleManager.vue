<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:role:add'" @click="showAdd = true">新增角色</n-button>
    </div>
    
    <n-table :bordered="false" :single-line="false">
      <thead>
        <tr>
          <th>ID</th>
          <th>角色名</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="role in roles" :key="role.id">
          <td>{{ role.id }}</td>
          <td>{{ role.name }}</td>
          <td>{{ role.remark }}</td>
          <td>
            <div class="flex gap-2">
              <n-button size="small" v-auth="'sys:role:add'" @click="openAssignModal(role)">分配权限</n-button>
              <n-button size="small" type="error" v-auth="'sys:role:delete'" @click="handleDelete(role.id)">删除</n-button>
            </div>
          </td>
        </tr>
      </tbody>
    </n-table>

    <!-- Add Role Modal -->
    <n-modal v-model:show="showAdd" preset="card" title="新增角色" class="w-[500px]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="角色名称" path="name">
          <n-input v-model:value="formModel.name" placeholder="输入角色名称 (如 admin)" />
        </n-form-item>
        <n-form-item label="角色备注" path="remark">
          <n-input v-model:value="formModel.remark" placeholder="输入备注信息" />
        </n-form-item>
      </n-form>
      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showAdd = false">取消</n-button>
        <n-button type="primary" @click="handleAdd" :loading="loading">确认</n-button>
      </div>
    </n-modal>

    <!-- Assign Menu Modal -->
    <n-modal v-model:show="showAssign" preset="card" :title="'分配权限 - ' + currentRole?.name" class="w-[500px]">
      <div v-if="menus.length" class="max-h-[400px] overflow-auto">
         <n-tree
          block-line
          cascade
          checkable
          :data="menus"
          key-field="id"
          label-field="name"
          children-field="children"
          v-model:checked-keys="checkedMenuIds"
          v-model:indeterminate-keys="indeterminateMenuIds"
          default-expand-all
        />
      </div>
      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showAssign = false">取消</n-button>
        <n-button type="primary" @click="handleAssign" :loading="loading">保存分派</n-button>
      </div>
    </n-modal>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '../api/request'
import { NButton, NTable, useMessage, NModal, NForm, NFormItem, NInput, NTree } from 'naive-ui'

const roles = ref([])
const showAdd = ref(false)
const message = useMessage()

const formRef = ref(null)
const formModel = ref({ name: '', remark: '' })
const rules = {
  name: { required: true, message: '请输入角色名称', trigger: 'blur' }
}
const loading = ref(false)

const showAssign = ref(false)
const menus = ref([])
const checkedMenuIds = ref([])
const indeterminateMenuIds = ref([])
const currentRole = ref(null)

const fetchRoles = async () => {
  roles.value = await request('/roles/list')
}

const handleAdd = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      try {
        await request('/roles/add', {
          method: 'POST',
          body: JSON.stringify(formModel.value)
        })
        message.success('新增角色成功')
        showAdd.value = false
        formModel.value = { name: '', remark: '' }
        fetchRoles()
      } finally {
        loading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  await request(`/roles/${id}`, { method: 'DELETE' })
  message.success('删除成功')
  fetchRoles()
}

// Build a flat list to tree for n-tree component
const buildMenuTree = (list, parentId = 0) => {
  return list
    .filter(item => (item.parentId || 0) === parentId)
    .map(item => ({
      ...item,
      children: buildMenuTree(list, item.id)
    }))
    .filter(item => item.children.length > 0 || item.type !== 1) // simplificaton for demo
}

const openAssignModal = async (role) => {
  currentRole.value = role
  loading.value = true
  try {
    // Fetch all menus
    const allMenus = await request('/menus/list')
    menus.value = buildMenuTree(allMenus)
    
    // Fetch role's current menus
    const roleMenus = await request(`/roles/${role.id}/menus`)
    
    // N-tree should only receive leaf nodes in checked-keys to avoid accidentally checking unselected children
    const parentNodeIds = new Set(allMenus.map(m => m.parentId).filter(id => id))
    checkedMenuIds.value = roleMenus.filter(id => !parentNodeIds.has(id))
    
    showAssign.value = true
  } catch (e) {
    message.error('获取权限数据失败')
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
      body: JSON.stringify(finalMenuIds)
    })
    message.success('权限分配成功')
    showAssign.value = false
  } finally {
    loading.value = false
  }
}

onMounted(fetchRoles)
</script>
