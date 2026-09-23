import { request } from './http'
import type { ApiResponse } from '../types/product'
import type {
  InventoryByStatus,
  InventoryOverview,
  InventoryProduct,
  StockDistribution,
  TrendResponse,
} from '../types/analytics'

const endpoint = '/api/v1/analytics'

export function getAnalyticsOverview() {
  return request<ApiResponse<InventoryOverview>>(`${endpoint}/overview`)
}

export function getInventoryByStatus() {
  return request<ApiResponse<InventoryByStatus[]>>(`${endpoint}/inventory-by-status`)
}

export function getTopInventoryValue(limit = 8) {
  return request<ApiResponse<InventoryProduct[]>>(`${endpoint}/top-inventory-value?limit=${limit}`)
}

export function getLowStock(limit = 8) {
  return request<ApiResponse<InventoryProduct[]>>(`${endpoint}/low-stock?limit=${limit}`)
}

export function getStockDistribution() {
  return request<ApiResponse<StockDistribution[]>>(`${endpoint}/stock-distribution`)
}

export function getAnalyticsTrends() {
  return request<ApiResponse<TrendResponse>>(`${endpoint}/trends?granularity=DAY`)
}
