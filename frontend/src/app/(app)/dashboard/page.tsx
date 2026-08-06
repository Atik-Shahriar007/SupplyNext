"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import api from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface DashboardSummary {
  totalInventoryValue: number;
  totalStockUnits: number;
  lowStockItemsCount: number;
  pendingPurchaseOrders: number;
  pendingSalesOrders: number;
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
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Total Inventory Value
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">${summary.totalInventoryValue.toFixed(2)}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Total Stock Units
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{summary.totalStockUnits}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Low Stock Items
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-red-500">{summary.lowStockItemsCount}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Pending Purchase Orders
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{summary.pendingPurchaseOrders}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Pending Sales Orders
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{summary.pendingSalesOrders}</p>
            </CardContent>
          </Card>
        </div>
      ) : (
        <p className="text-red-500">Failed to load dashboard data.</p>
      )}
    </div>
  );
}