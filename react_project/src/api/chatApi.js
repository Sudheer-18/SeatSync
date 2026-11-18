import axiosClient from './axiosClient'

const chatApi = {
  send: async (message) => (
    await axiosClient.post('/chat', { message }, { timeout: 10000 })
  ).data,
  history: async (limit = 50) => (
    await axiosClient.get('/chat/history', { params: { limit }, timeout: 10000 })
  ).data,
}

export default chatApi
