"use client";

import { useEffect, useState } from "react";
import { useForm, Controller, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { Transfer } from "@/types/transfer";
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

const transferSchema = z
  .object({
    fromWarehouseId: z.string().min(1, "Source warehouse is required"),
    toWarehouseId: z.string().min(1, "Destination warehouse is required"),
    transferDate: z.string().min(1, "Transfer date is required"),
    items: z
      .array(
        z.object({
          productId: z.string().min(1, "Product is required"),
          quantity: z.coerce.number().positive("Quantity must be greater than 0"),
        })
      )
      .min(1, "Add at least one item"),
  })
  .refine((data) => data.fromWarehouseId !== data.toWarehouseId, {
    message: "Source and destination warehouses must be different",
    path: ["toWarehouseId"],
  });

export default function TransfersPage() {
  const [transfers, setTransfers] = useState<Transfer[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState("");
  const [actionError, setActionError] = useState("");

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(transferSchema),
    defaultValues: {
  fromWarehouseId: "",
  toWarehouseId: "",
  transferDate: "",
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
      api.get<Transfer[]>("/api/transfers"),
      api.get<Warehouse[]>("/api/warehouses"),
      api.get<Product[]>("/api/products"),
    ])
      .then(([transfersRes, warehousesRes, productsRes]) => {
        setTransfers(transfersRes.data);
        setWarehouses(warehousesRes.data);
        setProducts(productsRes.data);
      })
      .catch((err) => console.error("Failed to load data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAll();
  }, []);

  async function onSubmit(data: any) {
    setSubmitError("");
    try {
      await api.post("/api/transfers", {
    fromWarehouseId: Number(data.fromWarehouseId),
    toWarehouseId: Number(data.toWarehouseId),
    transferDate: data.transferDate,
    items: data.items.map((item: any) => ({
    productId: Number(item.productId),
    quantity: item.quantity,
  })),
});
      reset({   fromWarehouseId: "",
  toWarehouseId: "",
  transferDate: "",
  items: [{ productId: "", quantity: 1 }], });
      loadAll();
    } catch (err: any) {
      setSubmitError(err.response?.data?.message || "Failed to create transfer");
    }
  }

  async function handleComplete(id: number) {
    setActionError("");
    try {
      await api.patch(`/api/transfers/${id}/complete`);
      loadAll();
    } catch (err: any) {
      setActionError(err.response?.data?.message || "Failed to complete transfer");
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Transfers</h1>
        <p className="text-sm text-muted-foreground">
          Move stock between warehouses
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Create Transfer</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Field>
                <FieldLabel htmlFor="fromWarehouseId">From Warehouse</FieldLabel>
                <Controller
                  name="fromWarehouseId"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger id="fromWarehouseId">
                        <SelectValue placeholder="Select source" />
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
                <FieldError errors={[errors.fromWarehouseId]} />
              </Field>

              <Field>
                <FieldLabel htmlFor="toWarehouseId">To Warehouse</FieldLabel>
                <Controller
                  name="toWarehouseId"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger id="toWarehouseId">
                        <SelectValue placeholder="Select destination" />
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
                <FieldError errors={[errors.toWarehouseId]} />
              </Field>

              <Field>
                <FieldLabel htmlFor="transferDate">Transfer Date</FieldLabel>
                <Input id="transferDate" type="date" {...register("transferDate")} />
                <FieldError errors={[errors.transferDate]} />
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
              {isSubmitting ? "Creating..." : "Create Transfer"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Transfers</CardTitle>
        </CardHeader>
        <CardContent>
          {actionError && (
            <p className="mb-3 text-sm text-red-500">{actionError}</p>
          )}
          {loading ? (
            <p>Loading...</p>
          ) : transfers.length === 0 ? (
            <p className="text-muted-foreground">No transfers yet.</p>
          ) : (
            <div className="space-y-4">
              {transfers.map((t) => (
                <div key={t.id} className="rounded-lg border p-4">
                  <div className="mb-2 flex items-center justify-between">
                    <div>
                      <p className="font-medium">
                      Transfer #{t.id} — {t.fromWarehouseName} → {t.toWarehouseName}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {t.transferDate} —{" "}
                        <span
                          className={
                            t.status === "COMPLETED"
                              ? "text-green-600"
                              : "text-amber-600"
                          }
                        >
                          {t.status}
                        </span>
                      </p>
                    </div>
                    {t.status === "PENDING" && (
                      <Button size="sm" onClick={() => handleComplete(t.id)}>
                        Complete
                      </Button>
                    )}
                  </div>
                  <ul className="text-sm text-muted-foreground">
                    {t.items?.map((item) => (
                  <li key={item.id}>
                  {item.productName} — Qty: {item.quantity}
                  </li>
                  ))}
                  </ul>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}