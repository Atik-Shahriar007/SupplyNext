import { Product } from "./product";
import { Warehouse } from "./warehouse";

export interface TransferItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface Transfer {
  id: number;
  fromWarehouse: Warehouse;
  toWarehouse: Warehouse;
  transferDate: string;
  status: string;
  items: TransferItem[];
}