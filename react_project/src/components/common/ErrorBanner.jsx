export default function ErrorBanner({ message, onRetry }) {
  return (
    <div className="flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-900 sm:flex-row sm:items-center sm:justify-between" role="alert">
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="font-bold underline" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  )
}
