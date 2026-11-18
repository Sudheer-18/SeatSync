import axiosClient from './axiosClient'

const bookingApi = {
  create: async (data, idempotencyKey) => (
    await axiosClient.post('/bookings', data, {
      headers: { 'Idempotency-Key': idempotencyKey },
    })
  ).data,
  mine: async (params) => (await axiosClient.get('/bookings/me', { params })).data,
  getById: async (id) => (await axiosClient.get(`/bookings/${id}`)).data,
  paymentInstructions: async (id) => (await axiosClient.get(`/bookings/${id}/payment-instructions`)).data,
  submitPaymentReference: async (id, paymentReference) => (
    axiosClient.put(`/bookings/${id}/payment-reference`, { paymentReference })
  ),
  cancel: async (id) => (await axiosClient.delete(`/bookings/${id}`)).data,
}

export default bookingApi
