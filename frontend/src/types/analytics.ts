import type { ProductStatus } from './product'

export interface InventoryOverview {
  totalProducts: number
  activeProducts: number
  lowStockProducts: number
  outOfStockProducts: number
  inactiveProducts: number
  totalStockQuantity: number
  totalInventoryValue: number
  averageProductPrice: number
  lowStockRate: number
}

export interface InventoryByStatus {
  status: ProductStatus
  productCount: number
  totalStockQuantity: number
  inventoryValue: number
}

export interface InventoryProduct {
  productId: number
  productCode: string
  name: string
  price: number
  stockQuantity: number
  status: ProductStatus
  inventoryValue: number
}

export type StockDistributionBucket = 'OUT_OF_STOCK' | 'LOW_STOCK' | '1_9' | '10_49' | '50_99' | '100_PLUS'

export interface StockDistribution {
  bucket: StockDistributionBucket
  label: string
  productCount: number
  totalStockQuantity: number
  inventoryValue: number
}

export interface TrendPoint {
  period: string
  productCount: number
  totalStockQuantity: number
  totalInventoryValue: number
}

export type TrendStatus = 'READY' | 'INSUFFICIENT_DATA'

export interface TrendResponse {
  status: TrendStatus
  message: string
  granularity: 'DAY' | 'WEEK' | 'MONTH'
  points: TrendPoint[]
}
