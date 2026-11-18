import adminApi from '../api/adminApi'

export const ADMIN_PAGE_SIZE = 10
export const ADMIN_TABS = ['buses', 'routes', 'trips']
export const ADMIN_RESOURCES = {
  buses: { list: adminApi.listBuses, create: adminApi.createBus, update: adminApi.updateBus, remove: adminApi.deleteBus },
  routes: { list: adminApi.listRoutes, create: adminApi.createRoute, update: adminApi.updateRoute, remove: adminApi.deleteRoute },
  trips: { list: adminApi.listTrips, create: adminApi.createTrip, update: adminApi.updateTrip, remove: adminApi.deleteTrip },
}
