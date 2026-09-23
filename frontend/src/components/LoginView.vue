<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{
  loading: boolean
  error: string
  notice: string
}>()

const emit = defineEmits<{
  submit: [username: string, password: string]
}>()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const touched = ref(false)

const formInvalid = computed(() => !username.value.trim() || !password.value)

function submit() {
  touched.value = true
  if (formInvalid.value || props.loading) return
  emit('submit', username.value.trim(), password.value)
}
</script>

<template>
  <main class="login-page">
    <section class="login-story" aria-label="Giới thiệu không gian quản trị">
      <div class="story-orbit orbit-one"></div>
      <div class="story-orbit orbit-two"></div>
      <div class="story-topline">
        <div class="login-brand-mark">P<span>.</span></div>
        <span class="story-code">OPS / 01</span>
      </div>

      <div class="story-content">
        <p class="eyebrow">Product operations</p>
        <h1>Mọi thứ<br /><em>trong tầm tay.</em></h1>
        <p class="story-copy">Một không gian gọn gàng để theo dõi sản phẩm, tồn kho và những quyết định phía sau mỗi con số.</p>
      </div>

      <div class="story-footer">
        <div class="story-signal"><i></i><span>Hệ thống sẵn sàng</span></div>
        <span class="story-note">Quản trị nội bộ</span>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-panel-inner">
        <div class="mobile-login-brand"><span>P</span><b>.</b> Product Ops</div>
        <div class="login-heading">
          <p class="eyebrow">Chào mừng trở lại</p>
          <h2>Đăng nhập<br /><span>vào workspace.</span></h2>
          <p>Nhập thông tin tài khoản quản trị để tiếp tục làm việc.</p>
        </div>

        <div v-if="notice" class="login-notice" role="status">
          <span>↗</span>{{ notice }}
        </div>
        <div v-if="error" class="login-error" role="alert">
          <span>!</span>{{ error }}
        </div>

        <form class="login-form" @submit.prevent="submit">
          <label class="login-field">
            <span>Tên đăng nhập</span>
            <div class="login-input-wrap">
              <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3.5" /><path d="M5 20c.8-3.3 3.1-5 7-5s6.2 1.7 7 5" /></svg>
              <input
                v-model="username"
                type="text"
                autocomplete="username"
                placeholder="Nhập tên đăng nhập"
                aria-label="Tên đăng nhập"
                :aria-invalid="touched && !username.trim()"
                autofocus
              />
            </div>
            <small v-if="touched && !username.trim()">Vui lòng nhập tên đăng nhập.</small>
          </label>

          <label class="login-field">
            <span>Mật khẩu</span>
            <div class="login-input-wrap">
              <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="10" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3" /></svg>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="Nhập mật khẩu"
                aria-label="Mật khẩu"
                :aria-invalid="touched && !password"
              />
              <button class="password-toggle" type="button" :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPassword = !showPassword">
                <svg v-if="showPassword" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18M10.6 10.7a2 2 0 0 0 2.7 2.7M9.9 5.3A11.7 11.7 0 0 1 12 5c5.2 0 8.6 4.2 9.5 7-.3 1-1 2.2-2 3.3M6.2 6.3C4.1 7.8 2.9 10 2.5 12c.9 2.8 4.3 7 9.5 7 1.4 0 2.7-.3 3.9-.8" /></svg>
                <svg v-else viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 5 12 5s9.5 7 9.5 7-3.5 7-9.5 7-9.5-7-9.5-7Z" /><circle cx="12" cy="12" r="2.5" /></svg>
              </button>
            </div>
            <small v-if="touched && !password">Vui lòng nhập mật khẩu.</small>
          </label>

          <button class="login-submit" type="submit" :disabled="loading">
            <span v-if="loading" class="login-spinner"></span>
            <span>{{ loading ? 'Đang xác thực...' : 'Đăng nhập' }}</span>
            <svg v-if="!loading" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 12h15M13 6l6 6-6 6" /></svg>
          </button>
        </form>

        <p class="login-footnote"><span class="lock-mark">◇</span> Phiên làm việc được bảo vệ bằng kết nối an toàn.</p>
      </div>
    </section>
  </main>
</template>
