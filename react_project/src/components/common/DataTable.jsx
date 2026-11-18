export default function DataTable({ columns, rows, onEdit, onDelete }) {
  const actionsEnabled = onEdit || onDelete
  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white">
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
        <caption className="sr-only">Management records</caption>
        <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-600">
          <tr>
            {columns.map((column) => <th key={column.key} scope="col" className="px-4 py-3">{column.label}</th>)}
            {actionsEnabled && <th scope="col" className="px-4 py-3">Actions</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {rows.map((row) => (
            <tr key={row.id || row.bookingId} className="text-slate-700">
              {columns.map((column) => (
                <td key={column.key} className="whitespace-nowrap px-4 py-3">
                  {column.render ? column.render(row) : row[column.key]}
                </td>
              ))}
              {actionsEnabled && (
                <td className="whitespace-nowrap px-4 py-3">
                  {onEdit && <button type="button" className="mr-3 font-bold text-emerald-700" onClick={() => onEdit(row)}>Edit</button>}
                  {onDelete && <button type="button" className="font-bold text-red-700" onClick={() => onDelete(row)}>Delete</button>}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
