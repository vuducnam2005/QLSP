import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import { ApiError } from '../api/http'
import { createProduct, deleteProduct, getProduct, getProducts, updateProduct } from '../api/products'
import { useSettingsStore } from './settings'
import type {
  Product,
  ProductCreatePayload,
  ProductFilters,
  ProductStatus,
  ProductUpdatePayload,
} from '../types/product'

export const useProductsStore = defineStore('products', () => {
  const settingsStore = useSettingsStore()
  const items = ref<Product[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<ApiError | null>(null)
  const page = reactive({ pageNo: 0, pageSize: 10, totalElements: 0, totalPages: 0, last: true })
  const filters = reactive<ProductFilters>({ keyword: '', status: '', sort: 'updatedAt,desc' })

  const activeCount = computed(() => items.value.filter((product) => product.status === 'ACTIVE').length)
  const lowStockCount = computed(
    () => items.value.filter((product) => product.status === 'LOW_STOCK' || product.stockQuantity < settingsStore.lowStockThreshold).length,
  )
  const inactiveCount = computed(() => items.value.filter((product) => product.status === 'INACTIVE').length)

  async function fetchProducts() {
    loading.value = true
    error.value = null
    try {
      const response = await getProducts({
        page: page.pageNo,
        size: page.pageSize,
        sort: filters.sort,
        keyword: filters.keyword.trim() || undefined,
        status: filters.status || undefined,
      })
      items.value = response.data.items
      page.pageNo = response.data.pageNo
      page.pageSize = response.data.pageSize
      page.totalElements = response.data.totalElements
      page.totalPages = response.data.totalPages
      page.last = response.data.last
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to load products', 0)
      items.value = []
    } finally {
      loading.value = false
    }
  }

  function setFilters(next: Partial<ProductFilters>) {
    Object.assign(filters, next)
    page.pageNo = 0
  }

  function setPage(nextPage: number) {
    page.pageNo = nextPage
  }

  async function create(payload: ProductCreatePayload) {
    saving.value = true
    error.value = null
    try {
      const response = await createProduct(payload)
      await fetchProducts()
      return response.data
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to create product', 0)
      throw error.value
    } finally {
      saving.value = false
    }
  }

  async function update(id: number, payload: ProductUpdatePayload) {
    saving.value = true
    error.value = null
    try {
      const response = await updateProduct(id, payload)
      await fetchProducts()
      return response.data
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to update product', 0)
      throw error.value
    } finally {
      saving.value = false
    }
  }

  async function refresh(id: number) {
    try {
      const response = await getProduct(id)
      const index = items.value.findIndex((product) => product.id === id)
      if (index >= 0) items.value[index] = response.data
      error.value = null
      return response.data
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to reload product', 0)
      throw error.value
    }
  }

  async function remove(id: number) {
    saving.value = true
    error.value = null
    try {
      await deleteProduct(id)
      if (items.value.length === 1 && page.pageNo > 0) page.pageNo -= 1
      await fetchProducts()
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to delete product', 0)
      throw error.value
    } finally {
      saving.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    items,
    loading,
    saving,
    error,
    page,
    filters,
    activeCount,
    lowStockCount,
    inactiveCount,
    fetchProducts,
    setFilters,
    setPage,
    create,
    update,
    refresh,
    remove,
    clearError,
  }
})
