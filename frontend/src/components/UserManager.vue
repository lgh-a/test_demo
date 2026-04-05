<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:user:add'" @click="openCreateModal">{{ t('userManager.addUser') }}</n-button>
    </div>

    <n-data-table :columns="columns" :data="users" :pagination="false" :row-key="row => row.id" />

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? t('userManager.editUser') : t('userManager.addUser')" class="w-[520px]">
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

      <div class="flex justify-end gap-2 mt-4">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.save') }}</n-button>
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
  NModal,
  NSelect,
  NTag,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'

const { t } = useI18n()
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

const statusOptions = computed(() => [
  { label: t('common.enabled'), value: 1 },
  { label: t('common.disabled'), value: 0 }
])

const roleOptions = ref([])

const rules = {
  username: { required: true, message: t('userManager.enterUsername'), trigger: 'blur' },
  password: { required: true, message: t('userManager.enterPassword'), trigger: 'blur' },
  status: { required: true, type: 'number', message: t('userManager.selectStatus'), trigger: 'change' },
  roleId: { required: true, type: 'number', message: t('userManager.selectRole'), trigger: 'change' }
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: t('userManager.username'), key: 'username', width: 160 },
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
  { title: t('userManager.role'), key: 'roleName', width: 140, render: (row) => row.roleName || '-' },
  { title: t('userManager.createdAt'), key: 'createTime', minWidth: 180 },
  {
    title: t('common.actions'),
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
      loading.value = false
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
