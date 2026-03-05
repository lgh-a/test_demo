import { defineStore } from 'pinia'
import { request } from '../api/request'

export const useAppStore = defineStore('app', {
    state: () => ({
        historyGames: [],
        userInfo: null,
        roles: [],
        permissions: []
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
                this.userInfo = data.user
                this.roles = data.roles
                this.permissions = data.permissions
            } catch (e) {
                this.logout()
            }
        },
        logout() {
            this.userInfo = null
            this.roles = []
            this.permissions = []
            localStorage.removeItem('satoken')
        }
    }
})
