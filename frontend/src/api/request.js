export const request = async (url, options = {}) => {
    const baseUrl = 'http://localhost:8080/api'
    const token = localStorage.getItem('satoken') // Read token directly from storage

    try {
        const response = await fetch(`${baseUrl}${url}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'satoken': token } : {}),
                ...options.headers,
            }
        })
        const result = await response.json()
        if (result.code !== 200) {
            if (result.code === 500 && result.msg.includes('Not logged in')) {
                // Token expired handling can go here
                localStorage.removeItem('satoken')
                window.location.reload()
            }
            throw new Error(result.msg || 'Request failed')
        }
        return result.data
    } catch (error) {
        console.error('API Error:', error)
        throw error
    }
}
