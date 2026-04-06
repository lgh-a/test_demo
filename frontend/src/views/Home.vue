<template>
  <div class="home-page space-y-8 pb-10">
    <section class="hero-panel rounded-[28px] px-6 py-8 sm:px-8 lg:px-10 lg:py-10">
      <div class="grid gap-8 lg:grid-cols-[minmax(0,1.4fr)_360px] lg:items-center">
        <div class="space-y-6">
          <div class="inline-flex items-center rounded-full border border-[var(--app-border)] bg-[var(--app-surface-soft)] px-4 py-1 text-sm text-[var(--app-text-muted)]">
            {{ heroBadge }}
          </div>

          <div class="space-y-4">
            <h1 class="max-w-4xl text-4xl font-black leading-tight sm:text-5xl">
              {{ heroTitle }}
            </h1>
            <p class="max-w-2xl text-base leading-7 text-[var(--app-text-muted)] sm:text-lg">
              {{ heroDescription }}
            </p>
          </div>

          <div class="hero-search rounded-2xl p-3 sm:p-4">
            <div class="flex flex-col gap-3 sm:flex-row">
              <n-input
                v-model:value="searchKeyword"
                size="large"
                clearable
                :placeholder="t('home.searchPlaceholder')"
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <span>{{ t('home.searchPrefix') }}</span>
                </template>
              </n-input>
              <n-button type="primary" size="large" class="sm:w-36" @click="handleSearch">
                {{ actionTexts.search }}
              </n-button>
            </div>
          </div>

          <div class="flex flex-wrap gap-3">
            <n-button strong secondary @click="$router.push('/games')">
              {{ actionTexts.browseLibrary }}
            </n-button>
            <n-button v-if="store.isLoggedIn" quaternary @click="$router.push('/favorites')">
              {{ actionTexts.viewFavorites }}
            </n-button>
            <n-button
              v-if="store.permissions.some((permission) => permission.startsWith('sys:'))"
              quaternary
              @click="$router.push('/admin')"
            >
              {{ actionTexts.openAdmin }}
            </n-button>
          </div>
        </div>

        <div class="hero-aside rounded-[24px] p-5 sm:p-6">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-sm font-medium text-[var(--app-text-muted)]">{{ actionTexts.dashboardTitle }}</div>
              <div class="mt-1 text-2xl font-bold">{{ dashboardTitle }}</div>
            </div>
            <div class="rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] px-3 py-2 text-xs text-[var(--app-text-muted)]">
              {{ dashboardTag }}
            </div>
          </div>

          <div class="mt-5 grid grid-cols-2 gap-3">
            <div v-for="stat in heroStats" :key="stat.label" class="stat-tile rounded-2xl p-4">
              <div class="text-2xl font-bold">{{ stat.value }}</div>
              <div class="mt-2 text-sm text-[var(--app-text-muted)]">{{ stat.label }}</div>
            </div>
          </div>

          <div class="mt-5 space-y-3">
            <div
              v-for="highlight in heroHighlights"
              :key="highlight.title"
              class="highlight-row flex items-start gap-3 rounded-2xl px-4 py-3"
            >
              <div class="highlight-dot mt-1 h-2.5 w-2.5 rounded-full"></div>
              <div>
                <div class="font-semibold">{{ highlight.title }}</div>
                <div class="mt-1 text-sm leading-6 text-[var(--app-text-muted)]">{{ highlight.description }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="grid gap-6 xl:grid-cols-[minmax(0,1.4fr)_minmax(320px,0.8fr)]">
      <div class="surface-card rounded-[24px] p-5 sm:p-6">
        <div class="mb-5 flex items-center justify-between gap-4">
          <div>
            <div class="section-kicker">{{ t('home.consoles') }}</div>
            <h2 class="section-title">{{ sectionTexts.consoleTitle }}</h2>
          </div>
          <n-button quaternary size="small" @click="$router.push('/games')">
            {{ t('home.browseMore') }}
          </n-button>
        </div>

        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <button
            v-for="consoleItem in consoles"
            :key="consoleItem.id"
            type="button"
            class="catalog-chip rounded-2xl px-4 py-4 text-left"
            @click="$router.push(`/games?consoleId=${consoleItem.id}`)"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="text-lg font-bold">{{ consoleItem.name }}</div>
              <div class="chip-arrow">01</div>
            </div>
            <div class="mt-2 text-sm text-[var(--app-text-muted)]">{{ consoleItem.fullName }}</div>
          </button>
        </div>
      </div>

      <div class="surface-card rounded-[24px] p-5 sm:p-6">
        <div class="mb-5">
          <div class="section-kicker">{{ t('home.series') }}</div>
          <h2 class="section-title">{{ sectionTexts.seriesTitle }}</h2>
        </div>

        <div class="flex flex-wrap gap-3">
          <button
            v-for="seriesItem in series"
            :key="seriesItem.id"
            type="button"
            class="series-pill rounded-full px-4 py-3 text-sm font-semibold"
            @click="$router.push(`/games?seriesId=${seriesItem.id}`)"
          >
            {{ seriesItem.name }}
          </button>
        </div>
      </div>
    </section>

    <section v-if="featuredGames.length > 0" class="surface-card rounded-[24px] p-5 sm:p-6">
      <div class="mb-5 flex items-center justify-between gap-4">
        <div>
          <div class="section-kicker">{{ t('home.recommended') }}</div>
          <h2 class="section-title">{{ sectionTexts.recommendedTitle }}</h2>
        </div>
        <n-button quaternary size="small" @click="$router.push('/games')">{{ t('home.browseMore') }}</n-button>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <GameCard v-for="game in featuredGames" :key="`featured-${game.id}`" :game="game" />
      </div>
    </section>

    <section class="grid gap-6 2xl:grid-cols-2">
      <div v-if="latestGames.length > 0" class="surface-card rounded-[24px] p-5 sm:p-6">
        <div class="mb-5 flex items-center justify-between gap-4">
          <div>
            <div class="section-kicker">{{ t('home.latestAdded') }}</div>
            <h2 class="section-title">{{ sectionTexts.latestTitle }}</h2>
          </div>
          <n-button quaternary size="small" @click="$router.push('/games?sort=latest')">
            {{ t('home.browseMore') }}
          </n-button>
        </div>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <GameCard v-for="game in latestGames" :key="`latest-${game.id}`" :game="game" />
        </div>
      </div>

      <div class="space-y-6">
        <section v-if="store.isLoggedIn && favoriteGames.length > 0" class="surface-card rounded-[24px] p-5 sm:p-6">
          <div class="mb-5 flex items-center justify-between gap-4">
            <div>
              <div class="section-kicker">{{ t('home.myFavorites') }}</div>
              <h2 class="section-title">{{ sectionTexts.favoriteTitle }}</h2>
            </div>
            <n-button quaternary size="small" @click="$router.push('/favorites')">{{ t('home.viewAll') }}</n-button>
          </div>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <GameCard v-for="game in favoriteGames.slice(0, 4)" :key="`favorite-${game.id}`" :game="game" />
          </div>
        </section>

        <section
          v-if="store.isLoggedIn && recentHistory.length > 0"
          class="surface-card rounded-[24px] p-5 sm:p-6"
        >
          <div class="mb-5 flex items-center justify-between gap-4">
            <div>
              <div class="section-kicker">{{ t('home.recentlyPlayed') }}</div>
              <h2 class="section-title">{{ sectionTexts.historyTitle }}</h2>
            </div>
            <n-button quaternary size="small" @click="refreshRecentHistory">{{ t('common.refresh') }}</n-button>
          </div>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <GameCard v-for="game in recentHistory.slice(0, 4)" :key="`history-${game.id}`" :game="game" />
          </div>
        </section>

        <section v-if="!store.isLoggedIn" class="surface-card rounded-[24px] p-5 sm:p-6">
          <div class="section-kicker">{{ emptyStateTexts.kicker }}</div>
          <h2 class="section-title mt-2">{{ emptyStateTexts.title }}</h2>
          <p class="mt-3 text-sm leading-7 text-[var(--app-text-muted)]">
            {{ emptyStateTexts.description }}
          </p>
          <div class="mt-5 flex flex-wrap gap-3">
            <n-button type="primary" @click="$router.push('/games')">{{ actionTexts.exploreNow }}</n-button>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NInput } from 'naive-ui'
import { request } from '../api/request'
import { useAppStore } from '../store'
import GameCard from '../components/GameCard.vue'
import { useI18n } from '../i18n'

const router = useRouter()
const store = useAppStore()
const { t, isZh } = useI18n()
const searchKeyword = ref('')
const consoles = ref([])
const series = ref([])
const featuredGames = ref([])
const latestGames = ref([])
const recentHistory = computed(() => store.recentHistory)
const favoriteGames = computed(() => store.favoriteGames)

const copy = computed(() =>
  isZh.value
    ? {
        heroBadge: '专业级在线游戏中台',
        heroTitle: '统一管理游戏、资源与玩家入口，让首页更像真正的产品门户',
        heroDescription:
          '把推荐内容、分类浏览、最近游玩和个人收藏集中到一个更清晰的首页，让用户进入系统后可以更快找到目标游戏。',
        dashboardTitle: '首页概览',
        dashboardTag: 'Live',
        actionTexts: {
          search: '开始搜索',
          browseLibrary: '浏览全部游戏',
          viewFavorites: '查看收藏',
          openAdmin: '进入后台',
          dashboardTitle: '运行状态',
          exploreNow: '立即探索'
        },
        sectionTexts: {
          consoleTitle: '按主机快速进入',
          seriesTitle: '按系列浏览',
          recommendedTitle: '精选推荐',
          latestTitle: '最新上架',
          favoriteTitle: '我的收藏',
          historyTitle: '最近游玩'
        },
        emptyStateTexts: {
          kicker: '个性化入口',
          title: '登录后可查看最近游玩与收藏内容',
          description: '当前首页已经支持按主机、系列和推荐内容快速进入，登录后还会补充个性化内容区。'
        },
        highlights: [
          {
            title: '分类更清晰',
            description: '主机和系列入口拆分展示，降低用户首次进入时的选择成本。'
          },
          {
            title: '内容更聚焦',
            description: '推荐、最新与个性化内容分区展示，首页不再只是简单平铺列表。'
          },
          {
            title: '主题可切换',
            description: '已支持白天与黑夜模式切换，适配不同使用环境。'
          }
        ]
      }
    : {
        heroBadge: 'Professional online game portal',
        heroTitle: 'A clearer entry point for games, content discovery, and player activity',
        heroDescription:
          'Bring recommendations, categories, recently played items, and personal favorites into one front page that feels like a real product portal.',
        dashboardTitle: 'Home overview',
        dashboardTag: 'Live',
        actionTexts: {
          search: 'Search',
          browseLibrary: 'Browse library',
          viewFavorites: 'Favorites',
          openAdmin: 'Open admin',
          dashboardTitle: 'Status',
          exploreNow: 'Explore now'
        },
        sectionTexts: {
          consoleTitle: 'Browse by console',
          seriesTitle: 'Browse by series',
          recommendedTitle: 'Recommended picks',
          latestTitle: 'Latest arrivals',
          favoriteTitle: 'Your favorites',
          historyTitle: 'Recently played'
        },
        emptyStateTexts: {
          kicker: 'Personalized area',
          title: 'Sign in to unlock favorites and recent activity',
          description:
            'The homepage already supports discovery by console, series, and recommendations. Sign in to enrich it with personal content.'
        },
        highlights: [
          {
            title: 'Clearer categories',
            description: 'Console and series entrances are separated for faster first-time navigation.'
          },
          {
            title: 'Sharper focus',
            description: 'Recommendations, latest content, and personal areas are grouped with a stronger hierarchy.'
          },
          {
            title: 'Theme support',
            description: 'Light and dark modes are now available for different work environments.'
          }
        ]
      }
)

const heroBadge = computed(() => copy.value.heroBadge)
const heroTitle = computed(() => copy.value.heroTitle)
const heroDescription = computed(() => copy.value.heroDescription)
const dashboardTitle = computed(() => copy.value.dashboardTitle)
const dashboardTag = computed(() => copy.value.dashboardTag)
const actionTexts = computed(() => copy.value.actionTexts)
const sectionTexts = computed(() => copy.value.sectionTexts)
const emptyStateTexts = computed(() => copy.value.emptyStateTexts)
const heroHighlights = computed(() => copy.value.highlights)
const heroStats = computed(() => [
  {
    label: isZh.value ? '主机分类' : 'Consoles',
    value: consoles.value.length
  },
  {
    label: isZh.value ? '系列分类' : 'Series',
    value: series.value.length
  },
  {
    label: isZh.value ? '推荐内容' : 'Featured',
    value: featuredGames.value.length
  },
  {
    label: isZh.value ? '最新内容' : 'Latest',
    value: latestGames.value.length
  }
])

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push(`/games?keyword=${encodeURIComponent(keyword)}`)
  }
}

const fetchGameSection = async (options = {}) => {
  const params = new URLSearchParams({
    current: '1',
    size: '6',
    sort: options.sort || 'latest'
  })
  if (typeof options.isRecommend === 'number') {
    params.append('isRecommend', String(options.isRecommend))
  }
  const data = await request(`/games/list?${params.toString()}`)
  return Array.isArray(data.records) ? data.records : []
}

const refreshRecentHistory = async () => {
  try {
    await store.fetchRecentHistory()
  } catch (error) {
    console.error('Failed to load recent history', error)
  }
}

const refreshFavorites = async () => {
  try {
    await Promise.all([store.fetchFavoriteGames(6), store.fetchFavoriteIds()])
  } catch (error) {
    console.error('Failed to load favorites', error)
  }
}

onMounted(async () => {
  const [consoleData, seriesData, recommendedData, latestData] = await Promise.all([
    request('/categories/consoles'),
    request('/categories/series'),
    fetchGameSection({ sort: 'recommend', isRecommend: 1 }),
    fetchGameSection({ sort: 'latest' })
  ])

  consoles.value = consoleData
  series.value = seriesData
  featuredGames.value = recommendedData
  latestGames.value = latestData

  if (store.isLoggedIn) {
    await Promise.all([refreshRecentHistory(), refreshFavorites()])
  }
})
</script>

<style scoped>
.hero-panel {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--app-border);
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.28), transparent 28%),
    radial-gradient(circle at left center, rgba(14, 165, 233, 0.18), transparent 32%),
    linear-gradient(135deg, var(--app-surface), var(--app-surface-soft));
  box-shadow: var(--app-shadow-lg);
}

.hero-panel::after {
  content: '';
  position: absolute;
  inset: auto -80px -120px auto;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.14);
  filter: blur(16px);
}

.hero-search {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(12px);
}

.hero-aside {
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(18px);
}

.surface-card {
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-md);
}

.section-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--app-primary-strong);
}

.section-title {
  margin-top: 8px;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 800;
}

.catalog-chip {
  border: 1px solid var(--app-border);
  background: var(--app-surface-soft);
  color: var(--app-text);
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.catalog-chip:hover {
  transform: translateY(-2px);
  border-color: rgba(59, 130, 246, 0.4);
  background: var(--app-surface-hover);
}

.chip-arrow {
  min-width: 38px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  padding: 6px 10px;
  text-align: center;
  font-size: 11px;
  font-weight: 700;
  color: var(--app-primary-strong);
}

.series-pill {
  border: 1px solid var(--app-border);
  background: var(--app-surface-soft);
  color: var(--app-text);
  transition: border-color 0.2s ease, transform 0.2s ease, background-color 0.2s ease;
}

.series-pill:hover {
  transform: translateY(-1px);
  border-color: rgba(59, 130, 246, 0.4);
  background: var(--app-surface-hover);
}

.stat-tile {
  border: 1px solid var(--app-border);
  background: var(--app-surface);
}

.highlight-row {
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.03);
}

.highlight-dot {
  background: linear-gradient(135deg, var(--app-primary), #38bdf8);
  box-shadow: 0 0 0 6px rgba(59, 130, 246, 0.12);
}

html[data-theme='light'] .hero-search,
html[data-theme='light'] .hero-aside,
html[data-theme='light'] .highlight-row {
  background: rgba(255, 255, 255, 0.72);
}

@media (max-width: 640px) {
  .section-title {
    font-size: 24px;
  }
}
</style>
