"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import api from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface DashboardSummary {
  totalInventoryValue: number;
  totalStockUnits: number;
  lowStockThreshold: number;
  lowStockItemsCount: number;
  pendingPurchaseOrders: number;
  pendingSalesOrders: number;
  pendingTransfers: number;
  totalProducts: number;
  totalWarehouses: number;
  totalSuppliers: number;
  deadStockItemsCount: number;
  averageSupplierOnTimeRate: number | null;
}

function StatCard({
  label,
  value,
  emphasis,
}: {
  label: string;
  value: string | number;
  emphasis?: "warning" | "default";
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {label}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p
          className={
            emphasis === "warning"
              ? "text-2xl font-bold text-red-500"
              : "text-2xl font-bold"
          }
        >
          {value}
        </p>
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const { role } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    api
      .get("/api/dashboard/summary")
      .then((res) => setSummary(res.data))
      .catch((err) => console.error("Failed to load dashboard:", err))
      .finally(() => setLoadingData(false));
  }, []);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-sm text-muted-foreground">Logged in as role: {role}</p>
      </div>

      {loadingData ? (
        <p>Loading dashboard data...</p>
      ) : summary ? (
        <div className="space-y-6">
          <div>
            <h2 className="mb-3 text-sm font-semibold text-muted-foreground">
              Inventory & Orders
            </h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard
                label="Total Inventory Value"
                value={`$${summary.totalInventoryValue.toFixed(2)}`}
              />
              <StatCard label="Total Stock Units" value={summary.totalStockUnits} />
              <StatCard
                label={`Low Stock Items (< ${summary.lowStockThreshold})`}
                value={summary.lowStockItemsCount}
                emphasis={summary.lowStockItemsCount > 0 ? "warning" : "default"}
              />
              <StatCard label="Pending Purchase Orders" value={summary.pendingPurchaseOrders} />
              <StatCard label="Pending Sales Orders" value={summary.pendingSalesOrders} />
              <StatCard label="Pending Transfers" value={summary.pendingTransfers} />
            </div>
          </div>

          <div>
            <h2 className="mb-3 text-sm font-semibold text-muted-foreground">
              Catalog & Supplier Health
            </h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard label="Total Products" value={summary.totalProducts} />
              <StatCard label="Total Warehouses" value={summary.totalWarehouses} />
              <StatCard label="Total Suppliers" value={summary.totalSuppliers} />
              <StatCard
                label="Dead Stock Items"
                value={summary.deadStockItemsCount}
                emphasis={summary.deadStockItemsCount > 0 ? "warning" : "default"}
              />
              <StatCard
                label="Avg. Supplier On-Time Rate"
                value={
                  summary.averageSupplierOnTimeRate != null
                    ? `${summary.averageSupplierOnTimeRate.toFixed(1)}%`
                    : "No data yet"
                }
              />
            </div>
          </div>
        </div>
      ) : (
        <p className="text-red-500">Failed to load dashboard data.</p>
      )}
    </div>
  );
}