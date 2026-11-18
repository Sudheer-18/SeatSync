import axiosClient from './axiosClient'

const tripApi = {
  search: async (params) => {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (Array.isArray(value)) value.forEach((item) => query.append(key, item))
      else if (value !== undefined && value !== '') query.set(key, value)
    })
    return (await axiosClient.get(`/trips/search?${query.toString()}`)).data
  },
  getById: async (id) => (await axiosClient.get(`/trips/${id}`)).data,
  getSeats: async (id) => (await axiosClient.get(`/trips/${id}/seats`)).data,
}

export default tripApi
