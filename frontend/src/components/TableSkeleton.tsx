import { Skeleton } from "@/components/ui/skeleton";

export function TableSkeleton({
  columns,
  rows = 5,
}: {
  columns: number;
  rows?: number;
}) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, r) => (
        <div key={r} className="flex gap-4">
          {Array.from({ length: columns }).map((_, c) => (
            <Skeleton key={c} className="h-5 flex-1" />
          ))}
        </div>
      ))}
    </div>
  );
}