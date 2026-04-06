import { computed, ref, watch } from 'vue'

const THEME_STORAGE_KEY = 'app-theme-mode'

const getSystemTheme = () => {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return 'dark'
  }
  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark'
}

const getStoredTheme = () => {
  if (typeof window === 'undefined') {
    return 'dark'
  }
  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)
  if (storedTheme === 'light' || storedTheme === 'dark') {
    return storedTheme
  }
  return getSystemTheme()
}

const themeMode = ref(getStoredTheme())

const applyTheme = (theme) => {
  if (typeof document === 'undefined') {
    return
  }
  document.documentElement.dataset.theme = theme
}

applyTheme(themeMode.value)

watch(
  themeMode,
  (theme) => {
    applyTheme(theme)
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(THEME_STORAGE_KEY, theme)
    }
  },
  { immediate: true }
)

export const useTheme = () => {
  const isDark = computed(() => themeMode.value === 'dark')

  const setTheme = (theme) => {
    if (theme === 'light' || theme === 'dark') {
      themeMode.value = theme
    }
  }

  const toggleTheme = () => {
    themeMode.value = isDark.value ? 'light' : 'dark'
  }

  return {
    isDark,
    setTheme,
    themeMode,
    toggleTheme
  }
}
