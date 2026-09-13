export function OrderStatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    PENDING: "bg-amber-100 text-amber-700",
    RECEIVED: "bg-green-100 text-green-700",
    SHIPPED: "bg-green-100 text-green-700",
    COMPLETED: "bg-green-100 text-green-700",
  };

  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
        styles[status] ?? "bg-slate-100 text-slate-600"
      }`}
    >
      {status}
    </span>
  );
}