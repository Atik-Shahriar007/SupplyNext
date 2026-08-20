export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  unitCost: number | null;
  holdingCostRate: number | null;
  orderingCost: number | null;
  categoryId: number;
  categoryName: string;
  supplierId: number;
  supplierName: string;
}

export interface PagedResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}