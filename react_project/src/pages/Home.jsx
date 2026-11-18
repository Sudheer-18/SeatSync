import { useNavigate } from 'react-router-dom'
import SearchForm from '../components/trips/SearchForm'

const steps = [
  { number: '01', title: 'Search', text: 'Choose your route and travel date.' },
  { number: '02', title: 'Pick seats', text: 'Find the seats that work for you.' },
  { number: '03', title: 'Book', text: 'Add passenger details and confirm.' },
]

export default function Home() {
  const navigate = useNavigate()
  const search = (values) => {
    const params = new URLSearchParams(values)
    navigate(`/trips?${params.toString()}`)
  }

  return (
    <main>
      <section className="bg-slate-950 px-5 py-16 text-white sm:py-24">
        <div className="mx-auto max-w-7xl">
          <p className="font-bold uppercase tracking-[0.2em] text-emerald-400">Make room for the journey</p>
          <h1 className="mt-4 max-w-3xl text-4xl font-black leading-tight sm:text-6xl">Your next trip is closer than you think.</h1>
          <p className="mt-4 max-w-xl text-slate-300">Compare bus trips, choose your seats, and book in a few easy steps.</p>
          <div className="mt-8 text-slate-900"><SearchForm onSubmit={search} /></div>
        </div>
      </section>
      <section className="mx-auto grid max-w-7xl gap-5 px-5 py-12 sm:grid-cols-3">
        {steps.map((step) => (
          <article key={step.number} className="rounded-2xl border border-slate-200 bg-white p-5">
            <p className="text-sm font-black text-emerald-700">{step.number}</p>
            <h2 className="mt-2 text-lg font-bold text-slate-900">{step.title}</h2>
            <p className="mt-1 text-sm text-slate-600">{step.text}</p>
          </article>
        ))}
      </section>
    </main>
  )
}
