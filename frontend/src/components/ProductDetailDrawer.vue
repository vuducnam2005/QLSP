<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Product, ProductStatus } from '../types/product'
import { formatCurrency, formatDate, formatNumber } from '../utils/formatters'

const props = defineProps<{
  open: boolean
  product: Product | null
  currency: 'VND' | 'USD'
  dateFormat: 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD'
  lowStockThreshold: number
}>()

const emit = defineEmits<{
  close: []
  edit: [product: Product]
}>()

const imageFailed = ref(false)

watch(
  () => props.product?.imageUrl,
  () => {
    imageFailed.value = false
  },
)

const inventoryValue = computed(() => {
  if (!props.product) return 0
  return props.product.price * props.product.stockQuantity
})

const profitPerUnit = computed(() => {
  if (props.product?.costPrice == null) return null
  return props.product.price - props.product.costPrice
})

const profitMargin = computed(() => {
  if (props.product?.costPrice == null || !props.product.price) return null
  return (profitPerUnit.value! / props.product.price) * 100
})

const stockState = computed(() => {
  if (!props.product) return 'empty'
  if (props.product.stockQuantity <= 0) return 'empty'
  const minimumStock = props.product.minimumStock ?? props.lowStockThreshold
  if (props.product.stockQuantity < minimumStock) return 'low'
  return 'available'
})

const stockStateLabel = computed(() => {
  if (stockState.value === 'empty') return 'Hết hàng'
  if (stockState.value === 'low') return 'Cần nhập thêm'
  return 'Mức tồn an toàn'
})

const statusLabels: Record<ProductStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  LOW_STOCK: 'Sắp hết hàng',
  INACTIVE: 'Ngừng hoạt động',
}

function statusLabel(status: ProductStatus) {
  return statusLabels[status]
}

function dateLabel(value: string | null | undefined) {
  return value ? formatDate(value, props.dateFormat) : 'Chưa có dữ liệu'
}
</script>

<template>
  <Transition name="fade">
    <div v-if="open" class="drawer-backdrop" @click.self="emit('close')">
      <aside class="product-detail-drawer" role="dialog" aria-modal="true" aria-labelledby="product-detail-title">
        <div class="drawer-topline">
          <span class="eyebrow">Chi tiết sản phẩm</span>
          <button class="icon-button" type="button" aria-label="Đóng chi tiết sản phẩm" @click="emit('close')">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" /></svg>
          </button>
        </div>

        <template v-if="product">
          <header class="product-detail-hero">
            <div class="product-detail-visual">
              <img v-if="product.imageUrl && !imageFailed" :src="product.imageUrl" :alt="product.name" @error="imageFailed = true" />
              <span v-else class="product-detail-glyph">{{ product.name.slice(0, 1).toUpperCase() }}</span>
            </div>
            <div>
              <span class="code-label">{{ product.productCode }}</span>
              <h2 id="product-detail-title">{{ product.name }}</h2>
              <span class="status-label" :class="product.status.toLowerCase()"><i></i>{{ statusLabel(product.status) }}</span>
            </div>
          </header>

          <section class="detail-section" aria-labelledby="detail-summary-title">
            <div class="detail-section-heading">
              <p class="eyebrow">Tóm tắt nhanh</p>
              <h3 id="detail-summary-title">Thông tin tồn kho</h3>
            </div>
            <div class="detail-stat-grid">
              <article class="detail-stat-card detail-stat-card-accent">
                <span>Giá bán</span>
                <strong>{{ formatCurrency(product.price, currency) }}</strong>
                <small>mỗi sản phẩm</small>
              </article>
              <article class="detail-stat-card">
                <span>Giá trị tồn</span>
                <strong>{{ formatCurrency(inventoryValue, currency) }}</strong>
                <small>giá bán × tồn kho</small>
              </article>
              <article class="detail-stat-card">
                <span>Số lượng tồn</span>
                <strong>{{ formatNumber(product.stockQuantity) }}</strong>
                <small>{{ product.minimumStock == null ? 'Theo ngưỡng chung' : `Tối thiểu ${formatNumber(product.minimumStock)}` }}</small>
              </article>
            </div>
          </section>

          <section class="detail-section" aria-labelledby="detail-description-title">
            <div class="detail-section-heading">
              <p class="eyebrow">Mô tả</p>
              <h3 id="detail-description-title">Về sản phẩm</h3>
            </div>
            <p class="product-detail-description">{{ product.description || 'Sản phẩm này chưa có mô tả. Hãy thêm mô tả để đội ngũ dễ dàng nhận biết công dụng và đặc điểm.' }}</p>
          </section>

          <section class="detail-section" aria-labelledby="detail-meta-title">
            <div class="detail-section-heading">
              <p class="eyebrow">Thông tin hệ thống</p>
              <h3 id="detail-meta-title">Chi tiết bản ghi</h3>
            </div>
            <dl class="detail-meta-grid">
              <div><dt>Mã sản phẩm</dt><dd>{{ product.productCode }}</dd></div>
              <div><dt>Phiên bản</dt><dd>v{{ product.version }}</dd></div>
              <div><dt>Ngày tạo</dt><dd>{{ dateLabel(product.createdAt) }}</dd></div>
              <div><dt>Cập nhật gần nhất</dt><dd>{{ dateLabel(product.updatedAt) }}</dd></div>
              <div><dt>Ngưỡng cảnh báo</dt><dd>{{ product.minimumStock == null ? `${formatNumber(lowStockThreshold)} sản phẩm` : 'Theo sản phẩm' }}</dd></div>
              <div><dt>Tồn kho tối thiểu</dt><dd>{{ product.minimumStock == null ? 'Theo cài đặt chung' : `${formatNumber(product.minimumStock)} sản phẩm` }}</dd></div>
              <div><dt>Tình trạng tồn</dt><dd :class="`detail-value-${stockState}`">{{ stockStateLabel }}</dd></div>
              <div><dt>Danh mục</dt><dd>{{ product.category || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Thương hiệu</dt><dd>{{ product.brand || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Nhà cung cấp</dt><dd>{{ product.supplier || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Đơn vị tính</dt><dd>{{ product.unit || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Vị trí kho</dt><dd>{{ product.warehouseLocation || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Bảo hành</dt><dd>{{ product.warrantyMonths == null ? 'Chưa cập nhật' : `${product.warrantyMonths} tháng` }}</dd></div>
              <div><dt>Mã vạch</dt><dd>{{ product.barcode || 'Chưa cập nhật' }}</dd></div>
              <div><dt>Giá nhập</dt><dd>{{ product.costPrice == null ? 'Chưa cập nhật' : formatCurrency(product.costPrice, currency) }}</dd></div>
              <div><dt>Lợi nhuận / sản phẩm</dt><dd>{{ profitPerUnit == null ? 'Chưa cập nhật' : formatCurrency(profitPerUnit, currency) }}</dd></div>
              <div><dt>Biên lợi nhuận</dt><dd>{{ profitMargin == null ? 'Chưa cập nhật' : `${profitMargin.toFixed(1)}%` }}</dd></div>
            </dl>
          </section>

          <div class="detail-actions">
            <button class="button secondary-button" type="button" @click="emit('close')">Đóng</button>
            <button class="button primary-button" type="button" @click="emit('edit', product)">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 5 4 4M4 20l3.2-.7L18.7 7.8a2.1 2.1 0 0 0-3-3L4.2 16.3 4 20Z" /></svg>
              Chỉnh sửa sản phẩm
            </button>
          </div>
        </template>
        <div v-else class="detail-empty">Không tìm thấy thông tin sản phẩm.</div>
      </aside>
    </div>
  </Transition>
</template>
