"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { Supplier } from "@/types/supplier";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

const supplierSchema = z.object({
  name: z.string().min(1, "Name is required"),
  contactPerson: z.string().optional(),
  phone: z.string().optional(),
  email: z.string().email("Must be a valid email").optional().or(z.literal("")),
  address: z.string().optional(),
});

export default function SuppliersPage() {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState("");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(supplierSchema),
  });

  function loadSuppliers() {
    setLoading(true);
    api
      .get<Supplier[]>("/api/suppliers")
      .then((res) => setSuppliers(res.data))
      .catch((err) => console.error("Failed to load suppliers:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadSuppliers();
  }, []);

  async function onSubmit(data: any) {
    setSubmitError("");
    try {
      await api.post("/api/suppliers", data);
      reset();
      loadSuppliers();
    } catch (err: any) {
      setSubmitError(
        err.response?.data?.message || "Failed to create supplier"
      );
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

            <Field className="sm:col-span-2 lg:col-span-2">
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
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}