<script setup lang="ts">
import type { Product } from '../types/product'
import { formatCurrency, formatDate, formatNumber } from '../utils/formatters'

defineProps<{
  products: Product[]
  loading: boolean
  currency: 'VND' | 'USD'
  dateFormat: 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD'
  lowStockThreshold: number
}>()

const emit = defineEmits<{
  view: [product: Product]
  edit: [product: Product]
  remove: [product: Product]
}>()

</script>

<template>
  <div class="table-shell" :aria-busy="loading">
    <div v-if="loading" class="skeleton-table" aria-label="Đang tải sản phẩm" role="status">
      <div v-for="row in 6" :key="row" class="skeleton-row">
        <span class="skeleton-block skeleton-product"></span>
        <span class="skeleton-block"></span>
        <span class="skeleton-block skeleton-short"></span>
        <span class="skeleton-block skeleton-short"></span>
        <span class="skeleton-block skeleton-short"></span>
        <span class="skeleton-block skeleton-action"></span>
      </div>
    </div>

    <div v-else-if="products.length === 0" class="empty-state">
      <div class="empty-icon" aria-hidden="true">+</div>
       <p class="eyebrow">Không có sản phẩm phù hợp</p>
       <h3>Danh mục đang trống</h3>
       <p>Hãy xóa bộ lọc hoặc thêm sản phẩm đầu tiên vào không gian làm việc này.</p>
    </div>

    <table v-else class="product-table">
      <thead>
        <tr>
           <th>Sản phẩm</th>
           <th>Mã</th>
           <th>Trạng thái</th>
           <th class="numeric">Giá</th>
           <th class="numeric">Tồn kho</th>
           <th>Cập nhật</th>
           <th><span class="sr-only">Thao tác</span></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="product in products"
          :key="product.id"
          tabindex="0"
          title="Bấm để xem chi tiết sản phẩm"
          @click="emit('view', product)"
          @keydown.enter="emit('view', product)"
          @keydown.space.prevent="emit('view', product)"
        >
          <td>
            <div class="product-cell">
              <span class="product-glyph">{{ product.name.slice(0, 1).toUpperCase() }}</span>
              <span>
                <strong>{{ product.name }}</strong>
                 <small>{{ product.description || 'Chưa có mô tả' }}</small>
              </span>
            </div>
          </td>
          <td><span class="code-label">{{ product.productCode }}</span></td>
          <td>
            <span class="status-label" :class="product.status.toLowerCase()">
               <i></i>{{ product.status === 'ACTIVE' ? 'Đang hoạt động' : product.status === 'LOW_STOCK' ? 'Sắp hết hàng' : 'Ngừng hoạt động' }}
            </span>
          </td>
           <td class="numeric price-cell">{{ formatCurrency(product.price, currency) }}</td>
          <td class="numeric">
            <span class="stock-value" :class="{ warning: product.stockQuantity < (product.minimumStock ?? lowStockThreshold) }">
               {{ formatNumber(product.stockQuantity) }}
               <small v-if="product.stockQuantity < (product.minimumStock ?? lowStockThreshold)">dưới mức tối thiểu</small>
            </span>
          </td>
          <td><span class="date-label">{{ formatDate(product.updatedAt, dateFormat) }}</span></td>
          <td>
            <div class="row-actions" @click.stop>
               <button class="icon-button" type="button" title="Sửa sản phẩm" aria-label="Sửa sản phẩm" @click="emit('edit', product)">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 5 4 4M4 20l3.2-.7L18.7 7.8a2.1 2.1 0 0 0-3-3L4.2 16.3 4 20Z" /></svg>
              </button>
               <button class="icon-button danger-icon" type="button" title="Xóa sản phẩm" aria-label="Xóa sản phẩm" @click="emit('remove', product)">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 13h10l1-13M9 7V4h6v3" /></svg>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
