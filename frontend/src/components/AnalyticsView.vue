<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAnalyticsStore } from '../stores/analytics'
import { useSettingsStore } from '../stores/settings'
import { formatCurrency, formatDate, formatNumber } from '../utils/formatters'
import type { ProductStatus } from '../types/product'

const store = useAnalyticsStore()
const settingsStore = useSettingsStore()
const { currency, dateFormat, lowStockThreshold } = storeToRefs(settingsStore)
const {
  overview,
  inventoryByStatus,
  topInventoryValue,
  lowStockProducts,
  stockDistribution,
  trends,
  loading,
  error,
} = storeToRefs(store)

const statusLabels: Record<ProductStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  LOW_STOCK: 'Sắp hết hàng',
  INACTIVE: 'Ngừng hoạt động',
}

const statusColors: Record<ProductStatus, string> = {
  ACTIVE: '#4f855f',
  LOW_STOCK: '#ef6b43',
  INACTIVE: '#9b968c',
}

const statusTotal = computed(() => inventoryByStatus.value.reduce((sum, item) => sum + item.productCount, 0))
const statusGradient = computed(() => {
  if (!statusTotal.value) return 'conic-gradient(#e6dfd2 0 100%)'
  let start = 0
  const segments = inventoryByStatus.value.map((item) => {
    const end = start + (item.productCount / statusTotal.value) * 100
    const segment = `${statusColors[item.status]} ${start}% ${end}%`
    start = end
    return segment
  })
  return `conic-gradient(${segments.join(', ')})`
})

const topValue = computed(() => Math.max(...topInventoryValue.value.map((item) => item.inventoryValue), 1))
const stockValue = computed(() => Math.max(...stockDistribution.value.map((item) => item.productCount), 1))
const trendValue = computed(() => Math.max(...(trends.value?.points || []).map((item) => item.totalInventoryValue), 1))

const emit = defineEmits<{
  viewProduct: [productId: number]
}>()

function statusLabel(status: ProductStatus) {
  return statusLabels[status]
}

function stockLabel(stockQuantity: number) {
  return stockQuantity === 0 ? 'Hết hàng' : `${formatNumber(stockQuantity)} sản phẩm`
}

function localizeError() {
  if (!error.value || error.value.status === 0) return 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Docker và thử lại.'
  if (error.value.status === 400) return 'Tham số phân tích không hợp lệ.'
  return 'Không thể tải dữ liệu phân tích. Vui lòng thử lại.'
}

onMounted(() => {
  if (!overview.value && !loading.value) store.fetchAnalytics()
})
</script>

<template>
  <section class="analytics-page" aria-labelledby="analytics-title">
    <div class="analytics-intro">
      <div>
        <p class="eyebrow">Không gian tồn kho / 02</p>
        <h1 id="analytics-title">Phân tích<span class="title-dot">.</span></h1>
        <p class="intro-copy">Một góc nhìn thực tế về số lượng, giá trị và những điểm cần bổ sung trong kho.</p>
      </div>
      <button class="button secondary-button analytics-refresh" type="button" :disabled="loading" @click="store.fetchAnalytics">
        <span :class="{ 'button-loader dark-loader': loading }">↻</span>
        {{ loading ? 'Đang cập nhật...' : 'Làm mới dữ liệu' }}
      </button>
    </div>

    <div v-if="error" class="page-alert" role="alert">
      <span class="alert-mark">!</span>
      <span><strong>Không thể tải phân tích.</strong> {{ localizeError() }}</span>
      <button class="text-button" type="button" @click="store.fetchAnalytics">Thử lại</button>
    </div>

    <div v-if="loading && !overview" class="analytics-loading" role="status" aria-label="Đang tải dữ liệu phân tích">
      <span class="loading-orbit"></span>
      <p>Đang tổng hợp dữ liệu tồn kho thực tế...</p>
    </div>

    <template v-else-if="overview">
      <section class="analytics-kpi-grid" aria-label="Chỉ số tổng quan">
        <article class="analytics-kpi analytics-kpi-primary">
          <span class="metric-label">Tổng sản phẩm</span>
          <strong>{{ formatNumber(overview.totalProducts) }}</strong>
          <span class="metric-foot">không tính sản phẩm đã xóa</span>
        </article>
        <article class="analytics-kpi">
          <span class="metric-label">Giá trị tồn kho</span>
          <strong>{{ formatCurrency(overview.totalInventoryValue, currency) }}</strong>
          <span class="metric-foot">giá × số lượng thực tế</span>
        </article>
        <article class="analytics-kpi">
          <span class="metric-label">Tổng số lượng</span>
          <strong>{{ formatNumber(overview.totalStockQuantity) }}</strong>
          <span class="metric-foot">đơn vị trong kho</span>
        </article>
        <article class="analytics-kpi">
          <span class="metric-label">Sắp hết hàng</span>
          <strong>{{ formatNumber(overview.lowStockProducts) }}</strong>
          <span class="metric-foot"><i class="metric-dot orange"></i>{{ overview.lowStockRate }}% tổng sản phẩm</span>
        </article>
        <article class="analytics-kpi">
          <span class="metric-label">Đang hoạt động</span>
          <strong>{{ formatNumber(overview.activeProducts) }}</strong>
          <span class="metric-foot"><i class="metric-dot green"></i>đang kinh doanh</span>
        </article>
        <article class="analytics-kpi">
          <span class="metric-label">Hết hàng</span>
          <strong>{{ formatNumber(overview.outOfStockProducts) }}</strong>
          <span class="metric-foot">cần được bổ sung ngay</span>
        </article>
      </section>

      <section class="analytics-grid analytics-grid-top">
        <article class="analytics-card status-card">
          <div class="analytics-card-heading">
            <div><p class="eyebrow">Phân bổ danh mục</p><h2>Trạng thái sản phẩm</h2></div>
            <span class="record-count">{{ formatNumber(statusTotal) }} sản phẩm</span>
          </div>
          <div v-if="inventoryByStatus.length" class="status-chart">
            <div class="status-donut" :style="{ background: statusGradient }"><div><strong>{{ formatNumber(statusTotal) }}</strong><span>sản phẩm</span></div></div>
            <div class="status-legend">
              <div v-for="item in inventoryByStatus" :key="item.status" class="legend-row">
                <span class="legend-swatch" :style="{ background: statusColors[item.status] }"></span>
                <span>{{ statusLabel(item.status) }}</span>
                <strong>{{ formatNumber(item.productCount) }}</strong>
              </div>
            </div>
          </div>
          <div v-else class="analytics-empty compact-empty">Chưa có dữ liệu trạng thái.</div>
        </article>

        <article class="analytics-card distribution-card">
          <div class="analytics-card-heading">
            <div><p class="eyebrow">Mật độ tồn kho</p><h2>Phân bổ số lượng</h2></div>
          </div>
          <div v-if="stockDistribution.length" class="distribution-list">
            <div v-for="item in stockDistribution" :key="item.bucket" class="distribution-row">
              <div class="distribution-copy"><span>{{ item.label }}</span><strong>{{ formatNumber(item.productCount) }} sản phẩm</strong></div>
              <div class="distribution-track"><i :style="{ width: `${(item.productCount / stockValue) * 100}%` }"></i></div>
            </div>
          </div>
          <div v-else class="analytics-empty compact-empty">Chưa có dữ liệu phân bổ.</div>
        </article>
      </section>

      <section class="analytics-grid analytics-grid-main">
        <article class="analytics-card wide-card">
          <div class="analytics-card-heading">
            <div><p class="eyebrow">Giá trị đang nằm trong kho</p><h2>Top sản phẩm</h2></div>
            <span class="record-count">{{ formatNumber(topInventoryValue.length) }} sản phẩm</span>
          </div>
          <div v-if="topInventoryValue.length" class="analytics-table-wrap">
            <table class="analytics-table">
              <thead><tr><th>Sản phẩm</th><th>Trạng thái</th><th class="numeric">Tồn kho</th><th class="numeric">Giá trị tồn</th></tr></thead>
              <tbody>
                <tr v-for="item in topInventoryValue" :key="item.productId" tabindex="0" title="Bấm để xem chi tiết sản phẩm" @click="emit('viewProduct', item.productId)" @keydown.enter="emit('viewProduct', item.productId)" @keydown.space.prevent="emit('viewProduct', item.productId)">
                  <td><div class="analytics-product"><span class="product-glyph">{{ item.name.slice(0, 1).toUpperCase() }}</span><span><strong>{{ item.name }}</strong><small>{{ item.productCode }}</small></span></div></td>
                  <td><span class="status-label" :class="item.status.toLowerCase()"><i></i>{{ statusLabel(item.status) }}</span></td>
                  <td class="numeric">{{ stockLabel(item.stockQuantity) }}</td>
                  <td class="numeric"><div class="value-cell"><span>{{ formatCurrency(item.inventoryValue, currency) }}</span><i><b :style="{ width: `${(item.inventoryValue / topValue) * 100}%` }"></b></i></div></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="analytics-empty">Chưa có sản phẩm để xếp hạng giá trị tồn kho.</div>
        </article>

        <article class="analytics-card low-stock-card">
          <div class="analytics-card-heading"><div><p class="eyebrow">Cần chú ý</p><h2>Sắp hết hàng</h2></div><span class="record-count">ngưỡng riêng / {{ lowStockThreshold }}</span></div>
          <div v-if="lowStockProducts.length" class="low-stock-list">
            <div v-for="item in lowStockProducts" :key="item.productId" class="low-stock-row" role="button" tabindex="0" title="Bấm để xem chi tiết sản phẩm" @click="emit('viewProduct', item.productId)" @keydown.enter="emit('viewProduct', item.productId)" @keydown.space.prevent="emit('viewProduct', item.productId)">
              <span class="low-stock-index">{{ item.stockQuantity === 0 ? '!' : item.stockQuantity }}</span>
              <div><strong>{{ item.name }}</strong><small>{{ item.stockQuantity === 0 ? 'Hết hàng' : `Còn ${formatNumber(item.stockQuantity)} sản phẩm` }}</small></div>
              <span class="low-stock-value">{{ formatCurrency(item.inventoryValue, currency) }}</span>
            </div>
          </div>
          <div v-else class="analytics-empty compact-empty">Kho đang ở mức an toàn.</div>
        </article>
      </section>

      <section class="analytics-card trend-card">
        <div class="analytics-card-heading"><div><p class="eyebrow">Lịch sử tồn kho</p><h2>Xu hướng giá trị theo thời gian</h2></div><span class="record-count">Theo ngày</span></div>
        <div v-if="trends?.status === 'READY' && trends.points.length" class="trend-content">
          <div class="trend-chart" aria-label="Biểu đồ xu hướng giá trị tồn kho">
            <div v-for="point in trends.points" :key="point.period" class="trend-bar-group"><span class="trend-tooltip">{{ formatCurrency(point.totalInventoryValue, currency) }}</span><i :style="{ height: `${(point.totalInventoryValue / trendValue) * 100}%` }"></i><small>{{ formatDate(point.period, dateFormat) }}</small></div>
          </div>
          <div class="trend-caption"><span>Giá trị tồn kho được tổng hợp từ các snapshot thực tế.</span><strong>{{ formatCurrency(trends.points[trends.points.length - 1].totalInventoryValue, currency) }}<small> kỳ gần nhất</small></strong></div>
        </div>
        <div v-else class="trend-empty"><span class="trend-empty-mark">∿</span><div><strong>Chưa đủ dữ liệu lịch sử để hiển thị xu hướng.</strong><p>Hệ thống sẽ tự động ghi một snapshot tồn kho mỗi ngày. Biểu đồ sẽ xuất hiện khi có ít nhất hai ngày dữ liệu thực tế.</p></div></div>
      </section>
    </template>

    <footer class="app-footer"><span>Phân tích tồn kho</span><span>Dữ liệu được tổng hợp trực tiếp từ hệ thống</span></footer>
  </section>
</template>
