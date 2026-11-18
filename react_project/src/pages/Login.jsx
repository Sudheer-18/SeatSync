import AuthForm from '../components/auth/AuthForm'

export default function Login() {
  return (
    <main className="px-4 py-12 sm:py-16">
      <AuthForm mode="login" />
    </main>
  )
}
