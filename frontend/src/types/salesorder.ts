export interface SalesOrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
}

export interface SalesOrder {
  id: number;
  customerName: string;
  warehouseId: number;
  warehouseName: string;
  orderDate: string;
  status: string;
  items: SalesOrderItem[];
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