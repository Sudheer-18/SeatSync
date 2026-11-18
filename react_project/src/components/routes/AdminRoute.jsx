import { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import useToast from '../../hooks/useToast'
import ProtectedRoute from './ProtectedRoute'

export default function AdminRoute({ children }) {
  const { user, loading } = useAuth()
  const addToast = useToast()

  useEffect(() => {
    if (!loading && user && user.role !== 'ADMIN') addToast('Admin access is required.', 'error')
  }, [addToast, loading, user])

  return (
    <ProtectedRoute>
      {user?.role === 'ADMIN' ? children : <Navigate to="/" replace />}
    </ProtectedRoute>
  )
}
