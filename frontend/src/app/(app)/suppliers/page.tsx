"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { Supplier, PagedResponse } from "@/types/supplier";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

const supplierSchema = z.object({
  name: z.string().min(1, "Name is required"),
  contactPerson: z.string().optional(),
  phone: z.string().optional(),
  email: z.string().email("Must be a valid email").optional().or(z.literal("")),
  address: z.string().optional(),
  leadTimeDays: z.coerce
    .number()
    .positive("Lead time must be greater than 0"),
});

type SupplierFormValues = z.infer<typeof supplierSchema>;

const emptyDefaults: SupplierFormValues = {
  name: "",
  contactPerson: "",
  phone: "",
  email: "",
  address: "",
  leadTimeDays: undefined as unknown as number,
};

export default function SuppliersPage() {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const [editError, setEditError] = useState("");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
} = useForm({
  resolver: zodResolver(supplierSchema),
  defaultValues: emptyDefaults,
});

  const {
    register: registerEdit,
    handleSubmit: handleEditSubmit,
    reset: resetEdit,
    formState: { errors: editErrors, isSubmitting: isEditSubmitting },
} = useForm({
  resolver: zodResolver(supplierSchema),
  defaultValues: emptyDefaults,
});

  function loadSuppliers() {
    setLoading(true);
    api
      .get<PagedResponse<Supplier>>(`/api/suppliers?page=${page}&size=10`)
      .then((res) => {
        setSuppliers(res.data.content);
        setTotalPages(res.data.totalPages);
      })
      .catch((err) => console.error("Failed to load suppliers:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadSuppliers();
  }, [page]);

  async function onSubmit(data: SupplierFormValues) {
    setSubmitError("");
    try {
      await api.post("/api/suppliers", data);
      reset(emptyDefaults);
      loadSuppliers();
    } catch (err: any) {
      setSubmitError(
        err.response?.data?.message || "Failed to create supplier"
      );
    }
  }

  function openEditDialog(supplier: Supplier) {
    setEditError("");
    setEditingSupplier(supplier);
    resetEdit({
      name: supplier.name,
      contactPerson: supplier.contactPerson ?? "",
      phone: supplier.phone ?? "",
      email: supplier.email ?? "",
      address: supplier.address ?? "",
      leadTimeDays: supplier.leadTimeDays ?? (undefined as unknown as number),
    });
  }

  async function onEditSubmit(data: SupplierFormValues) {
    if (!editingSupplier) return;
    setEditError("");
    try {
      await api.patch(`/api/suppliers/${editingSupplier.id}`, data);
      setEditingSupplier(null);
      loadSuppliers();
    } catch (err: any) {
      setEditError(err.response?.data?.message || "Failed to update supplier");
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Suppliers</h1>
        <p className="text-sm text-muted-foreground">Manage your suppliers</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Add New Supplier</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleSubmit(onSubmit)}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 sm:items-end"
          >
            <Field>
              <FieldLabel htmlFor="name">Name</FieldLabel>
              <Input id="name" {...register("name")} />
              <FieldError errors={[errors.name]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="contactPerson">Contact Person</FieldLabel>
              <Input id="contactPerson" {...register("contactPerson")} />
              <FieldError errors={[errors.contactPerson]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="phone">Phone</FieldLabel>
              <Input id="phone" {...register("phone")} />
              <FieldError errors={[errors.phone]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="email">Email</FieldLabel>
              <Input id="email" {...register("email")} />
              <FieldError errors={[errors.email]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="leadTimeDays">Lead Time (days)</FieldLabel>
              <Input
                id="leadTimeDays"
                type="number"
                {...register("leadTimeDays")}
              />
              <FieldError errors={[errors.leadTimeDays]} />
            </Field>

            <Field className="sm:col-span-2 lg:col-span-3">
              <FieldLabel htmlFor="address">Address</FieldLabel>
              <Input id="address" {...register("address")} />
              <FieldError errors={[errors.address]} />
            </Field>

            <div className="sm:col-span-2 lg:col-span-3">
              {submitError && (
                <p className="mb-2 text-sm text-red-500">{submitError}</p>
              )}
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Adding..." : "Add Supplier"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Suppliers</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : suppliers.length === 0 ? (
            <p className="text-muted-foreground">No suppliers yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="pb-2">ID</th>
                  <th className="pb-2">Name</th>
                  <th className="pb-2">Contact</th>
                  <th className="pb-2">Phone</th>
                  <th className="pb-2">Email</th>
                  <th className="pb-2">Address</th>
                  <th className="pb-2">Lead Time</th>
                  <th className="pb-2"></th>
                </tr>
              </thead>
              <tbody>
                {suppliers.map((s) => (
                  <tr key={s.id} className="border-b last:border-0">
                    <td className="py-2">{s.id}</td>
                    <td className="py-2">{s.name}</td>
                    <td className="py-2">{s.contactPerson}</td>
                    <td className="py-2">{s.phone}</td>
                    <td className="py-2">{s.email}</td>
                    <td className="py-2">{s.address}</td>
                    <td className="py-2">
                      {s.leadTimeDays != null ? (
                        `${s.leadTimeDays} days`
                      ) : (
                        <span className="text-muted-foreground">Not set</span>
                      )}
                    </td>
                    <td className="py-2 text-right">
                      <Button variant="outline" size="sm" onClick={() => openEditDialog(s)}>
                        Edit
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {!loading && suppliers.length > 0 && (
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

      <Dialog
        open={!!editingSupplier}
        onOpenChange={(open) => !open && setEditingSupplier(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Supplier: {editingSupplier?.name}</DialogTitle>
          </DialogHeader>

          <form
            onSubmit={handleEditSubmit(onEditSubmit)}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2 py-2"
          >
            <Field>
              <FieldLabel htmlFor="edit-name">Name</FieldLabel>
              <Input id="edit-name" {...registerEdit("name")} />
              <FieldError errors={[editErrors.name]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-contactPerson">Contact Person</FieldLabel>
              <Input id="edit-contactPerson" {...registerEdit("contactPerson")} />
              <FieldError errors={[editErrors.contactPerson]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-phone">Phone</FieldLabel>
              <Input id="edit-phone" {...registerEdit("phone")} />
              <FieldError errors={[editErrors.phone]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-email">Email</FieldLabel>
              <Input id="edit-email" {...registerEdit("email")} />
              <FieldError errors={[editErrors.email]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-leadTimeDays">Lead Time (days)</FieldLabel>
              <Input
                id="edit-leadTimeDays"
                type="number"
                {...registerEdit("leadTimeDays")}
              />
              <FieldError errors={[editErrors.leadTimeDays]} />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="edit-address">Address</FieldLabel>
              <Input id="edit-address" {...registerEdit("address")} />
              <FieldError errors={[editErrors.address]} />
            </Field>

            {editError && (
              <p className="sm:col-span-2 text-sm text-red-500">{editError}</p>
            )}

            <DialogFooter className="sm:col-span-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setEditingSupplier(null)}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isEditSubmitting}>
                {isEditSubmitting ? "Saving..." : "Save"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}