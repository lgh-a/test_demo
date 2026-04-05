<template>
  <n-modal v-model:show="showModal" preset="card" title="Change Password" class="w-[420px]">
    <div class="space-y-4">
      <n-input
        v-model:value="form.oldPassword"
        type="password"
        show-password-on="click"
        placeholder="Current password"
      />
      <n-input
        v-model:value="form.newPassword"
        type="password"
        show-password-on="click"
        placeholder="New password"
      />
      <n-input
        v-model:value="form.confirmPassword"
        type="password"
        show-password-on="click"
        placeholder="Confirm new password"
      />

      <div class="flex justify-end gap-2 pt-2">
        <n-button @click="showModal = false">Cancel</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">Update</n-button>
      </div>
    </div>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { NButton, NInput, NModal, useMessage } from 'naive-ui'
import { request } from '../api/request'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['update:show', 'success'])
const message = useMessage()
const loading = ref(false)
const createEmptyForm = () => ({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const form = ref(createEmptyForm())

const showModal = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

watch(() => props.show, (value) => {
  if (!value) {
    form.value = createEmptyForm()
  }
})

const handleSubmit = async () => {
  if (!form.value.oldPassword || !form.value.newPassword || !form.value.confirmPassword) {
    message.warning('Please complete all password fields')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    message.warning('The new passwords do not match')
    return
  }
  if (form.value.newPassword.length < 6) {
    message.warning('The new password must be at least 6 characters')
    return
  }

  loading.value = true
  try {
    await request('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({
        oldPassword: form.value.oldPassword,
        newPassword: form.value.newPassword
      })
    })
    message.success('Password updated successfully')
    form.value = createEmptyForm()
    showModal.value = false
    emit('success')
  } catch (error) {
    form.value.oldPassword = ''
    form.value.newPassword = ''
    form.value.confirmPassword = ''
    message.error(error.message || 'Password update failed')
  } finally {
    loading.value = false
  }
}
</script>
