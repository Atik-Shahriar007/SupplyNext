"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import {
  EOQResult,
  ABCResult,
  SafetyStockResult,
  ReorderPointResult,
  DeadStockResult,
  SupplierPerformanceResult,
} from "@/types/analytics";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field, FieldLabel } from "@/components/ui/field";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/analytics/StatusBadge";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";

const SERVICE_LEVELS = [
  { value: "0.9", label: "90%" },
  { value: "0.95", label: "95%" },
  { value: "0.975", label: "97.5%" },
  { value: "0.99", label: "99%" },
  { value: "0.999", label: "99.9%" },
];

const TIER_COLORS: Record<string, string> = {
  A: "#16a34a",
  B: "#f59e0b",
  C: "#94a3b8",
};

/* ---------------------------- EOQ ---------------------------- */

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

/* ------------------------- ABC Analysis ------------------------- */

function ABCTab() {
  const [data, setData] = useState<ABCResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<ABCResult[]>("/api/analytics/abc")
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load ABC data:", err))
      .finally(() => setLoading(false));
  }, []);

  const tierCounts = ["A", "B", "C"]
    .map((tier) => ({
      name: `Tier ${tier}`,
      value: data.filter((r) => r.tier === tier).length,
      tier,
    }))
    .filter((t) => t.value > 0);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Product Distribution by Tier</CardTitle>
        </CardHeader>
        <CardContent>
          {tierCounts.length === 0 ? (
            <p className="text-muted-foreground">
              No products have unit cost set yet — add cost data on the Products
              page to see ABC classification here.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={tierCounts}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={90}
                  label
                >
                  {tierCounts.map((entry) => (
                    <Cell key={entry.tier} fill={TIER_COLORS[entry.tier]} />
                  ))}
                </Pie>
                <Legend />
              </PieChart>
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
                <th className="pb-2">Tier</th>
                <th className="pb-2">Consumption Value</th>
                <th className="pb-2">% of Total</th>
                <th className="pb-2">Cumulative %</th>
              </tr>
            </thead>
            <tbody>
              {data.map((r) => (
                <tr key={r.productId} className="border-b last:border-0">
                  <td className="py-2">{r.sku}</td>
                  <td className="py-2">{r.productName}</td>
                  <td className="py-2">
                    <StatusBadge status={r.status} />
                  </td>
                  <td className="py-2">
                    {r.tier ? (
                      <span
                        className="rounded-full px-2 py-0.5 text-xs font-semibold text-white"
                        style={{ backgroundColor: TIER_COLORS[r.tier] }}
                      >
                        {r.tier}
                      </span>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td className="py-2">
                    {r.annualConsumptionValue != null
                      ? `$${r.annualConsumptionValue.toFixed(2)}`
                      : "—"}
                  </td>
                  <td className="py-2">
                    {r.percentOfTotalValue != null
                      ? `${r.percentOfTotalValue.toFixed(1)}%`
                      : "—"}
                  </td>
                  <td className="py-2">
                    {r.cumulativePercent != null
                      ? `${r.cumulativePercent.toFixed(1)}%`
                      : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}

/* ------------------------- Safety Stock ------------------------- */

function SafetyStockTab() {
  const [data, setData] = useState<SafetyStockResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [serviceLevel, setServiceLevel] = useState("0.95");

  function load() {
    setLoading(true);
    api
      .get<SafetyStockResult[]>(
        `/api/analytics/safety-stock?serviceLevel=${serviceLevel}`
      )
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load safety stock data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceLevel]);

  const chartData = data
    .filter((r) => r.status === "OK" && r.safetyStock != null)
    .map((r) => ({ name: r.sku, safetyStock: r.safetyStock }));

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Safety Stock by Product</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 max-w-xs">
            <Field>
              <FieldLabel htmlFor="service-level">Service Level</FieldLabel>
              <Select
                value={serviceLevel}
                onValueChange={(value) => {
                  if (value !== null) setServiceLevel(value);
                }}
              >
                <SelectTrigger id="service-level">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {SERVICE_LEVELS.map((sl) => (
                    <SelectItem key={sl.value} value={sl.value}>
                      {sl.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : chartData.length === 0 ? (
            <p className="text-muted-foreground">
              No products have complete data yet — set supplier lead times and
              generate shipped sales history spanning 2+ days to see safety stock
              here.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="safetyStock" fill="#7c3aed" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {!loading && (
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
                  <th className="pb-2">Supplier</th>
                  <th className="pb-2">Lead Time</th>
                  <th className="pb-2">Mean Daily Demand</th>
                  <th className="pb-2">Std Dev</th>
                  <th className="pb-2">Safety Stock</th>
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
                    <td className="py-2">{r.supplierName}</td>
                    <td className="py-2">
                      {r.leadTimeDays != null ? `${r.leadTimeDays}d` : "—"}
                    </td>
                    <td className="py-2">{r.meanDailyDemand ?? "—"}</td>
                    <td className="py-2">{r.stdDevDailyDemand ?? "—"}</td>
                    <td className="py-2">{r.safetyStock ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

/* ------------------------- Reorder Point ------------------------- */

function ReorderPointTab() {
  const [data, setData] = useState<ReorderPointResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [serviceLevel, setServiceLevel] = useState("0.95");

  function load() {
    setLoading(true);
    api
      .get<ReorderPointResult[]>(
        `/api/analytics/reorder-point?serviceLevel=${serviceLevel}`
      )
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load reorder point data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceLevel]);

  const chartData = data
    .filter((r) => r.status === "OK" && r.reorderPoint != null)
    .map((r) => ({
      name: r.sku,
      currentStock: r.warehouseStock.reduce((sum, w) => sum + w.currentQuantity, 0),
      reorderPoint: r.reorderPoint,
    }));

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Current Stock vs Reorder Point</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 max-w-xs">
            <Field>
              <FieldLabel htmlFor="reorder-service-level">Service Level</FieldLabel>
              <Select
                value={serviceLevel}
                onValueChange={(value) => {
                  if (value !== null) setServiceLevel(value);
                }}
              >
                <SelectTrigger id="reorder-service-level">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {SERVICE_LEVELS.map((sl) => (
                    <SelectItem key={sl.value} value={sl.value}>
                      {sl.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : chartData.length === 0 ? (
            <p className="text-muted-foreground">
              No products have complete data yet.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="currentStock" fill="#0891b2" name="Current Stock" radius={[4, 4, 0, 0]} />
                <Bar dataKey="reorderPoint" fill="#dc2626" name="Reorder Point" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {!loading &&
        data.map((r) => (
          <Card key={r.productId}>
            <CardHeader>
              <CardTitle className="flex items-center gap-3 text-lg">
                {r.productName}
                <span className="text-sm font-normal text-muted-foreground">
                  ({r.sku})
                </span>
                <StatusBadge status={r.status} />
              </CardTitle>
            </CardHeader>
            <CardContent>
              {r.note && (
                <p className="mb-3 text-sm text-muted-foreground">{r.note}</p>
              )}
              {r.status === "OK" && (
                <p className="mb-3 text-sm">
                  Reorder point: <span className="font-semibold">{r.reorderPoint}</span>{" "}
                  (mean daily demand {r.meanDailyDemand} × lead time {r.leadTimeDays}d +
                  safety stock {r.safetyStock})
                </p>
              )}
              {r.warehouseStock.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  No inventory record for this product yet.
                </p>
              ) : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left text-muted-foreground">
                      <th className="pb-2">Warehouse</th>
                      <th className="pb-2">Current Quantity</th>
                      <th className="pb-2">Needs Reorder?</th>
                    </tr>
                  </thead>
                  <tbody>
                    {r.warehouseStock.map((w) => (
                      <tr key={w.warehouseId} className="border-b last:border-0">
                        <td className="py-2">{w.warehouseName}</td>
                        <td className="py-2">{w.currentQuantity}</td>
                        <td className="py-2">
                          {w.belowReorderPoint === null ? (
                            "—"
                          ) : w.belowReorderPoint ? (
                            <span className="font-semibold text-red-500">Yes</span>
                          ) : (
                            <span className="text-green-600">No</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        ))}
    </div>
  );
}

/* ------------------------- Dead Stock ------------------------- */

function DeadStockTab() {
  const [data, setData] = useState<DeadStockResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [thresholdInput, setThresholdInput] = useState("90");
  const [appliedThreshold, setAppliedThreshold] = useState(90);

  function load() {
    setLoading(true);
    api
      .get<DeadStockResult[]>(
        `/api/analytics/dead-stock?thresholdDays=${appliedThreshold}`
      )
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load dead stock data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appliedThreshold]);

  function applyThreshold() {
    const n = Number(thresholdInput);
    if (!isNaN(n) && n > 0) setAppliedThreshold(n);
  }

  const deadItems = data.filter((r) => r.isDeadStock);

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Dead Stock Detection</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex items-end gap-2">
            <Field>
              <FieldLabel htmlFor="threshold">Threshold (days since last sale)</FieldLabel>
              <Input
                id="threshold"
                type="number"
                value={thresholdInput}
                onChange={(e) => setThresholdInput(e.target.value)}
                className="w-40"
              />
            </Field>
            <Button variant="outline" onClick={applyThreshold}>
              Apply
            </Button>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : (
            <p className="mb-4 text-sm text-muted-foreground">
              {deadItems.length} of {data.length} in-stock product(s) flagged as dead
              stock at the {appliedThreshold}-day threshold.
            </p>
          )}
        </CardContent>
      </Card>

      {!loading && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Products With Stock On Hand</CardTitle>
          </CardHeader>
          <CardContent>
            {data.length === 0 ? (
              <p className="text-muted-foreground">
                No products currently have stock on hand.
              </p>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-muted-foreground">
                    <th className="pb-2">SKU</th>
                    <th className="pb-2">Name</th>
                    <th className="pb-2">Qty On Hand</th>
                    <th className="pb-2">Last Sale</th>
                    <th className="pb-2">Days Since</th>
                    <th className="pb-2">Dead Stock?</th>
                  </tr>
                </thead>
                <tbody>
                  {data.map((r) => (
                    <tr key={r.productId} className="border-b last:border-0">
                      <td className="py-2">{r.sku}</td>
                      <td className="py-2">{r.productName}</td>
                      <td className="py-2">{r.totalQuantityOnHand}</td>
                      <td className="py-2">{r.lastSaleDate ?? "Never"}</td>
                      <td className="py-2">{r.daysSinceLastSale ?? "—"}</td>
                      <td className="py-2">
                        {r.isDeadStock ? (
                          <span className="font-semibold text-red-500">Yes</span>
                        ) : (
                          <span className="text-green-600">No</span>
                        )}
                        {r.note && (
                          <p className="mt-1 text-xs text-muted-foreground">{r.note}</p>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

/* ------------------------- Supplier Performance ------------------------- */

function SupplierPerformanceTab() {
  const [data, setData] = useState<SupplierPerformanceResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<SupplierPerformanceResult[]>("/api/analytics/supplier-performance")
      .then((res) => setData(res.data))
      .catch((err) => console.error("Failed to load supplier performance data:", err))
      .finally(() => setLoading(false));
  }, []);

  const chartData = data
    .filter((r) => r.status === "OK" && r.performanceScore != null)
    .map((r) => ({ name: r.supplierName, score: r.performanceScore }));

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Supplier Performance Score</CardTitle>
        </CardHeader>
        <CardContent>
          {chartData.length === 0 ? (
            <p className="text-muted-foreground">
              No suppliers have measurable performance yet — receive at least one PO
              with a stated lead time on the supplier to see scores here.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Bar dataKey="score" fill="#0d9488" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Suppliers</CardTitle>
        </CardHeader>
        <CardContent>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-muted-foreground">
                <th className="pb-2">Supplier</th>
                <th className="pb-2">Status</th>
                <th className="pb-2">Stated Lead Time</th>
                <th className="pb-2">Total POs</th>
                <th className="pb-2">Received</th>
                <th className="pb-2">Pending</th>
                <th className="pb-2">Avg Actual Lead Time</th>
                <th className="pb-2">On-Time Rate</th>
                <th className="pb-2">Score</th>
              </tr>
            </thead>
            <tbody>
              {data.map((r) => (
                <tr key={r.supplierId} className="border-b last:border-0">
                  <td className="py-2">{r.supplierName}</td>
                  <td className="py-2">
                    <StatusBadge status={r.status} />
                    {r.note && (
                      <p className="mt-1 text-xs text-muted-foreground">{r.note}</p>
                    )}
                  </td>
                  <td className="py-2">
                    {r.statedLeadTimeDays != null ? `${r.statedLeadTimeDays}d` : "—"}
                  </td>
                  <td className="py-2">{r.totalPurchaseOrders}</td>
                  <td className="py-2">{r.receivedPurchaseOrders}</td>
                  <td className="py-2">{r.pendingPurchaseOrders}</td>
                  <td className="py-2">
                    {r.averageActualLeadTimeDays != null
                      ? `${r.averageActualLeadTimeDays}d`
                      : "—"}
                  </td>
                  <td className="py-2">
                    {r.onTimeDeliveryRate != null ? `${r.onTimeDeliveryRate}%` : "—"}
                  </td>
                  <td className="py-2">
                    {r.performanceScore != null ? r.performanceScore : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}

/* ------------------------- Page ------------------------- */

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
          <ABCTab />
        </TabsContent>
        <TabsContent value="safety-stock">
          <SafetyStockTab />
        </TabsContent>
        <TabsContent value="reorder-point">
          <ReorderPointTab />
        </TabsContent>
        <TabsContent value="dead-stock">
          <DeadStockTab />
        </TabsContent>
        <TabsContent value="supplier-performance">
          <SupplierPerformanceTab />
        </TabsContent>
      </Tabs>
    </div>
  );
}