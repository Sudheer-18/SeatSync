import { useEffect, useState } from 'react'
import { Link, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import Login from './pages/Login'
import Register from './pages/Register'
import Home from './pages/Home'
import SearchResults from './pages/SearchResults'
import SeatSelection from './pages/SeatSelection'
import Checkout from './pages/Checkout'
import BookingSuccess from './pages/BookingSuccess'
import MyBookings from './pages/MyBookings'
import AdminDashboard from './pages/AdminDashboard'
import AdminManage from './pages/AdminManage'
import AdminBookings from './pages/AdminBookings'
import Navbar from './components/layout/Navbar'
import PublicOnlyRoute from './components/routes/PublicOnlyRoute'
import ProtectedRoute from './components/routes/ProtectedRoute'
import AdminRoute from './components/routes/AdminRoute'
import ChatWidget from './components/chat/ChatWidget'
import useToast from './hooks/useToast'

function NotFound() {
  return (
    <main className="mx-auto max-w-7xl px-5 py-20">
      <h1 className="text-3xl font-black text-slate-900">Page not found</h1>
      <Link to="/" className="mt-4 inline-block font-bold text-emerald-700">Return home</Link>
    </main>
  )
}

const App = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const addToast = useToast()
  const [assistantOpen, setAssistantOpen] = useState(false)
  const showChat = !location.pathname.startsWith('/admin')
    && !['/login', '/register'].includes(location.pathname)

  useEffect(() => {
    if (!showChat) setAssistantOpen(false)
  }, [showChat])

  useEffect(() => {
    const handleForbidden = () => {
      addToast('You do not have permission to access that page.', 'error')
      navigate('/', { replace: true })
    }
    const handleRateLimit = () => addToast('Please wait before trying again.', 'error')
    window.addEventListener('seatsync:forbidden', handleForbidden)
    window.addEventListener('seatsync:rate-limited', handleRateLimit)
    return () => {
      window.removeEventListener('seatsync:forbidden', handleForbidden)
      window.removeEventListener('seatsync:rate-limited', handleRateLimit)
    }
  }, [addToast, navigate])

  return (
    <>
      <Navbar
        showAssistant={showChat}
        assistantOpen={assistantOpen}
        onToggleAssistant={() => setAssistantOpen((open) => !open)}
      />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/trips" element={<SearchResults />} />
        <Route path="/trips/:tripId/seats" element={<SeatSelection />} />
        <Route path="/checkout/:tripId" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
        <Route path="/booking-success/:bookingId" element={<ProtectedRoute><BookingSuccess /></ProtectedRoute>} />
        <Route path="/my-bookings" element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />
        <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/manage" element={<AdminRoute><AdminManage /></AdminRoute>} />
        <Route path="/admin/bookings" element={<AdminRoute><AdminBookings /></AdminRoute>} />
        <Route path="/login" element={<PublicOnlyRoute><Login /></PublicOnlyRoute>} />
        <Route path="/register" element={<PublicOnlyRoute><Register /></PublicOnlyRoute>} />
        <Route path="*" element={<NotFound />} />
      </Routes>
      {showChat && <ChatWidget open={assistantOpen} setOpen={setAssistantOpen} />}
    </>
  )
}

export default App