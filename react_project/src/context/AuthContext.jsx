import { createContext, useEffect, useRef, useState } from 'react'
import authApi from '../api/authApi'
import { TOKEN_STORAGE_KEY } from '../utils/constants'
import useToast from '../hooks/useToast'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const initialToken = useRef(localStorage.getItem(TOKEN_STORAGE_KEY))
  const [token, setToken] = useState(initialToken.current)
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(Boolean(initialToken.current))
  const addToast = useToast()

  useEffect(() => {
    if (!initialToken.current) return
    let active = true

    authApi.me()
      .then((profile) => {
        if (active) setUser(profile)
      })
      .catch((error) => {
        if (!active) return
        if (error.status === 401) {
          localStorage.removeItem(TOKEN_STORAGE_KEY)
          setToken(null)
        } else {
          addToast(error.message, 'error')
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [addToast])

  const login = async (credentials) => {
    const result = await authApi.login(credentials)
    localStorage.setItem(TOKEN_STORAGE_KEY, result.token)
    setToken(result.token)
    setUser(result.user)
    return result.user
  }

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
