import { defineStore } from 'pinia'
import { request } from '../api/request'

export const useAppStore = defineStore('app', {
    state: () => ({
        historyGames: [],
        recentHistory: [],
        favoriteIds: [],
        favoriteGames: [],
        userInfo: null,
        roles: [],
        permissions: [],
        authReady: false,
        authLoading: false
    }),
    getters: {
        isLoggedIn: (state) => !!state.userInfo,
        hasPerm: (state) => (perm) => state.permissions.includes(perm)
    },
    actions: {
        addHistory(game) {
            if (!this.historyGames.find(g => g.id === game.id)) {
                this.historyGames.unshift(game)
            }
        },
        async fetchUserInfo() {
            try {
                const data = await request('/auth/info')
                this.userInfo = data.user ?? data.userInfo ?? null
                this.roles = Array.isArray(data.roles) ? data.roles : []
                this.permissions = Array.isArray(data.permissions) ? data.permissions : []
                return this.userInfo
            } catch (e) {
                this.logout()
                throw e
            }
        },
        async fetchRecentHistory(limit = 6) {
            if (!this.isLoggedIn) {
                this.recentHistory = []
                return []
            }
            try {
                const data = await request(`/history/games/recent?limit=${limit}`)
                this.recentHistory = Array.isArray(data) ? data : []
                return this.recentHistory
            } catch (e) {
                this.recentHistory = []
                throw e
            }
        },
        async fetchFavoriteIds() {
            if (!this.isLoggedIn) {
                this.favoriteIds = []
                return []
            }
            try {
                const data = await request('/favorites/games/ids')
                this.favoriteIds = Array.isArray(data) ? data : []
                return this.favoriteIds
            } catch (e) {
                this.favoriteIds = []
                throw e
            }
        },
        async fetchFavoriteGames(limit = 20) {
            if (!this.isLoggedIn) {
                this.favoriteGames = []
                return []
            }
            try {
                const data = await request(`/favorites/games?limit=${limit}`)
                this.favoriteGames = Array.isArray(data) ? data : []
                return this.favoriteGames
            } catch (e) {
                this.favoriteGames = []
                throw e
            }
        },
        async toggleFavorite(gameId) {
            if (!this.isLoggedIn) {
                throw new Error('Please log in first')
            }

            const isFavorite = this.favoriteIds.includes(gameId)
            if (isFavorite) {
                await request(`/favorites/games/${gameId}`, { method: 'DELETE' })
                this.favoriteIds = this.favoriteIds.filter((id) => id !== gameId)
                this.favoriteGames = this.favoriteGames.filter((game) => game.id !== gameId)
                return false
            }

            await request(`/favorites/games/${gameId}`, { method: 'POST' })
            if (!this.favoriteIds.includes(gameId)) {
                this.favoriteIds = [gameId, ...this.favoriteIds]
            }
            return true
        },
        async bootstrapAuth(force = false) {
            if (this.authLoading) {
                return
            }
            if (this.authReady && !force) {
                return
            }

            this.authLoading = true
            try {
                if (localStorage.getItem('satoken')) {
                    await this.fetchUserInfo()
                    await this.fetchFavoriteIds()
                } else {
                    this.logout()
                }
            } catch (e) {
                this.logout()
            } finally {
                this.authReady = true
                this.authLoading = false
            }
        },
        logout() {
            this.userInfo = null
            this.roles = []
            this.permissions = []
            this.recentHistory = []
            this.favoriteIds = []
            this.favoriteGames = []
            localStorage.removeItem('satoken')
        }
    }
})
