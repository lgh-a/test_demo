import { useAppStore } from '../store'

export const authDirective = {
    mounted(el, binding) {
        const store = useAppStore()
        const { value } = binding
        if (value && !store.hasPerm(value)) {
            el.parentNode && el.parentNode.removeChild(el)
        }
    }
}
