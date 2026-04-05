<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <n-button type="primary" v-auth="'sys:role:add'" @click="showAdd = true">{{ t('roleManager.addRole') }}</n-button>
    </div>

    <n-table :bordered="false" :single-line="false">
      <thead>
        <tr>
          <th>ID</th>
          <th>{{ t('roleManager.roleName') }}</th>
          <th>{{ t('roleManager.remark') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="role in roles" :key="role.id">
          <td>{{ role.id }}</td>
          <td>{{ role.name }}</td>
          <td>{{ role.remark }}</td>
          <td>
            <div class="flex gap-2">
              <n-button size="small" v-auth="'sys:role:add'" @click="openAssignModal(role)">{{ t('roleManager.assignPermissions') }}</n-button>
              <n-button size="small" type="error" v-auth="'sys:role:delete'" @click="handleDelete(role.id)">{{ t('common.delete') }}</n-button>
            </div>
          </td>
        </tr>
      </tbody>
    </n-table>

    <n-modal v-model:show="showAdd" preset="card" :title="t('roleManager.addRole')" class="w-[500px]">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100">
        <n-form-item :label="t('roleManager.roleName')" path="name">
          <n-input v-model:value="formModel.name" :placeholder="t('roleManager.roleNamePlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('roleManager.remark')" path="remark">
          <n-input v-model:value="formModel.remark" :placeholder="t('roleManager.remarkPlaceholder')" />
        </n-form-item>
      </n-form>
      <div class="mt-4 flex justify-end gap-2">
        <n-button @click="showAdd = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleAdd">{{ t('common.confirm') }}</n-button>
      </div>
    </n-modal>

    <n-modal
      v-model:show="showAssign"
      preset="card"
      :title="t('roleManager.assignTitle', { name: currentRole?.name || '' })"
      class="w-[500px]"
    >
      <div v-if="menus.length" class="max-h-[400px] overflow-auto">
        <n-tree
          v-model:checked-keys="checkedMenuIds"
          v-model:indeterminate-keys="indeterminateMenuIds"
          block-line
          cascade
          checkable
          :data="menus"
          key-field="id"
          label-field="name"
          children-field="children"
          default-expand-all
        />
      </div>
      <div class="mt-4 flex justify-end gap-2">
        <n-button @click="showAssign = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleAssign">{{ t('roleManager.saveAssignment') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { NButton, NForm, NFormItem, NInput, NModal, NTable, NTree, useMessage } from 'naive-ui'
import { request } from '../api/request'
import { useI18n } from '../i18n'

const { t } = useI18n()
const message = useMessage()
const roles = ref([])
const showAdd = ref(false)
const formRef = ref(null)
const formModel = ref({ name: '', remark: '' })
const rules = {
  name: { required: true, message: t('roleManager.enterRoleName'), trigger: 'blur' }
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
    } finally {
      loading.value = false
    }
  })
}

const handleDelete = async (id) => {
  await request(`/roles/${id}`, { method: 'DELETE' })
  message.success(t('roleManager.deleteSuccess'))
  await fetchRoles()
}

const buildMenuTree = (list, parentId = 0) => {
  return list
    .filter((item) => (item.parentId || 0) === parentId)
    .map((item) => ({
      ...item,
      children: buildMenuTree(list, item.id)
    }))
    .filter((item) => item.children.length > 0 || item.type !== 1)
}

const openAssignModal = async (role) => {
  currentRole.value = role
  loading.value = true
  try {
    const allMenus = await request('/menus/list')
    menus.value = buildMenuTree(allMenus)

    const roleMenus = await request(`/roles/${role.id}/menus`)
    const parentNodeIds = new Set(allMenus.map((menu) => menu.parentId).filter((id) => id))
    checkedMenuIds.value = roleMenus.filter((id) => !parentNodeIds.has(id))
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
      body: JSON.stringify(finalMenuIds)
    })
    message.success(t('roleManager.assignSuccess'))
    showAssign.value = false
  } finally {
    loading.value = false
  }
}

onMounted(fetchRoles)
</script>
