"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { EOQResult } from "@/types/analytics";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StatusBadge } from "@/components/analytics/StatusBadge";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

function EOQTab() {
  const [data, setData] = useState<EOQResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<EOQResult[]>("/api/analytics/eoq")
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load EOQ data:", err))
      .finally(() => setLoading(false));
  }, []);

  const chartData = data
    .filter((r) => r.status === "OK" && r.eoq != null)
    .map((r) => ({ name: r.sku, eoq: r.eoq }));

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Economic Order Quantity by Product</CardTitle>
        </CardHeader>
        <CardContent>
          {chartData.length === 0 ? (
            <p className="text-muted-foreground">
              No products have complete data yet — set cost fields and generate some
              shipped sales history to see EOQ values here.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="eoq" fill="#2563eb" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Products</CardTitle>
        </CardHeader>
        <CardContent>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-muted-foreground">
                <th className="pb-2">SKU</th>
                <th className="pb-2">Name</th>
                <th className="pb-2">Status</th>
                <th className="pb-2">Annual Demand</th>
                <th className="pb-2">EOQ</th>
                <th className="pb-2">Orders/Year</th>
              </tr>
            </thead>
            <tbody>
              {data.map((r) => (
                <tr key={r.productId} className="border-b last:border-0">
                  <td className="py-2">{r.sku}</td>
                  <td className="py-2">{r.productName}</td>
                  <td className="py-2">
                    <StatusBadge status={r.status} />
                    {r.note && (
                      <p className="mt-1 text-xs text-muted-foreground">{r.note}</p>
                    )}
                  </td>
                  <td className="py-2">{r.annualDemand ?? "—"}</td>
                  <td className="py-2">{r.eoq ?? "—"}</td>
                  <td className="py-2">{r.recommendedOrdersPerYear ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}

export default function AnalyticsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Analytics</h1>
        <p className="text-sm text-muted-foreground">
          Inventory intelligence: EOQ, ABC classification, safety stock, reorder
          points, dead stock, and supplier performance.
        </p>
      </div>

      <Tabs defaultValue="eoq">
        <TabsList>
          <TabsTrigger value="eoq">EOQ</TabsTrigger>
          <TabsTrigger value="abc">ABC Analysis</TabsTrigger>
          <TabsTrigger value="safety-stock">Safety Stock</TabsTrigger>
          <TabsTrigger value="reorder-point">Reorder Point</TabsTrigger>
          <TabsTrigger value="dead-stock">Dead Stock</TabsTrigger>
          <TabsTrigger value="supplier-performance">Suppliers</TabsTrigger>
        </TabsList>

        <TabsContent value="eoq">
          <EOQTab />
        </TabsContent>
        <TabsContent value="abc">
          <p className="text-muted-foreground">Coming next.</p>
        </TabsContent>
        <TabsContent value="safety-stock">
          <p className="text-muted-foreground">Coming next.</p>
        </TabsContent>
        <TabsContent value="reorder-point">
          <p className="text-muted-foreground">Coming next.</p>
        </TabsContent>
        <TabsContent value="dead-stock">
          <p className="text-muted-foreground">Coming next.</p>
        </TabsContent>
        <TabsContent value="supplier-performance">
          <p className="text-muted-foreground">Coming next.</p>
        </TabsContent>
      </Tabs>
    </div>
  );
}