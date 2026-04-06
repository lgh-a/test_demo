<template>
  <div class="w-full min-w-0 space-y-4">
    <AdminPageCard :eyebrow="t('app.admin')" :title="t('admin.games')">
      <template #actions>
        <n-button v-auth="'game:manage:add'" type="primary" @click="openCreateModal">
          {{ t('gameManager.addGame') }}
        </n-button>
      </template>

      <AdminFilterBar>
        <div class="admin-filter-grid md:grid-cols-2 xl:grid-cols-5">
          <n-input
            v-model:value="filters.keyword"
            :placeholder="t('gameManager.keywordPlaceholder')"
            @keyup.enter="handleQuery"
          />
          <n-select
            v-model:value="filters.consoleId"
            clearable
            :options="consoleOptions"
            :placeholder="t('gameManager.allConsoles')"
          />
          <n-select
            v-model:value="filters.seriesId"
            clearable
            :options="seriesOptions"
            :placeholder="t('gameManager.allSeries')"
          />
          <n-select
            v-model:value="filters.status"
            clearable
            :options="statusOptions"
            :placeholder="t('gameManager.allStatus')"
          />
          <n-select
            v-model:value="filters.isRecommend"
            clearable
            :options="recommendOptions"
            :placeholder="t('gameManager.allRecommend')"
          />
        </div>
        <template #actions>
          <n-button @click="handleReset">{{ t('common.reset') }}</n-button>
          <n-button type="primary" @click="handleQuery">{{ t('common.query') }}</n-button>
        </template>
      </AdminFilterBar>

      <AdminStatsBar class="mt-4">
        <span class="admin-stat-chip rounded-2xl px-3 py-1">总数 {{ pagination.total }}</span>
        <span class="admin-stat-chip admin-stat-chip--success rounded-2xl px-3 py-1">当前 {{ games.length }}</span>
        <span class="admin-stat-chip admin-stat-chip--warning rounded-2xl px-3 py-1">
          推荐 {{ games.filter((item) => item.isRecommend === 1).length }}
        </span>
        <template #actions>
          <n-button quaternary :loading="loading" @click="fetchGames">{{ t('common.refresh') }}</n-button>
        </template>
      </AdminStatsBar>
    </AdminPageCard>

    <AdminDataCard>
      <AdminDataTable
        :columns="columns"
        :data="games"
        :loading="loading"
        :pagination="false"
        :row-key="(row) => row.id"
        :scroll-x="tableScrollX"
      />
    </AdminDataCard>

    <AdminPaginationBar>
      <n-pagination
        v-model:page="pagination.current"
        :item-count="pagination.total"
        :page-size="pagination.size"
        :page-sizes="pageSizes"
        show-size-picker
        @update:page="fetchGames"
        @update:page-size="handlePageSizeChange"
      />
    </AdminPaginationBar>

    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="isEdit ? t('gameManager.editGame') : t('gameManager.addGame')"
      class="admin-form-modal w-[760px] max-w-[calc(100vw-2rem)]"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="110">
        <div class="grid gap-4 md:grid-cols-2">
          <n-form-item :label="t('gameManager.gameName')" path="name">
            <n-input v-model:value="formModel.name" :placeholder="t('gameManager.gameNamePlaceholder')" />
          </n-form-item>
          <n-form-item :label="t('gameManager.console')" path="consoleId">
            <n-select v-model:value="formModel.consoleId" :options="consoleOptions" :placeholder="t('gameManager.selectConsole')" />
          </n-form-item>
          <n-form-item :label="t('gameManager.series')" path="seriesId">
            <n-select
              v-model:value="formModel.seriesId"
              clearable
              :options="seriesOptions"
              :placeholder="t('gameManager.selectSeries')"
            />
          </n-form-item>
          <n-form-item :label="t('gameManager.language')" path="language">
            <n-input v-model:value="formModel.language" />
          </n-form-item>
          <n-form-item :label="t('gameManager.region')" path="region">
            <n-input v-model:value="formModel.region" />
          </n-form-item>
          <n-form-item :label="t('gameManager.genre')" path="genre">
            <n-input v-model:value="formModel.genre" />
          </n-form-item>
          <n-form-item :label="t('gameManager.status')" path="status">
            <n-select v-model:value="formModel.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item :label="t('gameManager.recommend')" path="isRecommend">
            <n-select v-model:value="formModel.isRecommend" :options="recommendOptions" />
          </n-form-item>
          <n-form-item :label="t('gameManager.sort')" path="sort">
            <n-input-number v-model:value="formModel.sort" :min="0" class="w-full" />
          </n-form-item>
        </div>

        <n-form-item :label="t('gameManager.romUrl')" path="romUrl">
          <n-input v-model:value="formModel.romUrl" :placeholder="t('gameManager.romUrlPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('gameManager.thumbUrl')" path="thumbUrl">
          <n-input v-model:value="formModel.thumbUrl" :placeholder="t('gameManager.thumbUrlPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('gameManager.description')" path="description">
          <n-input
            v-model:value="formModel.description"
            type="textarea"
            :rows="4"
            :placeholder="t('gameManager.descriptionPlaceholder')"
          />
        </n-form-item>
      </n-form>

      <div class="admin-form-actions">
        <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" :loading="saving" @click="handleSubmit">{{ t('common.save') }}</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NPagination,
  NSelect,
  NTag,
  useMessage
} from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import { useI18n } from '../i18n'
import AdminDataCard from './admin/AdminDataCard.vue'
import AdminDataTable from './admin/AdminDataTable.vue'
import AdminFilterBar from './admin/AdminFilterBar.vue'
import AdminPageCard from './admin/AdminPageCard.vue'
import AdminPaginationBar from './admin/AdminPaginationBar.vue'
import AdminStatsBar from './admin/AdminStatsBar.vue'

const { t } = useI18n()
const message = useMessage()
const store = useAppStore()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const games = ref([])
const consoles = ref([])
const series = ref([])
const pageSizes = [10, 20, 50]
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})
const filters = ref({
  keyword: '',
  consoleId: null,
  seriesId: null,
  status: null,
  isRecommend: null
})

const defaultForm = {
  name: '',
  description: '',
  romUrl: '',
  thumbUrl: '',
  consoleId: null,
  seriesId: null,
  language: 'Unknown',
  region: 'Unknown',
  genre: '',
  status: 1,
  isRecommend: 0,
  sort: 0
}
const formModel = ref({ ...defaultForm })

const consoleMap = computed(() => Object.fromEntries(consoles.value.map((item) => [item.id, item.name])))
const seriesMap = computed(() => Object.fromEntries(series.value.map((item) => [item.id, item.name])))
const consoleOptions = computed(() =>
  consoles.value.map((item) => ({
    label: `${item.name}${item.fullName ? ` / ${item.fullName}` : ''}`,
    value: item.id
  }))
)
const seriesOptions = computed(() =>
  series.value.map((item) => ({
    label: item.name,
    value: item.id
  }))
)
const statusOptions = computed(() => [
  { label: t('common.enabled'), value: 1 },
  { label: t('common.disabled'), value: 0 }
])
const recommendOptions = computed(() => [
  { label: t('common.yes'), value: 1 },
  { label: t('common.no'), value: 0 }
])

const rules = computed(() => ({
  name: { required: true, message: t('gameManager.enterGameName'), trigger: 'blur' },
  romUrl: { required: true, message: t('gameManager.enterRomUrl'), trigger: 'blur' },
  consoleId: { required: true, type: 'number', message: t('gameManager.selectConsoleRequired'), trigger: 'change' },
  status: { required: true, type: 'number', message: t('gameManager.selectStatusRequired'), trigger: 'change' },
  isRecommend: { required: true, type: 'number', message: t('gameManager.selectRecommendRequired'), trigger: 'change' },
  sort: { required: true, type: 'number', message: t('gameManager.enterSortRequired'), trigger: ['blur', 'change'] }
}))

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 70 },
  { title: t('gameManager.gameName'), key: 'name', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: t('gameManager.console'),
    key: 'consoleId',
    width: 130,
    ellipsis: { tooltip: true },
    render: (row) => consoleMap.value[row.consoleId] || row.consoleId
  },
  {
    title: t('gameManager.series'),
    key: 'seriesId',
    width: 140,
    ellipsis: { tooltip: true },
    render: (row) => seriesMap.value[row.seriesId] || '-'
  },
  {
    title: t('gameManager.language'),
    key: 'language',
    width: 110,
    render: (row) => h(NTag, { size: 'small', type: 'info' }, () => row.language || 'Unknown')
  },
  {
    title: t('gameManager.status'),
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === 1 ? 'success' : 'default' },
        () => (row.status === 1 ? t('common.enabled') : t('common.disabled'))
      )
  },
  {
    title: t('gameManager.recommend'),
    key: 'isRecommend',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.isRecommend === 1 ? 'warning' : 'default' },
        () => (row.isRecommend === 1 ? t('common.yes') : t('common.no'))
      )
  },
  { title: t('gameManager.sort'), key: 'sort', width: 90, render: (row) => row.sort ?? 0 },
  { title: t('gameManager.region'), key: 'region', width: 110, ellipsis: { tooltip: true } },
  { title: t('gameManager.genre'), key: 'genre', width: 120, ellipsis: { tooltip: true }, render: (row) => row.genre || '-' },
  { title: t('gameManager.romUrl'), key: 'romUrl', minWidth: 220, ellipsis: { tooltip: true } },
  { title: t('gameManager.updatedAt'), key: 'updateTime', width: 180, ellipsis: { tooltip: true } },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 160,
    align: 'center',
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'admin-table-actions' }, [
        h(
          NButton,
          {
            size: 'small',
            style: store.hasPerm('game:manage:update') ? '' : 'display:none',
            onClick: () => openEditModal(row)
          },
          () => t('common.edit')
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            style: store.hasPerm('game:manage:delete') ? '' : 'display:none',
            onClick: () => handleDelete(row.id)
          },
          () => t('common.delete')
        )
      ])
  }
])

const tableScrollX = computed(() =>
  columns.value.reduce((total, column) => total + Number(column.width || column.minWidth || 160), 0)
)

const fetchOptions = async () => {
  const [consoleData, seriesData] = await Promise.all([
    request('/categories/consoles'),
    request('/categories/series')
  ])
  consoles.value = Array.isArray(consoleData) ? consoleData : []
  series.value = Array.isArray(seriesData) ? seriesData : []
}

const fetchGames = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      current: String(pagination.value.current),
      size: String(pagination.value.size)
    })
    if (filters.value.keyword.trim()) params.append('keyword', filters.value.keyword.trim())
    if (filters.value.consoleId) params.append('consoleId', String(filters.value.consoleId))
    if (filters.value.seriesId) params.append('seriesId', String(filters.value.seriesId))
    if (filters.value.status !== null) params.append('status', String(filters.value.status))
    if (filters.value.isRecommend !== null) params.append('isRecommend', String(filters.value.isRecommend))

    const data = await request(`/admin/games/page?${params.toString()}`)
    games.value = Array.isArray(data.records) ? data.records : []
    pagination.value.total = Number(data.total || 0)
  } catch (error) {
    message.error(error.message || t('gameManager.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  pagination.value.current = 1
  await fetchGames()
}

const handleReset = async () => {
  filters.value = {
    keyword: '',
    consoleId: null,
    seriesId: null,
    status: null,
    isRecommend: null
  }
  pagination.value.current = 1
  pagination.value.size = 10
  await fetchGames()
}

const handlePageSizeChange = async (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  await fetchGames()
}

const openCreateModal = () => {
  isEdit.value = false
  editingId.value = null
  formModel.value = { ...defaultForm }
  showModal.value = true
}

const openEditModal = (game) => {
  isEdit.value = true
  editingId.value = game.id
  formModel.value = {
    name: game.name || '',
    description: game.description || '',
    romUrl: game.romUrl || '',
    thumbUrl: game.thumbUrl || '',
    consoleId: game.consoleId || null,
    seriesId: game.seriesId || null,
    language: game.language || 'Unknown',
    region: game.region || 'Unknown',
    genre: game.genre || '',
    status: typeof game.status === 'number' ? game.status : 1,
    isRecommend: typeof game.isRecommend === 'number' ? game.isRecommend : 0,
    sort: typeof game.sort === 'number' ? game.sort : 0
  }
  showModal.value = true
}

const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return

    saving.value = true
    try {
      const payload = {
        ...formModel.value,
        seriesId: formModel.value.seriesId || null,
        sort: formModel.value.sort ?? 0
      }

      if (isEdit.value) {
        await request(`/admin/games/${editingId.value}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        })
        message.success(t('gameManager.updateSuccess'))
      } else {
        await request('/admin/games', {
          method: 'POST',
          body: JSON.stringify(payload)
        })
        message.success(t('gameManager.createSuccess'))
      }

      showModal.value = false
      formModel.value = { ...defaultForm }
      await fetchGames()
    } catch (error) {
      message.error(error.message || t('gameManager.saveFailed'))
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id) => {
  if (!window.confirm(t('gameManager.deleteConfirm'))) {
    return
  }

  try {
    await request(`/admin/games/${id}`, { method: 'DELETE' })
    message.success(t('gameManager.deleteSuccess'))
    if (games.value.length === 1 && pagination.value.current > 1) {
      pagination.value.current -= 1
    }
    await fetchGames()
  } catch (error) {
    message.error(error.message || t('gameManager.deleteFailed'))
  }
}

onMounted(async () => {
  await Promise.all([fetchOptions(), fetchGames()])
})
</script>
