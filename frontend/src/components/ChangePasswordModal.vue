<template>
  <n-modal v-model:show="showModal" preset="card" :title="t('auth.changePasswordTitle')" class="w-[420px]">
    <div class="space-y-4">
      <n-input
        v-model:value="form.oldPassword"
        type="password"
        show-password-on="click"
        :placeholder="t('auth.currentPassword')"
      />
      <n-input
        v-model:value="form.newPassword"
        type="password"
        show-password-on="click"
        :placeholder="t('auth.newPassword')"
      />
      <n-input
        v-model:value="form.confirmPassword"
        type="password"
        show-password-on="click"
        :placeholder="t('auth.confirmNewPassword')"
      />

      <div class="flex justify-end gap-2 pt-2">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.update') }}</n-button>
      </div>
    </div>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { NButton, NInput, NModal, useMessage } from 'naive-ui'
import { request } from '../api/request'
import { useI18n } from '../i18n'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['update:show', 'success'])
const message = useMessage()
const loading = ref(false)
const { t } = useI18n()
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
    message.warning(t('auth.completePasswordFields'))
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    message.warning(t('auth.newPasswordsMismatch'))
    return
  }
  if (form.value.newPassword.length < 6) {
    message.warning(t('auth.newPasswordLength'))
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
    message.success(t('auth.passwordUpdated'))
    form.value = createEmptyForm()
    showModal.value = false
    emit('success')
  } catch (error) {
    form.value.oldPassword = ''
    form.value.newPassword = ''
    form.value.confirmPassword = ''
    message.error(error.message || t('auth.passwordUpdateFailed'))
  } finally {
    loading.value = false
  }
}
</script>
