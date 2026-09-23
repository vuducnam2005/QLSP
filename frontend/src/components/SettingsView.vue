<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ApiError } from '../api/http'
import { useSettingsStore } from '../stores/settings'
import type { PasswordChangePayload, SettingsUpdatePayload } from '../types/settings'

const emit = defineEmits<{ saved: [message: string] }>()
const store = useSettingsStore()
const { settings, loading, saving, error } = storeToRefs(store)
const activeTab = ref<'inventory' | 'general' | 'account'>('inventory')
const fieldErrors = reactive<Record<string, string>>({})
const form = reactive({
  lowStockThreshold: 10,
  productCodePrefix: 'PRD-',
  allowNegativeStock: false,
  workspaceName: 'Không gian làm việc',
  currency: 'VND' as 'VND' | 'USD',
  dateFormat: 'DD/MM/YYYY' as 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD',
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const accountLabel = computed(() => settings.value?.account.username || settings.value?.username || 'admin')

watch(settings, (value) => {
  if (!value) return
  form.lowStockThreshold = value.lowStockThreshold
  form.productCodePrefix = value.productCodePrefix
  form.allowNegativeStock = value.allowNegativeStock
  form.workspaceName = value.workspaceName
  form.currency = value.currency
  form.dateFormat = value.dateFormat
}, { immediate: true })

function resetErrors() {
  Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
}

function validate() {
  resetErrors()
  if (!Number.isInteger(form.lowStockThreshold) || form.lowStockThreshold < 0) {
    fieldErrors.lowStockThreshold = 'Ngưỡng phải là số nguyên từ 0 trở lên.'
  }
  if (!/^[A-Za-z0-9][A-Za-z0-9-]*$/.test(form.productCodePrefix.trim())) {
    fieldErrors.productCodePrefix = 'Prefix chỉ gồm chữ, số và dấu gạch ngang.'
  }
  if (!form.workspaceName.trim()) fieldErrors.workspaceName = 'Tên workspace là bắt buộc.'
  if (activeTab.value === 'account' && (form.currentPassword || form.newPassword || form.confirmPassword)) {
    if (!form.currentPassword) fieldErrors.currentPassword = 'Nhập mật khẩu hiện tại.'
    if (form.newPassword.length < 8) fieldErrors.newPassword = 'Mật khẩu mới cần ít nhất 8 ký tự.'
    if (form.newPassword !== form.confirmPassword) fieldErrors.confirmPassword = 'Mật khẩu xác nhận không khớp.'
  }
  return Object.keys(fieldErrors).length === 0
}

function passwordPayload(): PasswordChangePayload | undefined {
  if (!form.currentPassword && !form.newPassword && !form.confirmPassword) return undefined
  return {
    currentPassword: form.currentPassword,
    newPassword: form.newPassword,
    confirmPassword: form.confirmPassword,
  }
}

async function save() {
  if (!validate()) return
  const payload: SettingsUpdatePayload = {
    lowStockThreshold: form.lowStockThreshold,
    productCodePrefix: form.productCodePrefix.trim().toUpperCase(),
    allowNegativeStock: form.allowNegativeStock,
    workspaceName: form.workspaceName.trim(),
    currency: form.currency,
    dateFormat: form.dateFormat,
  }
  const password = passwordPayload()
  if (password) payload.password = password

  try {
    await store.save(payload)
    form.currentPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    emit('saved', activeTab.value === 'account' && password ? 'Tài khoản và cài đặt đã được cập nhật.' : 'Cài đặt đã được lưu và áp dụng ngay.')
  } catch {
    // The inline alert below keeps the API error close to the form.
  }
}

function localizeError() {
  if (!error.value || error.value.status === 0) return 'Không thể kết nối đến máy chủ. Vui lòng thử lại.'
  if (error.value.details) return Object.values(error.value.details).join(' ')
  return error.value.message || 'Không thể lưu cài đặt.'
}

onMounted(() => {
  if (!settings.value && !loading.value) store.fetchSettings()
})
</script>

<template>
  <section class="settings-page" aria-labelledby="settings-title">
    <div class="settings-intro">
      <div>
        <p class="eyebrow">Không gian tồn kho / 03</p>
        <h1 id="settings-title">Cài đặt<span class="title-dot">.</span></h1>
        <p class="intro-copy">Điều chỉnh cách workspace nhận diện sản phẩm, hiển thị dữ liệu và bảo vệ tài khoản.</p>
      </div>
      <div class="settings-stamp"><span>LIVE CONFIG</span><strong>{{ settings?.workspaceName || 'Workspace' }}</strong></div>
    </div>

    <div v-if="loading && !settings" class="settings-loading" role="status"><span class="loading-orbit"></span><p>Đang tải cấu hình...</p></div>
    <div v-else class="settings-layout">
      <aside class="settings-tabs" aria-label="Các nhóm cài đặt">
        <button type="button" :class="{ active: activeTab === 'inventory' }" @click="activeTab = 'inventory'"><span>01</span><strong>Sản phẩm & tồn kho</strong><small>Quy tắc vận hành</small></button>
        <button type="button" :class="{ active: activeTab === 'general' }" @click="activeTab = 'general'"><span>02</span><strong>Cài đặt chung</strong><small>Nhận diện workspace</small></button>
        <button type="button" :class="{ active: activeTab === 'account' }" @click="activeTab = 'account'"><span>03</span><strong>Tài khoản & bảo mật</strong><small>Thông tin đăng nhập</small></button>
      </aside>

      <form class="settings-card" @submit.prevent="save">
        <div v-if="error" class="page-alert" role="alert"><span class="alert-mark">!</span><span><strong>Không thể lưu cài đặt.</strong> {{ localizeError() }}</span></div>

        <template v-if="activeTab === 'inventory'">
          <div class="settings-card-heading"><div><p class="eyebrow">Quy tắc vận hành</p><h2>Kiểm soát tồn kho</h2></div><span class="settings-index">01 / 03</span></div>
          <div class="settings-fields">
            <label class="field"><span>Ngưỡng cảnh báo tồn kho</span><div class="input-suffix"><input v-model.number="form.lowStockThreshold" type="number" min="0" max="1000000" step="1" :aria-invalid="Boolean(fieldErrors.lowStockThreshold)" /><b>sản phẩm</b></div><small class="field-hint">Sản phẩm có tồn kho thấp hơn ngưỡng này sẽ được đánh dấu LOW_STOCK trên danh mục và Dashboard.</small><small v-if="fieldErrors.lowStockThreshold" class="field-error">{{ fieldErrors.lowStockThreshold }}</small></label>
            <label class="field"><span>Tiền tố mã sản phẩm</span><input v-model="form.productCodePrefix" maxlength="20" placeholder="PRD-" :aria-invalid="Boolean(fieldErrors.productCodePrefix)" /><small class="field-hint">Dùng để sinh mã tự động dạng {{ form.productCodePrefix || 'PRD-' }}0001.</small><small v-if="fieldErrors.productCodePrefix" class="field-error">{{ fieldErrors.productCodePrefix }}</small></label>
            <label class="toggle-field"><span><strong>Cho phép tồn kho âm</strong><small>Cho phép nhập số lượng âm khi điều chỉnh tồn kho.</small></span><input v-model="form.allowNegativeStock" type="checkbox" /><i></i></label>
          </div>
        </template>

        <template v-else-if="activeTab === 'general'">
          <div class="settings-card-heading"><div><p class="eyebrow">Bản sắc workspace</p><h2>Cài đặt chung</h2></div><span class="settings-index">02 / 03</span></div>
          <div class="settings-fields">
            <label class="field"><span>Tên cửa hàng / workspace</span><input v-model="form.workspaceName" maxlength="120" placeholder="Không gian làm việc" :aria-invalid="Boolean(fieldErrors.workspaceName)" /><small v-if="fieldErrors.workspaceName" class="field-error">{{ fieldErrors.workspaceName }}</small></label>
            <div class="field-grid"><label class="field"><span>Định dạng tiền tệ</span><select v-model="form.currency"><option value="VND">VND — Việt Nam đồng</option><option value="USD">USD — US Dollar</option></select></label><label class="field"><span>Định dạng ngày tháng</span><select v-model="form.dateFormat"><option value="DD/MM/YYYY">DD/MM/YYYY</option><option value="MM/DD/YYYY">MM/DD/YYYY</option><option value="YYYY-MM-DD">YYYY-MM-DD</option></select></label></div>
            <div class="settings-preview"><span>PREVIEW</span><strong>{{ form.currency === 'VND' ? '₫2.247.500' : '$89.90' }}</strong><small>{{ form.dateFormat }} · {{ form.workspaceName || 'Workspace' }}</small></div>
          </div>
        </template>

        <template v-else>
          <div class="settings-card-heading"><div><p class="eyebrow">Quyền truy cập</p><h2>Tài khoản & bảo mật</h2></div><span class="settings-index">03 / 03</span></div>
          <div class="account-summary"><span class="account-summary-avatar">{{ accountLabel.slice(0, 2).toUpperCase() }}</span><div><strong>{{ accountLabel }}</strong><small>Quản trị viên · Tài khoản hiện tại</small></div><span class="account-verified">ĐANG HOẠT ĐỘNG</span></div>
          <div class="settings-fields"><div class="settings-section-label"><span>Đổi mật khẩu</span><small>Để trống nếu bạn chưa muốn thay đổi mật khẩu.</small></div><label class="field"><span>Mật khẩu hiện tại</span><input v-model="form.currentPassword" type="password" autocomplete="current-password" :aria-invalid="Boolean(fieldErrors.currentPassword)" /><small v-if="fieldErrors.currentPassword" class="field-error">{{ fieldErrors.currentPassword }}</small></label><div class="field-grid"><label class="field"><span>Mật khẩu mới</span><input v-model="form.newPassword" type="password" autocomplete="new-password" :aria-invalid="Boolean(fieldErrors.newPassword)" /><small v-if="fieldErrors.newPassword" class="field-error">{{ fieldErrors.newPassword }}</small></label><label class="field"><span>Xác nhận mật khẩu</span><input v-model="form.confirmPassword" type="password" autocomplete="new-password" :aria-invalid="Boolean(fieldErrors.confirmPassword)" /><small v-if="fieldErrors.confirmPassword" class="field-error">{{ fieldErrors.confirmPassword }}</small></label></div></div>
        </template>

        <div class="settings-actions"><span>Thay đổi được áp dụng cho workspace hiện tại.</span><button class="button primary-button" type="submit" :disabled="saving"><span v-if="saving" class="button-loader"></span>{{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}</button></div>
      </form>
    </div>
    <footer class="app-footer"><span>Cấu hình hệ thống</span><span>Thay đổi được lưu an toàn và áp dụng tức thì</span></footer>
  </section>
</template>
