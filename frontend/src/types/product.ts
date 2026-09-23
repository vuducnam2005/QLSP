export type ProductStatus = 'ACTIVE' | 'LOW_STOCK' | 'INACTIVE'

export interface Product {
  id: number
  version: number
  productCode: string
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  items: T[]
  pageNo: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiResponse<T> {
  success: boolean
  statusCode: number
  message: string
  data: T
  timestamp: string
}

export interface ProductCreatePayload {
  productCode?: string | null
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
}

export interface ProductUpdatePayload {
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: ProductStatus
  version: number
}

export interface ProductFilters {
  keyword: string
  status: ProductStatus | ''
  sort: 'updatedAt,desc' | 'createdAt,asc' | 'name,asc' | 'price,desc' | 'stockQuantity,asc'
}
