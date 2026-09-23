import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCsrfToken, getCurrentUser, login as loginRequest, logout as logoutRequest } from '../api/auth'
import { ApiError } from '../api/http'
import type { AuthUser } from '../types/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(null)
  const authenticated = ref(false)
  const loading = ref(false)
  const error = ref('')
  const notice = ref('')
  const initialized = ref(false)

  function clearSession(showNotice = false) {
    const hadSession = authenticated.value
    user.value = null
    authenticated.value = false
    if (showNotice && hadSession) notice.value = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
  }

  async function fetchCurrentUser() {
    try {
      const response = await getCurrentUser()
      user.value = response.data
      authenticated.value = true
      notice.value = ''
      return response.data
    } catch (caught) {
      if (caught instanceof ApiError && caught.status === 401) {
        clearSession(false)
      } else {
        throw caught
      }
      return null
    }
  }

  async function initializeAuth() {
    loading.value = true
    error.value = ''
    try {
      await fetchCurrentUser()
    } catch (caught) {
      error.value =
        caught instanceof ApiError && caught.status === 0
          ? 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Docker và thử lại.'
          : 'Không thể kiểm tra phiên đăng nhập. Vui lòng thử lại.'
    } finally {
      initialized.value = true
      loading.value = false
    }
  }

  async function login(username: string, password: string) {
    loading.value = true
    error.value = ''
    notice.value = ''
    try {
      await getCsrfToken()
      const response = await loginRequest({ username, password })
      user.value = response.data
      authenticated.value = true
      return response.data
    } catch (caught) {
      error.value =
        caught instanceof ApiError && caught.status === 0
          ? 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Docker và thử lại.'
          : 'Tên đăng nhập hoặc mật khẩu không đúng.'
      throw caught
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    loading.value = true
    try {
      await getCsrfToken()
      await logoutRequest()
    } catch {
      // Local auth state is cleared even when the server session has expired.
    } finally {
      clearSession(false)
      loading.value = false
    }
  }

  function handleUnauthorized() {
    clearSession(true)
  }

  return {
    user,
    authenticated,
    loading,
    error,
    notice,
    initialized,
    login,
    logout,
    fetchCurrentUser,
    initializeAuth,
    handleUnauthorized,
  }
})
