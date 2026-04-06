import { computed, ref } from 'vue'
import { messages } from './messages'

const runtimeMessages = {
  ...messages,
  'zh-CN': {
    ...messages['zh-CN'],
    admin: {
      ...messages['zh-CN']?.admin,
      categories: '分类',
      navigation: '导航'
    },
    gameManager: {
      ...messages['zh-CN']?.gameManager,
      status: '状态',
      recommend: '推荐',
      sort: '排序值',
      allStatus: '全部状态',
      allRecommend: '全部推荐',
      selectStatusRequired: '请选择状态',
      selectRecommendRequired: '请选择推荐状态',
      enterSortRequired: '请输入排序值'
    },
    categoryManager: {
      consolesTitle: '主机管理',
      seriesTitle: '系列管理',
      addConsole: '新增主机',
      editConsole: '编辑主机',
      addSeries: '新增系列',
      editSeries: '编辑系列',
      name: '名称',
      fullName: '全称',
      iconUrl: '图标路径',
      description: '描述',
      keywordPlaceholder: '按名称搜索',
      namePlaceholder: '请输入名称',
      fullNamePlaceholder: '请输入全称',
      iconUrlPlaceholder: '例如: /icons/nes.png',
      descriptionPlaceholder: '输入系列描述',
      enterName: '请输入名称',
      enterFullName: '请输入全称',
      createSuccess: '创建成功',
      updateSuccess: '更新成功',
      deleteSuccess: '删除成功',
      loadFailed: '加载列表失败',
      saveFailed: '保存失败',
      deleteFailed: '删除失败',
      deleteConsoleConfirm: '确定删除这个主机吗？',
      deleteSeriesConfirm: '确定删除这个系列吗？'
    }
  },
  'en-US': {
    ...messages['en-US'],
    admin: {
      ...messages['en-US']?.admin,
      navigation: 'Navigation'
    },
    gameManager: {
      ...messages['en-US']?.gameManager,
      status: 'Status',
      recommend: 'Recommend',
      sort: 'Sort',
      allStatus: 'All Status',
      allRecommend: 'All Recommend',
      selectStatusRequired: 'Please select status',
      selectRecommendRequired: 'Please select recommend status',
      enterSortRequired: 'Please enter sort value'
    }
  }
}

const STORAGE_KEY = 'app-locale'
const SUPPORTED_LOCALES = ['zh-CN', 'en-US']

const normalizeLocale = (value) => {
  if (!value) return null
  if (SUPPORTED_LOCALES.includes(value)) return value
  if (value.toLowerCase().startsWith('zh')) return 'zh-CN'
  if (value.toLowerCase().startsWith('en')) return 'en-US'
  return null
}

const detectInitialLocale = () => {
  const saved = normalizeLocale(localStorage.getItem(STORAGE_KEY))
  if (saved) return saved
  const browserLocale = normalizeLocale(navigator.language)
  return browserLocale || 'zh-CN'
}

const locale = ref(detectInitialLocale())

const resolveMessage = (target, path) => {
  return path.split('.').reduce((current, segment) => {
    if (current && Object.prototype.hasOwnProperty.call(current, segment)) {
      return current[segment]
    }
    return undefined
  }, target)
}

const formatMessage = (template, params = {}) => {
  if (typeof template !== 'string') {
    return template
  }

  return template.replace(/\{(\w+)\}/g, (_, key) => String(params[key] ?? ''))
}

export const setLocale = (value) => {
  const next = normalizeLocale(value)
  if (!next || next === locale.value) {
    return
  }
  locale.value = next
  localStorage.setItem(STORAGE_KEY, next)
  document.documentElement.lang = next
}

document.documentElement.lang = locale.value

export const t = (path, params) => {
  const currentMessages = runtimeMessages[locale.value] || runtimeMessages['zh-CN']
  const fallbackMessages = runtimeMessages['en-US']
  const template = resolveMessage(currentMessages, path) ?? resolveMessage(fallbackMessages, path) ?? path
  return formatMessage(template, params)
}

export const localeOptions = [
  { label: '中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' }
]

export const useI18n = () => {
  const isZh = computed(() => locale.value === 'zh-CN')

  return {
    locale,
    localeOptions,
    isZh,
    setLocale,
    t
  }
}
