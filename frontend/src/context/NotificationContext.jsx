import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuth } from './AuthContext'

const NotificationContext = createContext(null)

export function NotificationProvider({ children }) {
  const { profile } = useAuth()
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const stompClientRef = useRef(null)

  const isLoggedIn = !!profile && profile.id !== 'demo'

  const fetchNotifications = useCallback(async () => {
    if (!isLoggedIn) return
    try {
      const res = await api.get('/notifications')
      const data = res.data
      setNotifications(data)
      setUnreadCount(data.filter(n => !n.read).length)
    } catch {}
  }, [isLoggedIn])

  useEffect(() => {
    fetchNotifications()
    if (!isLoggedIn) return
    const interval = setInterval(fetchNotifications, 30000)
    return () => clearInterval(interval)
  }, [fetchNotifications, isLoggedIn])

  useEffect(() => {
    if (!isLoggedIn || !profile?.id) return
    const token = localStorage.getItem('token')

    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${profile.id}`, (frame) => {
          const notification = JSON.parse(frame.body)
          setNotifications(prev => [notification, ...prev])
          setUnreadCount(prev => prev + 1)
        })
      },
      onStompError: (frame) => console.error('Notification WS error:', frame),
    })

    client.activate()
    stompClientRef.current = client

    return () => {
      client.deactivate()
      stompClientRef.current = null
    }
  }, [isLoggedIn, profile?.id])

  const markAsRead = async (id) => {
    try {
      await api.patch(`/notifications/${id}/read`)
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n))
      setUnreadCount(prev => Math.max(0, prev - 1))
    } catch {}
  }

  const markAllAsRead = async () => {
    if (unreadCount === 0) return
    try {
      await api.patch('/notifications/read-all')
      setNotifications(prev => prev.map(n => ({ ...n, read: true })))
      setUnreadCount(0)
    } catch {}
  }

  const clearAll = async () => {
    try {
      await api.delete('/notifications')
      setNotifications([])
      setUnreadCount(0)
    } catch {}
  }

  return (
    <NotificationContext.Provider value={{ notifications, unreadCount, markAsRead, markAllAsRead, clearAll, refresh: fetchNotifications }}>
      {children}
    </NotificationContext.Provider>
  )
}

export const useNotifications = () => useContext(NotificationContext)
