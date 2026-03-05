<template>
  <div class="h-full bg-[#18181c] p-6 text-white min-h-[calc(100vh-3.5rem)]">
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold">后台管理</h1>
      <n-button @click="$router.push('/')">返回首页</n-button>
    </div>
    
    <n-tabs type="segment" animated>
      <n-tab-pane name="roles" tab="角色管理" v-auth="'sys:role:list'">
        <RoleManager />
      </n-tab-pane>
      <n-tab-pane name="menus" tab="菜单管理" v-auth="'sys:menu:list'">
        <MenuManager />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup>
import { NTabs, NTabPane, NButton } from 'naive-ui'
import RoleManager from '../components/RoleManager.vue'
import MenuManager from '../components/MenuManager.vue'
</script>
