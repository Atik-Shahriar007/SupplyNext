export function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    OK: "bg-green-100 text-green-700",
    MISSING_COST_DATA: "bg-amber-100 text-amber-700",
    MISSING_LEAD_TIME: "bg-amber-100 text-amber-700",
    INSUFFICIENT_DATA: "bg-slate-100 text-slate-600",
    NO_RECEIVED_ORDERS: "bg-slate-100 text-slate-600",
  };

  const labels: Record<string, string> = {
    OK: "OK",
    MISSING_COST_DATA: "Missing cost data",
    MISSING_LEAD_TIME: "Missing lead time",
    INSUFFICIENT_DATA: "Insufficient data",
    NO_RECEIVED_ORDERS: "No received orders",
  };

  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
        styles[status] ?? "bg-slate-100 text-slate-600"
      }`}
    >
      {labels[status] ?? status}
    </span>
  );
}