import api from './axios'

export const getNotifications  = ()           => api.get('/notifications')
export const getUnreadCount    = ()           => api.get('/notifications/unread-count')
export const markAsRead        = (id)         => api.patch(`/notifications/${id}/read`)
export const markAllAsRead     = ()           => api.patch('/notifications/read-all')
