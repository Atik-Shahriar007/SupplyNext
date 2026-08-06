"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import api from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface DashboardSummary {
  totalInventoryValue: number;
  totalStockUnits: number;
  lowStockItemsCount: number;
  pendingPurchaseOrders: number;
  pendingSalesOrders: number;
}

export default function DashboardPage() {
  const { token, role, logout, isLoading } = useAuth();
  const router = useRouter();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loadingData, setLoadingData] = useState(true);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!isLoading && !token) {
      router.push("/login");
    }
  }, [isLoading, token, router]);

  // Fetch dashboard data once we know we're authenticated
  useEffect(() => {
    if (token) {
      api
        .get("/api/dashboard/summary")
        .then((res) => setSummary(res.data))
        .catch((err) => console.error("Failed to load dashboard:", err))
        .finally(() => setLoadingData(false));
    }
  }, [token]);

  if (isLoading || !token) {
    return <div className="flex min-h-screen items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-muted/40 p-8">
      <div className="mx-auto max-w-5xl">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">SupplyNext Dashboard</h1>
            <p className="text-sm text-muted-foreground">Logged in as role: {role}</p>
          </div>
          <Button variant="outline" onClick={logout}>
            Logout
          </Button>
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
    </div>
  );
}