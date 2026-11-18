import axiosClient from './axiosClient'

const adminApi = {
  createBus: async (data) => (await axiosClient.post('/admin/buses', data)).data,
  listBuses: async (params) => (await axiosClient.get('/admin/buses', { params })).data,
  updateBus: async (id, data) => (await axiosClient.put(`/admin/buses/${id}`, data)).data,
  deleteBus: async (id) => axiosClient.delete(`/admin/buses/${id}`),
  createRoute: async (data) => (await axiosClient.post('/admin/routes', data)).data,
  listRoutes: async (params) => (await axiosClient.get('/admin/routes', { params })).data,
  updateRoute: async (id, data) => (await axiosClient.put(`/admin/routes/${id}`, data)).data,
  deleteRoute: async (id) => axiosClient.delete(`/admin/routes/${id}`),
  createTrip: async (data) => (await axiosClient.post('/admin/trips', data)).data,
  listTrips: async (params) => (await axiosClient.get('/admin/trips', { params })).data,
  updateTrip: async (id, data) => (await axiosClient.put(`/admin/trips/${id}`, data)).data,
  deleteTrip: async (id) => axiosClient.delete(`/admin/trips/${id}`),
  listBookings: async (params) => (await axiosClient.get('/admin/bookings', { params })).data,
  verifyPayment: async (id) => axiosClient.post(`/admin/bookings/${id}/verify-payment`),
  rejectPayment: async (id) => axiosClient.post(`/admin/bookings/${id}/reject-payment`),
  stats: async (params) => (await axiosClient.get('/admin/stats', { params })).data,
}

export default adminApi
