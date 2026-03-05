<template>
  <n-modal v-model:show="showModal" preset="dialog" title="用户登录" style="width: 400px">
    <div class="space-y-4 pt-4">
      <n-input v-model:value="form.username" placeholder="用户名 (支持 admin 或 player)" />
      <n-input v-model:value="form.password" type="password" placeholder="密码 (默认 123456)" />
      
      <n-button type="primary" block @click="handleLogin" :loading="loading">
        登录
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

watch(() => props.show, (val) => {
  showModal.value = val
})
watch(() => showModal.value, (val) => {
  emit('update:show', val)
})

const form = ref({ username: '', password: '' })
const loading = ref(false)
const message = useMessage()
const store = useAppStore()

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(form.value)
    })
    
    // Save SA-Token
    localStorage.setItem(data.tokenName, data.tokenValue)
    message.success('登录成功')
    
    await store.fetchUserInfo()
    showModal.value = false
    emit('success')
  } catch (err) {
    message.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
