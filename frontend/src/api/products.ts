import { request } from './http'
import type {
  ApiResponse,
  PageResponse,
  Product,
  ProductCreatePayload,
  ProductStatus,
  ProductUpdatePayload,
} from '../types/product'

const endpoint = '/api/v1/products'

export function getProducts(params: {
  page: number
  size: number
  sort: string
  keyword?: string
  status?: ProductStatus
}) {
  const query = new URLSearchParams({
    page: String(params.page),
    size: String(params.size),
    sort: params.sort,
  })
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.status) query.set('status', params.status)
  return request<ApiResponse<PageResponse<Product>>>(`${endpoint}?${query}`)
}

export function getProduct(id: number) {
  return request<ApiResponse<Product>>(`${endpoint}/${id}`)
}

export function createProduct(payload: ProductCreatePayload) {
  return request<ApiResponse<Product>>(endpoint, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProduct(id: number, payload: ProductUpdatePayload) {
  return request<ApiResponse<Product>>(`${endpoint}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProduct(id: number) {
  return request<ApiResponse<null>>(`${endpoint}/${id}`, { method: 'DELETE' })
}
