import axios from 'axios'
import { API_URL, TOKEN_STORAGE_KEY } from '../utils/constants'

const axiosClient = axios.create({ baseURL: API_URL })

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status || 0
    const isLoginRequest = error.config?.url?.endsWith('/auth/login')

    if (status === 401 && !isLoginRequest) {
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      window.location.assign('/login')
    }
    if (status === 403) window.dispatchEvent(new Event('seatsync:forbidden'))
    if (status === 429) window.dispatchEvent(new Event('seatsync:rate-limited'))

    return Promise.reject({
      status,
      message: error.response?.data?.message || 'Unable to reach the server. Please try again.',
    })
  },
)

export default axiosClient
