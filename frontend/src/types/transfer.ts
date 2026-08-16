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