<template>
  <n-modal v-model:show="showModal" preset="dialog" title="User Login" style="width: 400px">
    <div class="space-y-4 pt-4">
      <n-input v-model:value="form.username" placeholder="Username (admin or player)" />
      <n-input v-model:value="form.password" type="password" placeholder="Password (default 123456)" />

      <n-button type="primary" block @click="handleLogin" :loading="loading">
        Login
      </n-button>
    </div>
  </n-modal>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import { request } from '../api/request'
import { useMessage, NModal, NInput, NButton } from 'naive-ui'
import { useAppStore } from '../store'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['update:show', 'success'])
const showModal = ref(props.show)
const createEmptyForm = () => ({ username: '', password: '' })

watch(() => props.show, (val) => {
  showModal.value = val
})

watch(() => showModal.value, (val) => {
  if (!val) {
    form.value.password = ''
  }
  emit('update:show', val)
})

const form = ref(createEmptyForm())
const loading = ref(false)
const message = useMessage()
const store = useAppStore()

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('Please enter username and password')
    return
  }

  loading.value = true
  try {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(form.value)
    })

    localStorage.setItem(data.tokenName, data.tokenValue)
    message.success('Login successful')

    await store.fetchUserInfo()
    form.value = createEmptyForm()
    showModal.value = false
    emit('success')
  } catch (err) {
    form.value.password = ''
    message.error(err.message || 'Login failed')
  } finally {
    loading.value = false
  }
}
</script>
