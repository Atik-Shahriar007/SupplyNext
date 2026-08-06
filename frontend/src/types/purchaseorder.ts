import { Product } from "./product";
import { Warehouse } from "./warehouse";
import { Supplier } from "./supplier";

export interface PurchaseOrderItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface PurchaseOrder {
  id: number;
  supplier: Supplier;
  warehouse: Warehouse;
  orderDate: string;
  status: string;
  items: PurchaseOrderItem[];
}