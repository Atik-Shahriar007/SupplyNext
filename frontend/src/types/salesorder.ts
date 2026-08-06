import { Product } from "./product";
import { Warehouse } from "./warehouse";

export interface SalesOrderItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface SalesOrder {
  id: number;
  customerName: string;
  warehouse: Warehouse;
  orderDate: string;
  status: string;
  items: SalesOrderItem[];
}