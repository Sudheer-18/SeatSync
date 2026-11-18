import axiosClient from './axiosClient'

const holdApi = {
  hold: async (tripId, seatIds) => (
    await axiosClient.post(`/trips/${tripId}/hold`, { seatIds })
  ).data,
  release: async (tripId, seatIds) => (
    await axiosClient.delete(`/trips/${tripId}/hold`, {
      data: seatIds?.length ? { seatIds } : undefined,
    })
  ),
}

export default holdApi
