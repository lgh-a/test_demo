import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from '../store'
import { pinia } from '../store/pinia'

const Home = () => import('../views/Home.vue')
const GameList = () => import('../views/GameList.vue')
const GamePlay = () => import('../views/GamePlay.vue')
const Favorites = () => import('../views/Favorites.vue')
const Admin = () => import('../views/Admin.vue')
const AdminUsers = () => import('../views/AdminUsers.vue')
const AdminRoles = () => import('../views/AdminRoles.vue')
const AdminMenus = () => import('../views/AdminMenus.vue')
const AdminRateLimit = () => import('../views/AdminRateLimit.vue')
const Forbidden = () => import('../views/Forbidden.vue')
const NotFound = () => import('../views/NotFound.vue')

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
            component: GamePlay,
            meta: {
                requiresAuth: true,
                requiredPermissions: ['game:play']
            }
        },
        {
            path: '/favorites',
            name: 'Favorites',
            component: Favorites,
            meta: {
                requiresAuth: true
            }
        },
        {
            path: '/admin',
            name: 'Admin',
            component: Admin,
            meta: {
                requiresAuth: true,
                requiredPermissions: [
                    'sys:user:list',
                    'sys:role:list',
                    'sys:menu:list',
                    'rate-limit:rule:list'
                ]
            },
            children: [
                {
                    path: 'users',
                    name: 'AdminUsers',
                    component: AdminUsers,
                    meta: {
                        requiresAuth: true,
                        requiredPermissions: ['sys:user:list']
                    }
                },
                {
                    path: 'roles',
                    name: 'AdminRoles',
                    component: AdminRoles,
                    meta: {
                        requiresAuth: true,
                        requiredPermissions: ['sys:role:list']
                    }
                },
                {
                    path: 'menus',
                    name: 'AdminMenus',
                    component: AdminMenus,
                    meta: {
                        requiresAuth: true,
                        requiredPermissions: ['sys:menu:list']
                    }
                },
                {
                    path: 'rate-limit',
                    name: 'AdminRateLimit',
                    component: AdminRateLimit,
                    meta: {
                        requiresAuth: true,
                        requiredPermissions: ['rate-limit:rule:list']
                    }
                }
            ]
        },
        {
            path: '/403',
            name: 'Forbidden',
            component: Forbidden
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'NotFound',
            component: NotFound
        }
    ]
})

router.beforeEach(async (to) => {
    const store = useAppStore(pinia)
    await store.bootstrapAuth()

    if (to.path === '/admin') {
        const adminTargets = [
            { path: '/admin/users', permission: 'sys:user:list' },
            { path: '/admin/roles', permission: 'sys:role:list' },
            { path: '/admin/menus', permission: 'sys:menu:list' },
            { path: '/admin/rate-limit', permission: 'rate-limit:rule:list' }
        ]
        const firstAllowed = adminTargets.find((item) => store.hasPerm(item.permission))
        if (firstAllowed) {
            return firstAllowed.path
        }
    }

    if (!to.meta.requiresAuth) {
        return true
    }

    if (!store.isLoggedIn) {
        return {
            path: '/',
            query: {
                login: '1',
                redirect: to.fullPath
            }
        }
    }

    const requiredPermissions = Array.isArray(to.meta.requiredPermissions) ? to.meta.requiredPermissions : []
    if (requiredPermissions.length > 0 && !requiredPermissions.some((permission) => store.hasPerm(permission))) {
        return {
            path: '/403',
            query: {
                from: to.fullPath
            }
        }
    }

    return true
})

export default router
