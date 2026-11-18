import useCountdown from '../../hooks/useCountdown'

export default function CountdownTimer({ expiresAt, onExpire }) {
  const seconds = useCountdown(expiresAt, onExpire)
  const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
  const remainder = String(seconds % 60).padStart(2, '0')

  return (
    <p className="font-bold tabular-nums text-amber-900" role="status" aria-live="polite">
      Hold expires in {minutes}:{remainder}
    </p>
  )
}
