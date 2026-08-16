"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";
import { Warehouse, PagedResponse } from "@/types/warehouse";

const warehouseSchema = z.object({
  name: z.string().min(1, "Name is required"),
  location: z.string().min(1, "Location is required"),
  capacity: z.coerce.number().positive("Capacity must be greater than 0"),
});

type WarehouseFormValues = z.infer<typeof warehouseSchema>;

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [submitError, setSubmitError] = useState("");

  const {
  register,
  handleSubmit,
  reset,
  formState: { errors, isSubmitting },
} = useForm({
  resolver: zodResolver(warehouseSchema),
});

  function loadWarehouses() {
  setLoading(true);
  api
    .get<PagedResponse<Warehouse>>(`/api/warehouses?page=${page}&size=10`)
    .then((res) => {
      setWarehouses(res.data.content);
      setTotalPages(res.data.totalPages);
    })
    .catch((err) => console.error("Failed to load warehouses:", err))
    .finally(() => setLoading(false));
}

  useEffect(() => { loadWarehouses(); }, [page]);

  async function onSubmit(data: WarehouseFormValues) {
    setSubmitError("");
    try {
      await api.post("/api/warehouses", data);
      reset();
      loadWarehouses();
    } catch (err: any) {
      setSubmitError(
        err.response?.data?.message || "Failed to create warehouse"
      );
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Warehouses</h1>
        <p className="text-sm text-muted-foreground">
          Manage your warehouse locations
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Add New Warehouse</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleSubmit(onSubmit)}
            className="grid grid-cols-1 gap-4 sm:grid-cols-3 sm:items-end"
          >
            <Field>
  <FieldLabel htmlFor="name">Name</FieldLabel>
  <Input id="name" {...register("name")} />
  <FieldError errors={[errors.name]} />
</Field>

<Field>
  <FieldLabel htmlFor="location">Location</FieldLabel>
  <Input id="location" {...register("location")} />
  <FieldError errors={[errors.location]} />
</Field>

<Field>
  <FieldLabel htmlFor="capacity">Capacity</FieldLabel>
  <Input id="capacity" type="number" {...register("capacity")} />
  <FieldError errors={[errors.capacity]} />
</Field>

            <div className="sm:col-span-3">
              {submitError && (
                <p className="mb-2 text-sm text-red-500">{submitError}</p>
              )}
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Adding..." : "Add Warehouse"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Warehouses</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : warehouses.length === 0 ? (
            <p className="text-muted-foreground">No warehouses yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="pb-2">ID</th>
                  <th className="pb-2">Name</th>
                  <th className="pb-2">Location</th>
                  <th className="pb-2">Capacity</th>
                </tr>
              </thead>
              <tbody>
                {warehouses.map((w) => (
                  <tr key={w.id} className="border-b last:border-0">
                    <td className="py-2">{w.id}</td>
                    <td className="py-2">{w.name}</td>
                    <td className="py-2">{w.location}</td>
                    <td className="py-2">{w.capacity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {!loading && warehouses.length > 0 && (
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