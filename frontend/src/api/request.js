export const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')
export const backendOrigin = new URL(apiBaseUrl, window.location.origin).origin

export const request = async (url, options = {}) => {
    const token = localStorage.getItem('satoken')

    try {
        const response = await fetch(`${apiBaseUrl}${url}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'satoken': token } : {}),
                ...options.headers,
            }
        })
        const contentType = response.headers.get('content-type') || ''
        const result = contentType.includes('application/json')
            ? await response.json()
            : null
        const errorMessage = result?.msg || `Request failed with status ${response.status}`

        if (response.status === 401) {
            localStorage.removeItem('satoken')
            if (window.location.pathname !== '/') {
                window.location.href = '/'
            }
        }

        if (!response.ok) {
            throw new Error(errorMessage)
        }

        if (result && result.code !== 200) {
            throw new Error(errorMessage)
        }

        return result?.data
    } catch (error) {
        console.error('API Error:', error)
        throw error
    }
}
