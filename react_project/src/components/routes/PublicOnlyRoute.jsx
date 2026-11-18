import { Navigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import Loader from '../common/Loader'

export default function PublicOnlyRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <Loader label="Restoring your session…" />
  if (user) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} replace />
  return children
}
