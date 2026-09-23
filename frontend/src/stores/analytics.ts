import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ApiError } from '../api/http'
import {
  getAnalyticsOverview,
  getAnalyticsTrends,
  getInventoryByStatus,
  getLowStock,
  getStockDistribution,
  getTopInventoryValue,
} from '../api/analytics'
import type {
  InventoryByStatus,
  InventoryOverview,
  InventoryProduct,
  StockDistribution,
  TrendResponse,
} from '../types/analytics'

export const useAnalyticsStore = defineStore('analytics', () => {
  const overview = ref<InventoryOverview | null>(null)
  const inventoryByStatus = ref<InventoryByStatus[]>([])
  const topInventoryValue = ref<InventoryProduct[]>([])
  const lowStockProducts = ref<InventoryProduct[]>([])
  const stockDistribution = ref<StockDistribution[]>([])
  const trends = ref<TrendResponse | null>(null)
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  async function fetchAnalytics() {
    loading.value = true
    error.value = null
    try {
      const [overviewResponse, statusResponse, topResponse, lowResponse, distributionResponse, trendResponse] =
        await Promise.all([
          getAnalyticsOverview(),
          getInventoryByStatus(),
          getTopInventoryValue(),
          getLowStock(),
          getStockDistribution(),
          getAnalyticsTrends(),
        ])
      overview.value = overviewResponse.data
      inventoryByStatus.value = statusResponse.data
      topInventoryValue.value = topResponse.data
      lowStockProducts.value = lowResponse.data
      stockDistribution.value = distributionResponse.data
      trends.value = trendResponse.data
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to load analytics', 0)
    } finally {
      loading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    overview,
    inventoryByStatus,
    topInventoryValue,
    lowStockProducts,
    stockDistribution,
    trends,
    loading,
    error,
    fetchAnalytics,
    clearError,
  }
})
