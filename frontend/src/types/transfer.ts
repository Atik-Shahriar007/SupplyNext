export interface TransferItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
}

export interface Transfer {
  id: number;
  fromWarehouseId: number;
  fromWarehouseName: string;
  toWarehouseId: number;
  toWarehouseName: string;
  transferDate: string;
  status: string;
  items: TransferItem[];
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