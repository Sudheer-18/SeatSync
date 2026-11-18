import { ADMIN_TABS } from '../../utils/adminResources'

export default function ResourceTabs({ activeTab, onChange }) {
  return (
    <div role="tablist" aria-label="Manage resources" className="flex gap-2 border-b border-slate-200">
      {ADMIN_TABS.map((tab) => (
        <button key={tab} type="button" role="tab" aria-selected={activeTab === tab} onClick={() => onChange(tab)} className={`px-4 py-3 font-bold capitalize ${activeTab === tab ? 'border-b-2 border-emerald-600 text-emerald-700' : 'text-slate-500'}`}>
          {tab}
        </button>
      ))}
    </div>
  )
}
