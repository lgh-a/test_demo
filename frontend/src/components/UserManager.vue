<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:user:add'" @click="openCreateModal">Add User</n-button>
    </div>

    <n-data-table :columns="columns" :data="users" :pagination="false" :row-key="row => row.id" />

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? 'Edit User' : 'Add User'" class="w-[520px]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="120">
        <n-form-item label="Username" path="username">
          <n-input v-model:value="formModel.username" :disabled="isEdit" placeholder="Enter username" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="Password" path="password">
          <n-input v-model:value="formModel.password" type="password" placeholder="Enter password" />
        </n-form-item>
        <n-form-item label="Status" path="status">
          <n-select v-model:value="formModel.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="Role" path="roleId">
          <n-select v-model:value="formModel.roleId" :options="roleOptions" />
        </n-form-item>
      </n-form>

      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showModal = false">Cancel</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">Save</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { h, onMounted, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSelect,
  NTag,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'

const message = useMessage()
const store = useAppStore()
const users = ref([])
const roles = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const defaultForm = {
  username: '',
  password: '',
  status: 1,
  roleId: null
}
const formModel = ref({ ...defaultForm })

const statusOptions = [
  { label: 'Enabled', value: 1 },
  { label: 'Disabled', value: 0 }
]

const roleOptions = ref([])

const rules = {
  username: { required: true, message: 'Please enter username', trigger: 'blur' },
  password: { required: true, message: 'Please enter password', trigger: 'blur' },
  status: { required: true, type: 'number', message: 'Please select status', trigger: 'change' },
  roleId: { required: true, type: 'number', message: 'Please select role', trigger: 'change' }
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Username', key: 'username', width: 160 },
  {
    title: 'Status',
    key: 'status',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        () => (row.status === 1 ? 'Enabled' : 'Disabled')
      )
  },
  { title: 'Role', key: 'roleName', width: 140, render: (row) => row.roleName || '-' },
  { title: 'Created At', key: 'createTime', minWidth: 180 },
  {
    title: 'Actions',
    key: 'actions',
    width: 220,
    render: (row) =>
      h('div', { class: 'flex gap-2' }, [
        h(
          NButton,
          {
            size: 'small',
            style: store.hasPerm('sys:user:update') ? '' : 'display:none',
            onClick: () => openEditModal(row)
          },
          () => 'Edit'
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'warning',
            style: store.hasPerm('sys:user:reset-password') ? '' : 'display:none',
            onClick: () => handleResetPassword(row.id)
          },
          () => 'Reset Password'
        )
      ])
  }
]

const fetchUsers = async () => {
  users.value = await request('/users/list')
}

const fetchRoles = async () => {
  roles.value = await request('/roles/list')
  roleOptions.value = roles.value.map((role) => ({
    label: role.name,
    value: role.id
  }))
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

    loading.value = true
    try {
      if (isEdit.value) {
        await request(`/users/${editingId.value}`, {
          method: 'PUT',
          body: JSON.stringify({
            status: formModel.value.status,
            roleId: formModel.value.roleId
          })
        })
        message.success('User updated successfully')
      } else {
        await request('/users', {
          method: 'POST',
          body: JSON.stringify(formModel.value)
        })
        message.success('User created successfully')
      }
      showModal.value = false
      formModel.value = { ...defaultForm }
      await fetchUsers()
    } catch (error) {
      message.error(error.message || 'Operation failed')
    } finally {
      loading.value = false
    }
  })
}

const handleResetPassword = async (id) => {
  try {
    await request(`/users/${id}/reset-password`, { method: 'POST' })
    message.success('Password reset to 123456')
  } catch (error) {
    message.error(error.message || 'Password reset failed')
  }
}

onMounted(async () => {
  await Promise.all([fetchUsers(), fetchRoles()])
})
</script>
