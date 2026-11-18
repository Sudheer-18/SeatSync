import { useState } from 'react'
import { Link } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'

export default function Navbar({ showAssistant, assistantOpen, onToggleAssistant }) {
  const { user, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const closeMenu = () => setMenuOpen(false)

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4">
        <Link to="/" className="text-xl font-black tracking-tight text-slate-900" onClick={closeMenu}>
          Seat<span className="text-emerald-600">Sync</span>
        </Link>
        <nav aria-label="Primary navigation" className={`${menuOpen ? 'flex' : 'hidden'} absolute left-0 right-0 top-[65px] z-20 flex-col gap-4 border-b border-slate-200 bg-white px-5 py-4 md:static md:order-2 md:ml-auto md:mr-4 md:flex md:flex-row md:items-center md:border-0 md:p-0`}>
          {user ? (
            <>
              <span className="text-sm text-slate-600">Hi, {user.name}</span>
              {user.role === 'ADMIN' ? (
                <>
                  <Link to="/admin" className="text-sm font-semibold text-slate-700" onClick={closeMenu}>Dashboard</Link>
                  <Link to="/admin/manage" className="text-sm font-semibold text-slate-700" onClick={closeMenu}>Manage</Link>
                  <Link to="/admin/bookings" className="text-sm font-semibold text-slate-700" onClick={closeMenu}>Bookings</Link>
                </>
              ) : (
                <Link to="/my-bookings" className="text-sm font-semibold text-slate-700" onClick={closeMenu}>My bookings</Link>
              )}
              <button type="button" className="text-left text-sm font-semibold text-slate-700" onClick={() => { logout(); closeMenu() }}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm font-semibold text-slate-700" onClick={closeMenu}>Sign in</Link>
              <Link to="/register" className="rounded-lg bg-emerald-600 px-4 py-2 text-center text-sm font-semibold text-white" onClick={closeMenu}>
                Create account
              </Link>
            </>
          )}
        </nav>
        <div className="ml-auto flex items-center gap-2 md:order-3 md:ml-0">
          {showAssistant && (
            <button
              type="button"
              onClick={onToggleAssistant}
              aria-expanded={assistantOpen}
              aria-label={assistantOpen ? 'Close AI assistant' : 'Open AI assistant'}
              className="rounded-lg bg-emerald-600 px-3 py-2 text-sm font-semibold text-white hover:bg-emerald-700"
            >
              {assistantOpen ? 'Close AI' : 'AI Assistant'}
            </button>
          )}
          <button
            type="button"
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold md:hidden"
            aria-expanded={menuOpen}
            aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            onClick={() => setMenuOpen((open) => !open)}
          >
            Menu
          </button>
        </div>
      </div>
    </header>
  )
}
