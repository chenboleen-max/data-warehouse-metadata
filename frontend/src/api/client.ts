/**
 * Axios HTTP client configuration
 */
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000'

// Create axios instance
const client: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
client.interceptors.request.use(
  (config: any) => {
    // Add auth token if exists
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
client.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  (error) => {
    // Handle errors globally
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        // Unauthorized - redirect to login
        localStorage.removeItem('access_token')
        window.location.href = '/login'
      } else if (status === 403) {
        // Forbidden
        console.error('Permission denied:', data.error_message)
      } else if (status >= 500) {
        // Server error
        console.error('Server error:', data.error_message)
      }
    }
    
    return Promise.reject(error)
  }
)

export default client
