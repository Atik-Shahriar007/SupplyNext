export interface PurchaseOrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
}

export interface PurchaseOrder {
  id: number;
  supplierId: number;
  supplierName: string;
  warehouseId: number;
  warehouseName: string;
  orderDate: string;
  status: string;
  items: PurchaseOrderItem[];
}