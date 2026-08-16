"use client";

import { useEffect, useState } from "react";
import { useForm, Controller, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { PurchaseOrder, PagedResponse } from "@/types/purchaseorder";
import { Supplier } from "@/types/supplier";
import { Warehouse } from "@/types/warehouse";
import { Product } from "@/types/product";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Trash2, Plus } from "lucide-react";

const poSchema = z.object({
  supplierId: z.string().min(1, "Supplier is required"),
  warehouseId: z.string().min(1, "Warehouse is required"),
  orderDate: z.string().min(1, "Order date is required"),
  items: z
    .array(
      z.object({
        productId: z.string().min(1, "Product is required"),
        quantity: z.coerce.number().positive("Quantity must be greater than 0"),
      })
    )
    .min(1, "Add at least one item"),
});

export default function PurchaseOrdersPage() {
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState("");
  const [actionError, setActionError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(poSchema),
    defaultValues: {
      supplierId: "",
      warehouseId: "",
      orderDate: "",
      items: [{ productId: "", quantity: 1 }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "items",
  });

  function loadAll() {
    setLoading(true);
    Promise.all([
      api.get<PagedResponse<PurchaseOrder>>(`/api/purchase-orders?page=${page}&size=10`),
      api.get<PagedResponse<Supplier>>("/api/suppliers?size=100"),
      api.get<PagedResponse<Warehouse>>("/api/warehouses?size=100"),
      api.get<PagedResponse<Product>>("/api/products?size=100"),
    ])
      .then(([ordersRes, suppliersRes, warehousesRes, productsRes]) => {
        setOrders(ordersRes.data.content);
        setTotalPages(ordersRes.data.totalPages);
        setSuppliers(suppliersRes.data.content);
        setWarehouses(warehousesRes.data.content);
        setProducts(productsRes.data.content);
      })
      .catch((err) => console.error("Failed to load data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAll();
  }, [page]);

  async function onSubmit(data: any) {
    setSubmitError("");
    try {
      await api.post("/api/purchase-orders", {
        supplierId: Number(data.supplierId),
        warehouseId: Number(data.warehouseId),
        orderDate: data.orderDate,
        items: data.items.map((item: any) => ({
          productId: Number(item.productId),
          quantity: item.quantity,
        })),
      });
      reset({
        supplierId: "",
        warehouseId: "",
        orderDate: "",
        items: [{ productId: "", quantity: 1 }],
      });
      loadAll();
    } catch (err: any) {
      setSubmitError(err.response?.data?.message || "Failed to create purchase order");
    }
  }

  async function handleReceive(id: number) {
    setActionError("");
    try {
      await api.patch(`/api/purchase-orders/${id}/receive`);
      loadAll();
    } catch (err: any) {
      setActionError(err.response?.data?.message || "Failed to receive order");
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Purchase Orders</h1>
        <p className="text-sm text-muted-foreground">
          Order stock from suppliers
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Create Purchase Order</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Field>
                <FieldLabel htmlFor="supplierId">Supplier</FieldLabel>
                <Controller
                  name="supplierId"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger id="supplierId">
                        <SelectValue placeholder="Select supplier" />
                      </SelectTrigger>
                      <SelectContent>
                        {suppliers.map((s) => (
                          <SelectItem key={s.id} value={String(s.id)}>
                            {s.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <FieldError errors={[errors.supplierId]} />
              </Field>

              <Field>
                <FieldLabel htmlFor="warehouseId">Warehouse</FieldLabel>
                <Controller
                  name="warehouseId"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger id="warehouseId">
                        <SelectValue placeholder="Select warehouse" />
                      </SelectTrigger>
                      <SelectContent>
                        {warehouses.map((w) => (
                          <SelectItem key={w.id} value={String(w.id)}>
                            {w.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <FieldError errors={[errors.warehouseId]} />
              </Field>

              <Field>
                <FieldLabel htmlFor="orderDate">Order Date</FieldLabel>
                <Input id="orderDate" type="date" {...register("orderDate")} />
                <FieldError errors={[errors.orderDate]} />
              </Field>
            </div>

            <div className="space-y-3">
              <FieldLabel>Items</FieldLabel>
              {fields.map((field, index) => (
                <div key={field.id} className="flex items-end gap-3">
                  <Field className="flex-1">
                    <FieldLabel htmlFor={`items.${index}.productId`}>
                      Product
                    </FieldLabel>
                    <Controller
                      name={`items.${index}.productId`}
                      control={control}
                      render={({ field }) => (
                        <Select onValueChange={field.onChange} value={field.value}>
                          <SelectTrigger id={`items.${index}.productId`}>
                            <SelectValue placeholder="Select product" />
                          </SelectTrigger>
                          <SelectContent>
                            {products.map((p) => (
                              <SelectItem key={p.id} value={String(p.id)}>
                                {p.name} ({p.sku})
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      )}
                    />
                  </Field>

                  <Field className="w-32">
                    <FieldLabel htmlFor={`items.${index}.quantity`}>
                      Quantity
                    </FieldLabel>
                    <Input
                      id={`items.${index}.quantity`}
                      type="number"
                      {...register(`items.${index}.quantity`)}
                    />
                  </Field>

                  <Button
                    type="button"
                    variant="outline"
                    size="icon"
                    onClick={() => remove(index)}
                    disabled={fields.length === 1}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}
              {errors.items?.message && (
                <p className="text-sm text-red-500">{errors.items.message}</p>
              )}

              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => append({ productId: "", quantity: 1 })}
              >
                <Plus className="mr-1 h-4 w-4" />
                Add Item
              </Button>
            </div>

            {submitError && <p className="text-sm text-red-500">{submitError}</p>}
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creating..." : "Create Purchase Order"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Purchase Orders</CardTitle>
        </CardHeader>
        <CardContent>
          {actionError && (
            <p className="mb-3 text-sm text-red-500">{actionError}</p>
          )}
          {loading ? (
            <p>Loading...</p>
          ) : orders.length === 0 ? (
            <p className="text-muted-foreground">No purchase orders yet.</p>
          ) : (
            <div className="space-y-4">
              {orders.map((po) => (
                <div key={po.id} className="rounded-lg border p-4">
                  <div className="mb-2 flex items-center justify-between">
                    <div>
                      <p className="font-medium">
                        PO #{po.id} — {po.supplierName} → {po.warehouseName}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {po.orderDate} —{" "}
                        <span
                          className={
                            po.status === "RECEIVED"
                              ? "text-green-600"
                              : "text-amber-600"
                          }
                        >
                          {po.status}
                        </span>
                      </p>
                    </div>
                    {po.status === "PENDING" && (
                      <Button size="sm" onClick={() => handleReceive(po.id)}>
                        Receive
                      </Button>
                    )}
                  </div>
                  <ul className="text-sm text-muted-foreground">
                    {po.items?.map((item) => (
                      <li key={item.id}>
                        {item.productName} — Qty: {item.quantity}
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          )}

          {!loading && orders.length > 0 && (
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
    </div>
  );
}