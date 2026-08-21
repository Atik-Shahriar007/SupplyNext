"use client";

import { Product, PagedResponse } from "@/types/product";
import { useEffect, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import api from "@/lib/api";
import { Category } from "@/types/category";
import { Supplier } from "@/types/supplier";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const productSchema = z.object({
  sku: z.string().min(1, "SKU is required"),
  name: z.string().min(1, "Name is required"),
  description: z.string().optional(),
  price: z.coerce.number().positive("Price must be greater than 0"),
  unitCost: z.coerce.number().positive("Unit cost must be greater than 0"),
  holdingCostRate: z.coerce
    .number()
    .positive("Holding cost rate must be greater than 0"),
  orderingCost: z.coerce
    .number()
    .positive("Ordering cost must be greater than 0"),
  categoryId: z.string().min(1, "Category is required"),
  supplierId: z.string().min(1, "Supplier is required"),
});

type ProductFormValues = z.infer<typeof productSchema>;

const emptyDefaults: ProductFormValues = {
  sku: "",
  name: "",
  description: "",
  price: undefined as unknown as number,
  unitCost: undefined as unknown as number,
  holdingCostRate: undefined as unknown as number,
  orderingCost: undefined as unknown as number,
  categoryId: "",
  supplierId: "",
};

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState("");

  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [editError, setEditError] = useState("");

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
} = useForm({
  resolver: zodResolver(productSchema),
  defaultValues: emptyDefaults,
});

  const {
    register: registerEdit,
    control: controlEdit,
    handleSubmit: handleEditSubmit,
    reset: resetEdit,
    formState: { errors: editErrors, isSubmitting: isEditSubmitting },
} = useForm({
  resolver: zodResolver(productSchema),
  defaultValues: emptyDefaults,
});

  function loadAll() {
    setLoading(true);
    Promise.all([
      api.get<PagedResponse<Product>>(`/api/products?page=${page}&size=10`),
      api.get<PagedResponse<Category>>("/api/categories?size=100"),
      api.get<PagedResponse<Supplier>>("/api/suppliers?size=100"),
    ])
      .then(([productsRes, categoriesRes, suppliersRes]) => {
        setProducts(productsRes.data.content);
        setTotalPages(productsRes.data.totalPages);
        setCategories(categoriesRes.data.content);
        setSuppliers(suppliersRes.data.content);
      })
      .catch((err) => console.error("Failed to load data:", err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAll();
  }, [page]);

  async function onSubmit(data: ProductFormValues) {
    setSubmitError("");
    try {
      await api.post("/api/products", {
        sku: data.sku,
        name: data.name,
        description: data.description,
        price: data.price,
        unitCost: data.unitCost,
        holdingCostRate: data.holdingCostRate,
        orderingCost: data.orderingCost,
        categoryId: Number(data.categoryId),
        supplierId: Number(data.supplierId),
      });
      reset(emptyDefaults);
      loadAll();
    } catch (err: any) {
      setSubmitError(err.response?.data?.message || "Failed to create product");
    }
  }

  function openEditDialog(product: Product) {
    setEditError("");
    setEditingProduct(product);
    resetEdit({
      sku: product.sku,
      name: product.name,
      description: product.description ?? "",
      price: product.price,
      unitCost: product.unitCost ?? (undefined as unknown as number),
      holdingCostRate: product.holdingCostRate ?? (undefined as unknown as number),
      orderingCost: product.orderingCost ?? (undefined as unknown as number),
      categoryId: String(product.categoryId),
      supplierId: String(product.supplierId),
    });
  }

  async function onEditSubmit(data: ProductFormValues) {
    if (!editingProduct) return;
    setEditError("");
    try {
      await api.patch(`/api/products/${editingProduct.id}`, {
        sku: data.sku,
        name: data.name,
        description: data.description,
        price: data.price,
        unitCost: data.unitCost,
        holdingCostRate: data.holdingCostRate,
        orderingCost: data.orderingCost,
        categoryId: Number(data.categoryId),
        supplierId: Number(data.supplierId),
      });
      setEditingProduct(null);
      loadAll();
    } catch (err: any) {
      setEditError(err.response?.data?.message || "Failed to update product");
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Products</h1>
        <p className="text-sm text-muted-foreground">Manage your product catalog</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Add New Product</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleSubmit(onSubmit)}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 sm:items-end"
          >
            <Field>
              <FieldLabel htmlFor="sku">SKU</FieldLabel>
              <Input id="sku" {...register("sku")} />
              <FieldError errors={[errors.sku]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="name">Name</FieldLabel>
              <Input id="name" {...register("name")} />
              <FieldError errors={[errors.name]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="price">Price</FieldLabel>
              <Input id="price" type="number" step="0.01" {...register("price")} />
              <FieldError errors={[errors.price]} />
            </Field>

            <Field className="lg:col-span-3">
              <FieldLabel htmlFor="description">Description</FieldLabel>
              <Input id="description" {...register("description")} />
              <FieldError errors={[errors.description]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="unitCost">Unit Cost</FieldLabel>
              <Input id="unitCost" type="number" step="0.01" {...register("unitCost")} />
              <FieldError errors={[errors.unitCost]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="holdingCostRate">
                Holding Cost Rate (e.g. 0.2 = 20%/yr)
              </FieldLabel>
              <Input
                id="holdingCostRate"
                type="number"
                step="0.01"
                {...register("holdingCostRate")}
              />
              <FieldError errors={[errors.holdingCostRate]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="orderingCost">Ordering Cost</FieldLabel>
              <Input id="orderingCost" type="number" step="0.01" {...register("orderingCost")} />
              <FieldError errors={[errors.orderingCost]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="categoryId">Category</FieldLabel>
              <Controller
                name="categoryId"
                control={control}
                render={({ field }) => (
                  <Select onValueChange={field.onChange} value={field.value}>
                    <SelectTrigger id="categoryId">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              <FieldError errors={[errors.categoryId]} />
            </Field>

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

            <div className="sm:col-span-2 lg:col-span-3">
              {submitError && <p className="mb-2 text-sm text-red-500">{submitError}</p>}
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Adding..." : "Add Product"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">All Products</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p>Loading...</p>
          ) : products.length === 0 ? (
            <p className="text-muted-foreground">No products yet.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="pb-2">SKU</th>
                  <th className="pb-2">Name</th>
                  <th className="pb-2">Price</th>
                  <th className="pb-2">Category</th>
                  <th className="pb-2">Supplier</th>
                  <th className="pb-2">Cost Data</th>
                  <th className="pb-2"></th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id} className="border-b last:border-0">
                    <td className="py-2">{p.sku}</td>
                    <td className="py-2">{p.name}</td>
                    <td className="py-2">${p.price.toFixed(2)}</td>
                    <td className="py-2">{p.categoryName}</td>
                    <td className="py-2">{p.supplierName}</td>
                    <td className="py-2">
                      {p.unitCost != null ? (
                        <span className="text-green-600">Set</span>
                      ) : (
                        <span className="text-muted-foreground">Missing</span>
                      )}
                    </td>
                    <td className="py-2 text-right">
                      <Button variant="outline" size="sm" onClick={() => openEditDialog(p)}>
                        Edit
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {!loading && products.length > 0 && (
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
        open={!!editingProduct}
        onOpenChange={(open) => !open && setEditingProduct(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Product: {editingProduct?.name}</DialogTitle>
          </DialogHeader>

          <form
            onSubmit={handleEditSubmit(onEditSubmit)}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2 py-2"
          >
            <Field>
              <FieldLabel htmlFor="edit-sku">SKU</FieldLabel>
              <Input id="edit-sku" {...registerEdit("sku")} />
              <FieldError errors={[editErrors.sku]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-name">Name</FieldLabel>
              <Input id="edit-name" {...registerEdit("name")} />
              <FieldError errors={[editErrors.name]} />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="edit-description">Description</FieldLabel>
              <Input id="edit-description" {...registerEdit("description")} />
              <FieldError errors={[editErrors.description]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-price">Price</FieldLabel>
              <Input id="edit-price" type="number" step="0.01" {...registerEdit("price")} />
              <FieldError errors={[editErrors.price]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-unitCost">Unit Cost</FieldLabel>
              <Input
                id="edit-unitCost"
                type="number"
                step="0.01"
                {...registerEdit("unitCost")}
              />
              <FieldError errors={[editErrors.unitCost]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-holdingCostRate">Holding Cost Rate</FieldLabel>
              <Input
                id="edit-holdingCostRate"
                type="number"
                step="0.01"
                {...registerEdit("holdingCostRate")}
              />
              <FieldError errors={[editErrors.holdingCostRate]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-orderingCost">Ordering Cost</FieldLabel>
              <Input
                id="edit-orderingCost"
                type="number"
                step="0.01"
                {...registerEdit("orderingCost")}
              />
              <FieldError errors={[editErrors.orderingCost]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-categoryId">Category</FieldLabel>
              <Controller
                name="categoryId"
                control={controlEdit}
                render={({ field }) => (
                  <Select onValueChange={field.onChange} value={field.value}>
                    <SelectTrigger id="edit-categoryId">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              <FieldError errors={[editErrors.categoryId]} />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-supplierId">Supplier</FieldLabel>
              <Controller
                name="supplierId"
                control={controlEdit}
                render={({ field }) => (
                  <Select onValueChange={field.onChange} value={field.value}>
                    <SelectTrigger id="edit-supplierId">
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
              <FieldError errors={[editErrors.supplierId]} />
            </Field>

            {editError && (
              <p className="sm:col-span-2 text-sm text-red-500">{editError}</p>
            )}

            <DialogFooter className="sm:col-span-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setEditingProduct(null)}
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