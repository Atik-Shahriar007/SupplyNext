export interface EOQResult {
  productId: number;
  sku: string;
  productName: string;
  status: "OK" | "MISSING_COST_DATA" | "INSUFFICIENT_DATA";
  note: string | null;
  annualDemand: number | null;
  unitCost: number | null;
  holdingCostRate: number | null;
  orderingCost: number | null;
  holdingCostPerUnit: number | null;
  eoq: number | null;
  recommendedOrdersPerYear: number | null;
}

export interface ABCResult {
  productId: number;
  sku: string;
  productName: string;
  status: "OK" | "MISSING_COST_DATA";
  annualDemand: number | null;
  unitCost: number | null;
  annualConsumptionValue: number | null;
  percentOfTotalValue: number | null;
  cumulativePercent: number | null;
  tier: "A" | "B" | "C" | null;
}

export interface SafetyStockResult {
  productId: number;
  sku: string;
  productName: string;
  status: "OK" | "MISSING_LEAD_TIME" | "INSUFFICIENT_DATA";
  note: string | null;
  supplierId: number;
  supplierName: string;
  leadTimeDays: number | null;
  serviceLevel: number;
  zScore: number;
  meanDailyDemand: number | null;
  stdDevDailyDemand: number | null;
  safetyStock: number | null;
}

export interface WarehouseStock {
  warehouseId: number;
  warehouseName: string;
  currentQuantity: number;
  belowReorderPoint: boolean | null;
}

export interface ReorderPointResult {
  productId: number;
  sku: string;
  productName: string;
  status: "OK" | "MISSING_LEAD_TIME" | "INSUFFICIENT_DATA";
  note: string | null;
  leadTimeDays: number | null;
  meanDailyDemand: number | null;
  safetyStock: number | null;
  reorderPoint: number | null;
  warehouseStock: WarehouseStock[];
}

export interface WarehouseQuantity {
  warehouseId: number;
  warehouseName: string;
  quantity: number;
}

export interface DeadStockResult {
  productId: number;
  sku: string;
  productName: string;
  totalQuantityOnHand: number;
  lastSaleDate: string | null;
  daysSinceLastSale: number | null;
  thresholdDays: number;
  isDeadStock: boolean;
  note: string | null;
  warehouseStock: WarehouseQuantity[];
}

export interface SupplierPerformanceResult {
  supplierId: number;
  supplierName: string;
  status: "OK" | "MISSING_LEAD_TIME" | "NO_RECEIVED_ORDERS";
  note: string | null;
  statedLeadTimeDays: number | null;
  totalPurchaseOrders: number;
  receivedPurchaseOrders: number;
  pendingPurchaseOrders: number;
  receivedWithoutDateCount: number;
  averageActualLeadTimeDays: number | null;
  onTimeDeliveryRate: number | null;
  performanceScore: number | null;
}