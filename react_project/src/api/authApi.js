import axiosClient from './axiosClient'

const authApi = {
  register: async (data) => (await axiosClient.post('/auth/register', data)).data,
  login: async (data) => (await axiosClient.post('/auth/login', data)).data,
  me: async () => (await axiosClient.get('/auth/me')).data,
}

export default authApi
