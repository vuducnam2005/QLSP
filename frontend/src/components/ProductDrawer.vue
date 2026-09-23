<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { Product, ProductCreatePayload, ProductStatus, ProductUpdatePayload } from '../types/product'

const props = defineProps<{
  open: boolean
  product: Product | null
  saving: boolean
  errorMessage?: string
  lowStockThreshold: number
  productCodePrefix: string
  allowNegativeStock: boolean
}>()

const emit = defineEmits<{
  close: []
  save: [payload: ProductCreatePayload | ProductUpdatePayload]
}>()

const form = reactive({
  productCode: '',
  name: '',
  description: '',
  price: '',
  stockQuantity: '',
  status: 'ACTIVE' as ProductStatus,
})

const fieldErrors = reactive<Record<string, string>>({})
const editing = computed(() => Boolean(props.product))
const stockIsLow = computed(() => {
  const stockQuantity = Number(form.stockQuantity)
  return form.stockQuantity !== '' && Number.isInteger(stockQuantity) && stockQuantity < props.lowStockThreshold
})

function resetForm() {
  form.productCode = props.product?.productCode || ''
  form.name = props.product?.name || ''
  form.description = props.product?.description || ''
  form.price = props.product ? String(props.product.price) : ''
  form.stockQuantity = props.product ? String(props.product.stockQuantity) : ''
  form.status = props.product?.status || 'ACTIVE'
  if (stockIsLow.value) form.status = 'LOW_STOCK'
  Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
}

watch(() => [props.open, props.product], resetForm, { immediate: true })
watch(
  () => form.stockQuantity,
  () => {
    if (stockIsLow.value) form.status = 'LOW_STOCK'
  },
)

function validate() {
  Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
  if (!editing.value && form.productCode.trim() && !new RegExp(`^${props.productCodePrefix.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\d{4}$`).test(form.productCode.trim())) {
    fieldErrors.productCode = `Nhập mã theo định dạng ${props.productCodePrefix}0000.`
  }
  if (!form.name.trim()) fieldErrors.name = 'Tên sản phẩm là bắt buộc.'
  const price = Number(form.price)
  if (!form.price || !Number.isFinite(price) || !Number.isInteger(price) || price <= 0) {
    fieldErrors.price = 'Giá bán phải là số nguyên lớn hơn 0.'
  }
  const stockQuantity = Number(form.stockQuantity)
  if (
    form.stockQuantity === '' ||
    !Number.isFinite(stockQuantity) ||
    !Number.isInteger(stockQuantity) ||
    (!props.allowNegativeStock && stockQuantity < 0)
  ) {
    fieldErrors.stockQuantity = props.allowNegativeStock
      ? 'Số lượng tồn kho phải là số nguyên.'
      : 'Số lượng tồn kho phải là số nguyên từ 0 trở lên.'
  }
  return Object.keys(fieldErrors).length === 0
}

function submit() {
  if (!validate()) return
  const base = {
    name: form.name.trim(),
    description: form.description.trim() || null,
    price: Number(form.price),
    stockQuantity: Number(form.stockQuantity),
    status: form.status,
  }
  if (editing.value && props.product) {
    emit('save', { ...base, version: props.product.version } satisfies ProductUpdatePayload)
  } else {
    emit('save', { ...base, productCode: form.productCode.trim() } satisfies ProductCreatePayload)
  }
}
</script>

<template>
  <Transition name="fade">
    <div v-if="open" class="drawer-backdrop" @click.self="emit('close')">
      <aside class="product-drawer" role="dialog" aria-modal="true" aria-labelledby="drawer-title">
        <div class="drawer-topline">
           <span class="eyebrow">{{ editing ? 'Chỉnh sửa sản phẩm' : 'Sản phẩm mới' }}</span>
           <button class="icon-button" type="button" aria-label="Đóng biểu mẫu" @click="emit('close')">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" /></svg>
          </button>
        </div>
        <div class="drawer-heading">
           <h2 id="drawer-title">{{ editing ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm' }}</h2>
           <p>{{ editing ? 'Thay đổi được bảo vệ bằng khóa phiên bản.' : 'Tạo một bản ghi mới cho danh mục sản phẩm.' }}</p>
        </div>

        <div v-if="errorMessage" class="form-alert" role="alert">
           <strong>Không thể lưu sản phẩm.</strong>
          <span>{{ errorMessage }}</span>
        </div>

        <form class="product-form" @submit.prevent="submit">
          <label v-if="!editing" class="field">
             <span>Mã sản phẩm</span>
            <input id="product-code" v-model="form.productCode" :placeholder="`${props.productCodePrefix}0000`" maxlength="50" autocomplete="off" :aria-invalid="Boolean(fieldErrors.productCode)" aria-describedby="product-code-error" />
            <small class="field-hint">Để trống để tự sinh mã {{ props.productCodePrefix }}0001.</small>
            <small v-if="fieldErrors.productCode" id="product-code-error" class="field-error">{{ fieldErrors.productCode }}</small>
          </label>
          <div v-else class="locked-code">
             <span>Mã sản phẩm</span>
            <strong>{{ form.productCode }}</strong>
             <small>Mã sản phẩm không thể thay đổi sau khi tạo.</small>
          </div>

          <label class="field">
             <span>Tên sản phẩm</span>
             <input id="product-name" v-model="form.name" placeholder="Ví dụ: Chuột không dây M720" maxlength="255" :aria-invalid="Boolean(fieldErrors.name)" aria-describedby="product-name-error" />
            <small v-if="fieldErrors.name" id="product-name-error" class="field-error">{{ fieldErrors.name }}</small>
          </label>

          <label class="field">
             <span>Mô tả <em>Không bắt buộc</em></span>
             <textarea v-model="form.description" placeholder="Mô tả công dụng của sản phẩm" maxlength="5000" rows="3"></textarea>
          </label>

          <div class="field-grid">
            <label class="field">
               <span>Giá bán</span>
               <div class="input-prefix"><b>₫</b><input id="product-price" v-model="form.price" inputmode="numeric" type="number" min="1" step="1" placeholder="0" :aria-invalid="Boolean(fieldErrors.price)" aria-describedby="product-price-error" /></div>
              <small v-if="fieldErrors.price" id="product-price-error" class="field-error">{{ fieldErrors.price }}</small>
            </label>
            <label class="field">
               <span>Số lượng tồn kho</span>
              <input id="product-stock" v-model="form.stockQuantity" inputmode="numeric" type="number" :min="props.allowNegativeStock ? undefined : 0" step="1" placeholder="0" :aria-invalid="Boolean(fieldErrors.stockQuantity)" aria-describedby="product-stock-error" />
              <small v-if="fieldErrors.stockQuantity" id="product-stock-error" class="field-error">{{ fieldErrors.stockQuantity }}</small>
            </label>
          </div>

          <label class="field">
             <span>Trạng thái</span>
            <select v-model="form.status" :disabled="stockIsLow" aria-describedby="product-status-hint">
               <option value="ACTIVE">Đang hoạt động</option>
               <option value="LOW_STOCK">Sắp hết hàng</option>
               <option value="INACTIVE">Ngừng hoạt động</option>
            </select>
            <small id="product-status-hint" class="field-hint">
              {{ stockIsLow ? `Tồn kho dưới ${props.lowStockThreshold}: trạng thái được đặt tự động.` : 'Bạn có thể chọn thủ công.' }}
            </small>
          </label>

          <div class="drawer-actions">
             <button class="button secondary-button" type="button" :disabled="saving" @click="emit('close')">Hủy</button>
            <button class="button primary-button" type="submit" :disabled="saving">
              <span v-if="saving" class="button-loader"></span>
               {{ saving ? 'Đang lưu...' : editing ? 'Lưu thay đổi' : 'Tạo sản phẩm' }}
            </button>
          </div>
        </form>
      </aside>
    </div>
  </Transition>
</template>
