import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import GameList from '../views/GameList.vue'
import GamePlay from '../views/GamePlay.vue'
import Admin from '../views/Admin.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: Home
        },
        {
            path: '/games',
            name: 'GameList',
            component: GameList
        },
        {
            path: '/play/:id',
            name: 'GamePlay',
            component: GamePlay
        },
        {
            path: '/admin',
            name: 'Admin',
            component: Admin
        }
    ]
})

export default router
