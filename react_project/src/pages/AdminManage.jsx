import { useEffect, useState } from 'react'
import adminApi from '../api/adminApi'
import AdminFormFields from '../components/admin/AdminFormFields'
import ResourceTabs from '../components/admin/ResourceTabs'
import ConfirmModal from '../components/common/ConfirmModal'
import DataTable from '../components/common/DataTable'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import FormModal from '../components/common/FormModal'
import Loader from '../components/common/Loader'
import Pagination from '../components/common/Pagination'
import useToast from '../hooks/useToast'
import { adminPayload, adminRecordLabel, adminRowValues, blankAdminForm, validateAdminForm } from '../utils/adminForms'
import { ADMIN_PAGE_SIZE, ADMIN_RESOURCES } from '../utils/adminResources'
import adminTableColumns from '../utils/adminTableColumns'

export default function AdminManage() {
  const [tab, setTab] = useState('buses')
  const [rows, setRows] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [open, setOpen] = useState(false)
  const [record, setRecord] = useState(null)
  const [values, setValues] = useState(blankAdminForm('buses'))
  const [fieldErrors, setFieldErrors] = useState({})
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(null)
  const [deleteError, setDeleteError] = useState('')
  const [savingDelete, setSavingDelete] = useState(false)
  const [options, setOptions] = useState({ buses: [], routes: [] })
  const [optionsError, setOptionsError] = useState('')
  const addToast = useToast()
  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    ADMIN_RESOURCES[tab].list({ page, size: ADMIN_PAGE_SIZE })
      .then((result) => {
        if (!Array.isArray(result.content)) throw new Error('Admin list response was invalid.')
        if (!active) return
        if (result.totalPages > 0 && page >= result.totalPages) {
          setPage(result.totalPages - 1)
          return
        }
        setRows(result.content)
        setTotalPages(result.totalPages)
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [tab, page, attempt])

  useEffect(() => {
    if (tab !== 'trips') return undefined
    let active = true
    setOptionsError('')
    Promise.all([
      adminApi.listBuses({ page: 0, size: 50 }),
      adminApi.listRoutes({ page: 0, size: 50 }),
    ])
      .then(([buses, routes]) => {
        if (!Array.isArray(buses.content) || !Array.isArray(routes.content)) throw new Error('Trip form options were invalid.')
        if (active) setOptions({ buses: buses.content, routes: routes.content })
      })
      .catch((requestError) => { if (active) setOptionsError(requestError.message) })
    return () => { active = false }
  }, [tab])

  const changeTab = (nextTab) => {
    setTab(nextTab)
    setPage(0)
    setError('')
  }
  const startAdd = () => {
    setRecord(null)
    setValues(blankAdminForm(tab))
    setFieldErrors({})
    setSaveError('')
    setOpen(true)
  }

  const startEdit = (row) => {
    setRecord(row)
    setValues(adminRowValues(tab, row))
    setFieldErrors({})
    setSaveError('')
    setOpen(true)
  }

  const updateValue = (key, value) => setValues((current) => ({ ...current, [key]: value }))
  const submitForm = async (event) => {
    event.preventDefault()
    const errors = validateAdminForm(tab, values, Boolean(record))
    setFieldErrors(errors)
    if (Object.keys(errors).length) return
    setSaving(true)
    setSaveError('')
    try {
      const payload = adminPayload(tab, values, Boolean(record))
      if (record) await ADMIN_RESOURCES[tab].update(record.id, payload)
      else await ADMIN_RESOURCES[tab].create(payload)
      addToast(`${tab.slice(0, -1)} ${record ? 'updated' : 'created'} successfully.`)
      setOpen(false)
      setAttempt((value) => value + 1)
    } catch (requestError) {
      setSaveError(requestError.message || `Unable to save ${tab.slice(0, -1)}.`)
    } finally {
      setSaving(false)
    }
  }

  const removeRecord = async () => {
    setSavingDelete(true)
    setDeleteError('')
    try {
      await ADMIN_RESOURCES[tab].remove(deleting.id)
      addToast(`${tab.slice(0, -1)} deleted successfully.`)
      setDeleting(null)
      setAttempt((value) => value + 1)
    } catch (requestError) {
      setDeleteError(requestError.message || `Unable to delete ${tab.slice(0, -1)}.`)
    } finally {
      setSavingDelete(false)
    }
  }

  return (
    <main className="mx-auto max-w-7xl space-y-5 px-5 py-10">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div><p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Administration</p><h1 className="mt-1 text-3xl font-black">Manage operations</h1></div>
        <button type="button" onClick={startAdd} className="rounded-xl bg-emerald-600 px-4 py-3 font-bold text-white">Add {tab.slice(0, -1)}</button>
      </header>
      <ResourceTabs activeTab={tab} onChange={changeTab} />
      {optionsError && tab === 'trips' && <ErrorBanner message={optionsError} />}
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {loading ? <Loader label={`Loading ${tab}…`} /> : rows.length
        ? <DataTable columns={adminTableColumns(tab)} rows={rows} onEdit={startEdit} onDelete={(row) => { setDeleting(row); setDeleteError('') }} />
        : !error && <EmptyState title={`No ${tab} found`} message={`Add a ${tab.slice(0, -1)} to get started.`} />}
      {!loading && !error && <Pagination page={page} totalPages={totalPages} onChange={setPage} />}
      <FormModal open={open} title={`${record ? 'Edit' : 'Add'} ${tab.slice(0, -1)}`} loading={saving} error={saveError} onClose={() => setOpen(false)} onSubmit={submitForm}>
        {(firstField) => <AdminFormFields tab={tab} values={values} errors={fieldErrors} onChange={updateValue} editing={Boolean(record)} options={options} firstField={firstField} />}
      </FormModal>
      <ConfirmModal open={Boolean(deleting)} title={`Delete ${tab.slice(0, -1)}?`} message={`Delete ${adminRecordLabel(tab, deleting)}? This action cannot be undone.`} loading={savingDelete} error={deleteError} onClose={() => setDeleting(null)} onConfirm={removeRecord} />
    </main>
  )
}
