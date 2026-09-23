<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import ConfirmDialog from './components/ConfirmDialog.vue'
import ProductDrawer from './components/ProductDrawer.vue'
import ProductDetailDrawer from './components/ProductDetailDrawer.vue'
import ProductTable from './components/ProductTable.vue'
import AnalyticsView from './components/AnalyticsView.vue'
import SettingsView from './components/SettingsView.vue'
import LoginView from './components/LoginView.vue'
import { ApiError } from './api/http'
import { setUnauthorizedHandler } from './api/http'
import { useAuthStore } from './stores/auth'
import { useAnalyticsStore } from './stores/analytics'
import { useProductsStore } from './stores/products'
import { useSettingsStore } from './stores/settings'
import type { Product, ProductCreatePayload, ProductUpdatePayload } from './types/product'
import { formatCurrency, formatNumber } from './utils/formatters'
import { getProduct, getProducts } from './api/products'

const store = useProductsStore()
const analyticsStore = useAnalyticsStore()
const authStore = useAuthStore()
const settingsStore = useSettingsStore()
const { items, loading, saving, error, page, filters, activeCount, lowStockCount, inactiveCount } = storeToRefs(store)
const { user, authenticated, initialized: authInitialized, loading: authLoading, error: authError, notice: authNotice } = storeToRefs(authStore)
const searchDraft = ref('')
const statusDraft = ref('')
const sortDraft = ref('updatedAt,desc')
const drawerOpen = ref(false)
const selectedProduct = ref<Product | null>(null)
const detailOpen = ref(false)
const detailProduct = ref<Product | null>(null)
const productToRemove = ref<Product | null>(null)
const toast = reactive({ visible: false, tone: 'success' as 'success' | 'error', title: '', detail: '' })

const searchFieldRef = ref<HTMLElement | null>(null)
const suggestions = ref<Product[]>([])
const suggestionsLoading = ref(false)
const showSuggestions = ref(false)
const highlightedIndex = ref(-1)
const masterProducts = ref<Product[]>([])
const activeView = ref<'products' | 'analytics' | 'settings'>('products')
const accountMenuOpen = ref(false)

let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null
let suggestionsDebounceTimer: ReturnType<typeof setTimeout> | null = null

watch(items, (newItems) => {
  if (!filters.value.keyword && !filters.value.status && newItems.length > 0) {
    masterProducts.value = [...newItems]
  }
}, { immediate: true })

const displayedProducts = computed(() => {
  const query = searchDraft.value.trim().toLowerCase()
  const st = statusDraft.value

  if (query || st) {
    const source = masterProducts.value.length > 0 ? masterProducts.value : items.value
    let filtered = source.filter((p) => {
      const matchStatus = !st || p.status === st
      const matchQuery =
        !query ||
        p.name.toLowerCase().includes(query) ||
        p.productCode.toLowerCase().includes(query)
      return matchStatus && matchQuery
    })

    const [field, direction] = sortDraft.value.split(',') as [keyof Product, 'asc' | 'desc']
    filtered = [...filtered].sort((a, b) => {
      const valA = a[field] ?? ''
      const valB = b[field] ?? ''
      if (valA < valB) return direction === 'asc' ? -1 : 1
      if (valA > valB) return direction === 'asc' ? 1 : -1
      return 0
    })

    if (filtered.length > 0) return filtered
    if (items.value.length > 0 && filters.value.keyword.toLowerCase() === query) {
      return items.value
    }
    return filtered
  }

  return items.value
})

const hasFilters = computed(() => Boolean(
  searchDraft.value.trim() ||
  statusDraft.value ||
  sortDraft.value !== 'updatedAt,desc'
))

const pageLabel = computed(() => {
  if (!page.value.totalElements) return '0 sản phẩm'
  const start = page.value.pageNo * page.value.pageSize + 1
  const end = Math.min((page.value.pageNo + 1) * page.value.pageSize, page.value.totalElements)
  return `${formatNumber(start)}-${formatNumber(end)} trên ${formatNumber(page.value.totalElements)}`
})

const displayCountLabel = computed(() => {
  if (searchDraft.value.trim() || statusDraft.value) {
    return `${displayedProducts.value.length} sản phẩm phù hợp`
  }
  return pageLabel.value
})

function localizeApiError(apiError: ApiError) {
  if (apiError.status === 0) return 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Docker và thử lại.'
  if (apiError.status === 409) {
    if (apiError.message.includes(':')) return `Mã sản phẩm ${apiError.message.split(':').slice(1).join(':').trim()} đã tồn tại.`
    return 'Sản phẩm đã được thay đổi bởi một yêu cầu khác. Dữ liệu mới nhất đã được tải lại, hãy thử lại.'
  }
  if (apiError.status === 404) return 'Không tìm thấy sản phẩm.'
  if (apiError.status === 400 && apiError.details) {
    const labels: Record<string, string> = {
      productCode: 'Mã sản phẩm',
      name: 'Tên sản phẩm',
      price: 'Giá bán',
      stockQuantity: 'Số lượng tồn kho',
      status: 'Trạng thái',
      version: 'Phiên bản',
      warrantyMonths: 'Thời hạn bảo hành',
      costPrice: 'Giá nhập',
      minimumStock: 'Tồn kho tối thiểu',
      barcode: 'Mã vạch',
      imageUrl: 'Ảnh sản phẩm',
    }
    return Object.entries(apiError.details)
      .map(([field, message]) => {
        if (message.includes('must follow the format')) return `Mã sản phẩm phải có định dạng ${settingsStore.productCodePrefix}0000.`
        if (message.includes('is required')) return `${labels[field] || field} là bắt buộc.`
        if (message.includes('greater than zero')) return 'Giá bán phải lớn hơn 0.'
        if (message.includes('greater than or equal to 0')) return `${labels[field] || field} không được nhỏ hơn 0.`
        return `${labels[field] || field}: dữ liệu không hợp lệ.`
      })
      .join(' ')
  }
  return 'Đã xảy ra lỗi. Vui lòng thử lại.'
}

const drawerError = computed(() => {
  if (!error.value) return ''
  return localizeApiError(error.value)
})

function notify(title: string, detail: string, tone: 'success' | 'error' = 'success') {
  toast.title = title
  toast.detail = detail
  toast.tone = tone
  toast.visible = true
  window.setTimeout(() => (toast.visible = false), 4200)
}

async function submitLogin(username: string, password: string) {
  try {
    await authStore.login(username, password)
    await store.fetchProducts()
  } catch {
    // The auth store provides a concise message for the login form.
  }
}

async function logout() {
  accountMenuOpen.value = false
  await authStore.logout()
}

function applyFilters() {
  store.setFilters({
    keyword: searchDraft.value.trim(),
    status: statusDraft.value as typeof filters.value.status,
    sort: sortDraft.value as typeof filters.value.sort,
  })
  store.fetchProducts()
}

function onFilterChange() {
  applyFilters()
}

function updateSuggestions(rawQuery: string) {
  const query = rawQuery.trim().toLowerCase()
  if (!query) {
    suggestions.value = []
    showSuggestions.value = false
    highlightedIndex.value = -1
    return
  }

  // Gợi ý ngay lập tức từ dữ liệu hiện có (0ms delay)
  const source = masterProducts.value.length > 0 ? masterProducts.value : items.value
  const localMatches = source
    .filter((p) => p.name.toLowerCase().includes(query) || p.productCode.toLowerCase().includes(query))
    .slice(0, 6)

  suggestions.value = localMatches
  showSuggestions.value = true
  highlightedIndex.value = -1

  // Đồng thời tải thêm từ backend
  if (suggestionsDebounceTimer) clearTimeout(suggestionsDebounceTimer)
  suggestionsDebounceTimer = setTimeout(async () => {
    try {
      const response = await getProducts({
        page: 0,
        size: 6,
        sort: 'updatedAt,desc',
        keyword: query,
      })
      if (searchDraft.value.trim().toLowerCase() === query) {
        suggestions.value = response.data.items
      }
    } catch {
      // Giữ gợi ý cục bộ
    }
  }, 180)
}

function onSearchInput(event: Event) {
  const target = event.target as HTMLInputElement
  const value = target.value
  searchDraft.value = value

  // Tức thì hiển thị gợi ý
  updateSuggestions(value)

  // Đồng bộ với backend qua debounce 200ms
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    store.setFilters({
      keyword: value.trim(),
      status: statusDraft.value as typeof filters.value.status,
      sort: sortDraft.value as typeof filters.value.sort,
    })
    store.fetchProducts()
  }, 200)
}

function onSearchFocus() {
  if (searchDraft.value.trim()) {
    updateSuggestions(searchDraft.value)
  }
}

function clearSearch() {
  searchDraft.value = ''
  suggestions.value = []
  showSuggestions.value = false
  highlightedIndex.value = -1
  applyFilters()
}

function selectSuggestion(product: Product) {
  searchDraft.value = product.name
  showSuggestions.value = false
  highlightedIndex.value = -1
  applyFilters()
}

function onSearchKeydown(event: KeyboardEvent) {
  if (!showSuggestions.value || suggestions.value.length === 0) {
    if (event.key === 'Enter') {
      event.preventDefault()
      applyFilters()
    }
    return
  }

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    highlightedIndex.value = (highlightedIndex.value + 1) % suggestions.value.length
  } else if (event.key === 'ArrowUp') {
    event.preventDefault()
    highlightedIndex.value =
      highlightedIndex.value <= 0 ? suggestions.value.length - 1 : highlightedIndex.value - 1
  } else if (event.key === 'Enter') {
    event.preventDefault()
    if (highlightedIndex.value >= 0 && highlightedIndex.value < suggestions.value.length) {
      selectSuggestion(suggestions.value[highlightedIndex.value])
    } else {
      showSuggestions.value = false
      applyFilters()
    }
  } else if (event.key === 'Escape') {
    showSuggestions.value = false
  }
}

function handleClickOutside(event: MouseEvent) {
  if (searchFieldRef.value && !searchFieldRef.value.contains(event.target as Node)) {
    showSuggestions.value = false
  }
}

function highlightMatch(text: string, query: string) {
  if (!query || !query.trim()) return [{ text, match: false }]
  const q = query.trim()
  const regex = new RegExp(`(${q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  const parts = text.split(regex)
  return parts
    .filter((part) => part.length > 0)
    .map((part) => ({
      text: part,
      match: part.toLowerCase() === q.toLowerCase(),
    }))
}

function resetFilters() {
  searchDraft.value = ''
  statusDraft.value = ''
  sortDraft.value = 'updatedAt,desc'
  suggestions.value = []
  showSuggestions.value = false
  highlightedIndex.value = -1
  applyFilters()
}

function openCreate() {
  store.clearError()
  detailOpen.value = false
  selectedProduct.value = null
  drawerOpen.value = true
}

function openDetail(product: Product) {
  detailProduct.value = product
  detailOpen.value = true
}

async function openDetailById(productId: number) {
  try {
    const response = await getProduct(productId)
    openDetail(response.data)
  } catch (caught) {
    if (caught instanceof ApiError) notify('Không thể mở chi tiết', 'Sản phẩm không còn tồn tại hoặc chưa thể tải dữ liệu.', 'error')
  }
}

function openEdit(product: Product) {
  store.clearError()
  detailOpen.value = false
  selectedProduct.value = product
  drawerOpen.value = true
}

function openDelete(product: Product) {
  store.clearError()
  productToRemove.value = product
}

async function saveProduct(payload: ProductCreatePayload | ProductUpdatePayload) {
  try {
    if (selectedProduct.value) {
      await store.update(selectedProduct.value.id, payload as ProductUpdatePayload)
      await analyticsStore.fetchAnalytics()
       notify('Đã cập nhật sản phẩm', 'Danh mục sản phẩm đã được đồng bộ.')
    } else {
      await store.create(payload as ProductCreatePayload)
      await analyticsStore.fetchAnalytics()
       notify('Đã tạo sản phẩm', 'Sản phẩm mới đã được thêm vào danh mục.')
    }
    drawerOpen.value = false
  } catch (caught) {
    if (caught instanceof ApiError && caught.status === 409 && selectedProduct.value) {
      try {
        selectedProduct.value = await store.refresh(selectedProduct.value.id)
        notify('Đã tải dữ liệu mới nhất', 'Hãy kiểm tra lại thông tin trước khi lưu.')
      } catch {
        notify('Không thể tải lại', 'Không thể tải phiên bản mới nhất của sản phẩm.', 'error')
      }
    } else if (!(caught instanceof ApiError)) {
      notify('Lưu thất bại', 'Máy chủ không thể lưu sản phẩm này.', 'error')
    }
  }
}

async function confirmDelete() {
  if (!productToRemove.value) return
  try {
    await store.remove(productToRemove.value.id)
    await analyticsStore.fetchAnalytics()
    notify('Đã xóa sản phẩm', 'Sản phẩm đã được chuyển vào trạng thái xóa mềm.')
    productToRemove.value = null
  } catch (caught) {
    if (caught instanceof ApiError) notify('Xóa thất bại', 'Không thể xóa sản phẩm. Vui lòng thử lại.', 'error')
  }
}

async function onSettingsSaved(detail: string) {
  await Promise.all([store.fetchProducts(), analyticsStore.fetchAnalytics()])
  notify('Đã lưu cài đặt', detail)
}

async function changePage(nextPage: number) {
  if (nextPage < 0 || nextPage >= page.value.totalPages) return
  store.setPage(nextPage)
  await store.fetchProducts()
}

watch(drawerOpen, (open) => {
  document.body.classList.toggle('drawer-open', open || detailOpen.value)
})

watch(detailOpen, (open) => {
  document.body.classList.toggle('drawer-open', open || drawerOpen.value)
})

function handleGlobalKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  if (productToRemove.value) {
    productToRemove.value = null
  } else if (drawerOpen.value && !saving.value) {
    drawerOpen.value = false
  } else if (detailOpen.value) {
    detailOpen.value = false
  }
}

onMounted(async () => {
  searchDraft.value = filters.value.keyword
  setUnauthorizedHandler(authStore.handleUnauthorized)
  window.addEventListener('keydown', handleGlobalKeydown)
  document.addEventListener('click', handleClickOutside)
  await authStore.initializeAuth()
  if (authenticated.value) {
    try {
      await settingsStore.fetchSettings()
    } catch {
      // The settings screen exposes the detailed error if the request fails.
    }
    await store.fetchProducts()
  }
})

onBeforeUnmount(() => {
  setUnauthorizedHandler(null)
  window.removeEventListener('keydown', handleGlobalKeydown)
  document.removeEventListener('click', handleClickOutside)
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  if (suggestionsDebounceTimer) clearTimeout(suggestionsDebounceTimer)
})
</script>

<template>
  <div v-if="!authInitialized" class="app-boot-screen">
    <div class="boot-mark">P<span>.</span></div>
    <span class="loading-orbit"></span>
    <p>Đang mở workspace...</p>
  </div>

  <LoginView
    v-else-if="!authenticated"
    :loading="authLoading"
    :error="authError"
    :notice="authNotice"
    @submit="submitLogin"
  />

  <template v-else>
  <div class="app-shell">
    <aside class="side-rail">
      <div class="brand-mark" aria-label="Danh mục sản phẩm">P<span>.</span></div>
      <nav class="side-nav" aria-label="Điều hướng chính">
        <button class="nav-item" :class="{ active: activeView === 'products' }" type="button" @click="activeView = 'products'"><span class="nav-icon">▦</span><span>Sản phẩm</span></button>
        <button class="nav-item" :class="{ active: activeView === 'analytics' }" type="button" @click="activeView = 'analytics'"><span class="nav-icon">◒</span><span>Phân tích</span></button>
        <button class="nav-item" :class="{ active: activeView === 'settings' }" type="button" @click="activeView = 'settings'"><span class="nav-icon">⌘</span><span>Cài đặt</span></button>
      </nav>
      <div class="rail-footer">
        <span class="connection-dot"></span>
        <span>Đã kết nối API</span>
      </div>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <div class="breadcrumb"><span>{{ settingsStore.settings?.workspaceName || 'Không gian làm việc' }}</span><b>/</b><strong>{{ activeView === 'products' ? 'Sản phẩm' : activeView === 'analytics' ? 'Phân tích' : 'Cài đặt' }}</strong></div>
        <div class="topbar-actions">
          <span class="live-status"><i></i>Dữ liệu trực tiếp</span>
          <div class="account-menu-wrap">
            <button class="avatar-button" type="button" title="Tài khoản" aria-label="Mở menu tài khoản" @click="accountMenuOpen = !accountMenuOpen">
              {{ user?.username.slice(0, 2).toUpperCase() || 'AD' }}
            </button>
            <div v-if="accountMenuOpen" class="account-menu">
              <div class="account-menu-heading">
                <span class="account-menu-avatar">{{ user?.username.slice(0, 2).toUpperCase() || 'AD' }}</span>
                <span><strong>{{ user?.username || 'Admin' }}</strong><small>Quản trị viên</small></span>
              </div>
              <div class="account-menu-divider"></div>
              <button type="button" class="account-menu-action" @click="logout">
                <span>↪</span> Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </header>

      <template v-if="activeView === 'products'">
      <section class="page-intro">
        <div>
          <p class="eyebrow">Không gian tồn kho / 01</p>
          <h1>Danh mục sản phẩm<span class="title-dot">.</span></h1>
          <p class="intro-copy">Quản lý danh mục gọn gàng, chính xác và sẵn sàng cho đơn hàng tiếp theo.</p>
        </div>
        <button class="button primary-button add-button" type="button" @click="openCreate">
          <span class="button-plus">+</span> Thêm sản phẩm
        </button>
      </section>

      <section class="metrics-grid" aria-label="Chỉ số sản phẩm">
        <article class="metric-card accent-card">
          <span class="metric-label">Tổng sản phẩm</span>
          <strong>{{ page.totalElements }}</strong>
          <span class="metric-foot">trong danh mục hiện tại</span>
        </article>
        <article class="metric-card">
          <span class="metric-label">Đang hoạt động</span>
          <strong>{{ activeCount }}</strong>
          <span class="metric-foot"><i class="metric-dot green"></i>hiển thị ở trang này</span>
        </article>
        <article class="metric-card">
          <span class="metric-label">Sắp hết hàng</span>
          <strong>{{ lowStockCount }}</strong>
          <span class="metric-foot"><i class="metric-dot orange"></i>tồn kho dưới {{ settingsStore.lowStockThreshold }} hoặc được đánh dấu</span>
        </article>
        <article class="metric-card">
          <span class="metric-label">Ngừng hoạt động</span>
          <strong>{{ inactiveCount }}</strong>
          <span class="metric-foot">sản phẩm đang tạm dừng</span>
        </article>
      </section>

      <section class="ledger-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Danh mục sản phẩm</p>
            <h2>Tất cả sản phẩm</h2>
          </div>
          <span class="record-count">{{ displayCountLabel }}</span>
        </div>

        <div class="filter-bar">
          <div ref="searchFieldRef" class="search-field">
            <svg viewBox="0 0 24 24" aria-hidden="true" class="search-icon"><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 5 5" /></svg>
            <input
              :value="searchDraft"
              type="text"
              placeholder="Tìm theo tên hoặc mã sản phẩm..."
              autocomplete="off"
              @input="onSearchInput"
              @compositionupdate="onSearchInput"
              @compositionend="onSearchInput"
              @focus="onSearchFocus"
              @keydown="onSearchKeydown"
            />
            <button
              v-if="searchDraft"
              class="search-clear-btn"
              type="button"
              title="Xóa tìm kiếm"
              aria-label="Xóa tìm kiếm"
              @click="clearSearch"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" /></svg>
            </button>

            <!-- Autocomplete Suggestions Dropdown -->
            <div v-if="showSuggestions" class="search-suggestions-dropdown">
              <div v-if="suggestionsLoading" class="suggestions-loading">
                <span class="button-loader suggestion-spinner"></span>
                <span>Đang tìm sản phẩm...</span>
              </div>
              <template v-else>
                <div v-if="suggestions.length > 0" class="suggestions-header">
                  <span>GỢI Ý SẢN PHẨM</span>
                  <small>{{ suggestions.length }} sản phẩm phù hợp</small>
                </div>
                <div v-if="suggestions.length > 0" class="suggestions-list" role="listbox">
                  <div
                    v-for="(item, idx) in suggestions"
                    :key="item.id"
                    class="suggestion-item"
                    :class="{ active: idx === highlightedIndex }"
                    role="option"
                    :aria-selected="idx === highlightedIndex"
                    @mousedown="selectSuggestion(item)"
                    @mouseenter="highlightedIndex = idx"
                  >
                    <span class="suggestion-glyph">{{ item.name.slice(0, 1).toUpperCase() }}</span>
                    <div class="suggestion-info">
                      <div class="suggestion-name">
                        <span
                          v-for="(chunk, cIdx) in highlightMatch(item.name, searchDraft)"
                          :key="cIdx"
                          :class="{ 'match-highlight': chunk.match }"
                        >{{ chunk.text }}</span>
                      </div>
                      <div class="suggestion-meta">
                        <span class="suggestion-code">{{ item.productCode }}</span>
                        <span class="suggestion-dot">•</span>
                        <span class="suggestion-price">{{ formatCurrency(item.price, settingsStore.currency) }}</span>
                        <span class="suggestion-dot">•</span>
                        <span class="suggestion-stock">Tồn kho: {{ item.stockQuantity }}</span>
                      </div>
                    </div>
                    <div class="suggestion-right">
                      <span class="status-badge" :class="item.status.toLowerCase()">
                        {{ item.status === 'ACTIVE' ? 'Hoạt động' : item.status === 'LOW_STOCK' ? 'Sắp hết' : 'Tạm dừng' }}
                      </span>
                      <button
                        class="suggestion-edit-btn"
                        type="button"
                        title="Chỉnh sửa sản phẩm này"
                        aria-label="Chỉnh sửa sản phẩm này"
                        @mousedown.stop="openEdit(item)"
                      >
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 5 4 4M4 20l3.2-.7L18.7 7.8a2.1 2.1 0 0 0-3-3L4.2 16.3 4 20Z" /></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <div v-else class="suggestions-empty">
                  <span>Không tìm thấy sản phẩm nào có từ khóa "<strong>{{ searchDraft }}</strong>"</span>
                </div>
              </template>
            </div>
          </div>

          <select v-model="statusDraft" aria-label="Lọc theo trạng thái" @change="onFilterChange">
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="LOW_STOCK">Sắp hết hàng</option>
            <option value="INACTIVE">Ngừng hoạt động</option>
          </select>

          <select v-model="sortDraft" aria-label="Sắp xếp sản phẩm" @change="onFilterChange">
            <option value="updatedAt,desc">Cập nhật gần đây</option>
            <option value="createdAt,asc">Cũ nhất trước</option>
            <option value="name,asc">Tên A-Z</option>
            <option value="price,desc">Giá cao nhất</option>
            <option value="stockQuantity,asc">Tồn kho thấp nhất</option>
          </select>

          <button
            class="reset-filter-button"
            :class="{ 'has-active-filters': hasFilters }"
            type="button"
            title="Đặt lại tất cả bộ lọc"
            @click="resetFilters"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true" class="reset-icon">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
            <span>Xóa bộ lọc</span>
          </button>
        </div>

        <div v-if="error && !drawerOpen" class="page-alert" role="alert">
          <span class="alert-mark">!</span>
          <span><strong>Không thể tải danh mục.</strong> {{ localizeApiError(error) }}</span>
          <button class="text-button" type="button" @click="store.fetchProducts">Thử lại</button>
        </div>

        <ProductTable :products="displayedProducts" :loading="loading && items.length === 0" :currency="settingsStore.currency" :date-format="settingsStore.dateFormat" :low-stock-threshold="settingsStore.lowStockThreshold" @view="openDetail" @edit="openEdit" @remove="openDelete" />

        <div v-if="page.totalPages > 1" class="pagination">
            <span>Trang {{ page.pageNo + 1 }} / {{ page.totalPages }}</span>
          <div>
            <button class="page-button" type="button" :disabled="page.pageNo === 0" @click="changePage(page.pageNo - 1)">←</button>
            <button class="page-button" type="button" :disabled="page.last" @click="changePage(page.pageNo + 1)">→</button>
          </div>
        </div>
      </section>

      <footer class="app-footer"><span>Quản lý sản phẩm</span><span>Dữ liệu được đồng bộ trực tiếp với hệ thống</span></footer>
      </template>
      <AnalyticsView v-else-if="activeView === 'analytics'" @view-product="openDetailById" />
      <SettingsView v-else @saved="onSettingsSaved" />
    </main>
  </div>

  <ProductDrawer :open="drawerOpen" :product="selectedProduct" :saving="saving" :error-message="drawerError" :low-stock-threshold="settingsStore.lowStockThreshold" :product-code-prefix="settingsStore.productCodePrefix" :allow-negative-stock="settingsStore.allowNegativeStock" @close="drawerOpen = false" @save="saveProduct" />
  <ProductDetailDrawer :open="detailOpen" :product="detailProduct" :currency="settingsStore.currency" :date-format="settingsStore.dateFormat" :low-stock-threshold="settingsStore.lowStockThreshold" @close="detailOpen = false" @edit="openEdit" />
  <ConfirmDialog :open="Boolean(productToRemove)" :product-name="productToRemove?.name || ''" :busy="saving" @cancel="productToRemove = null" @confirm="confirmDelete" />

  <Transition name="toast">
    <div v-if="toast.visible" class="toast" :class="toast.tone" role="status">
      <span class="toast-check">{{ toast.tone === 'success' ? '✓' : '!' }}</span>
      <span><strong>{{ toast.title }}</strong><small>{{ toast.detail }}</small></span>
    </div>
  </Transition>
  </template>
</template>
