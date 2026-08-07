
export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  categoryName: string;
  supplierId: number;
  supplierName: string;
}