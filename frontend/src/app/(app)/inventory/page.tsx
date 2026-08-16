"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import { Inventory, PagedResponse } from "@/types/inventory";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Field, FieldLabel } from "@/components/ui/field";

export default function InventoryPage() {
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [selectedItem, setSelectedItem] = useState<Inventory | null>(null);
  const [adjustAmount, setAdjustAmount] = useState("");
  const [adjustError, setAdjustError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function loadInventory() {
    setLoading(true);
    api
      .get<PagedResponse<Inventory>>(`/api/inventory?page=${page}&size=10`)
      .then((res) => {
        setInventory(res.data.content);
        setTotalPages(res.data.totalPages);
      })
      .catch((err) => console.error("Failed to load inventory:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadInventory();
  }, [page]);

  function openAdjustDialog(item: Inventory) {
    setSelectedItem(item);
    setAdjustAmount("");
    setAdjustError("");
  }

  async function handleAdjust() {
    if (!selectedItem) return;
    const change = Number(adjustAmount);

    if (!change || isNaN(change)) {
      setAdjustError("Please enter a valid number (positive or negative)");
      return;
    }

    setSubmitting(true);
    setAdjustError("");
    try {
      await api.patch(`/api/inventory/${selectedItem.id}/adjust`, { change });
      setSelectedItem(null);
      loadInventory();
    } catch (err: any) {
      setAdjustError(err.response?.data?.message || "Failed to adjust stock");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Inventory</h1>
        <p className="text-sm text-muted-foreground">
          Stock levels across all warehouses
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Stock Levels</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : inventory.length === 0 ? (
            <p className="text-muted-foreground">No inventory records yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="pb-2">Product</th>
                  <th className="pb-2">SKU</th>
                  <th className="pb-2">Warehouse</th>
                  <th className="pb-2">Quantity</th>
                  <th className="pb-2"></th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((item) => (
                  <tr key={item.id} className="border-b last:border-0">
                    <td className="py-2">{item.productName}</td>
                    <td className="py-2">{item.productSku}</td>
                    <td className="py-2">{item.warehouseName}</td>
                    <td className="py-2">
                      <span
                        className={
                          item.quantity < 20
                            ? "font-semibold text-red-500"
                            : ""
                        }
                      >
                        {item.quantity}
                      </span>
                    </td>
                    <td className="py-2 text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => openAdjustDialog(item)}
                      >
                        Adjust
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {!loading && inventory.length > 0 && (
            <div className="mt-4 flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={!!selectedItem} onOpenChange={(open) => !open && setSelectedItem(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Adjust Stock: {selectedItem?.productName}</DialogTitle>
          </DialogHeader>

          <div className="py-4">
            <p className="mb-4 text-sm text-muted-foreground">
              Current quantity: {selectedItem?.quantity} — enter a positive
              number to increase, or a negative number to decrease.
            </p>
            <Field>
              <FieldLabel htmlFor="change">Change Amount</FieldLabel>
              <Input
                id="change"
                type="number"
                value={adjustAmount}
                onChange={(e) => setAdjustAmount(e.target.value)}
                placeholder="e.g. 10 or -5"
              />
              {adjustError && (
                <p className="text-sm text-red-500">{adjustError}</p>
              )}
            </Field>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedItem(null)}>
              Cancel
            </Button>
            <Button onClick={handleAdjust} disabled={submitting}>
              {submitting ? "Saving..." : "Save"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}