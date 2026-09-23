import { request, setCsrfToken } from './http'
import type { ApiResponse } from '../types/product'
import type { AuthResponse, AuthUser, LoginPayload } from '../types/auth'

const endpoint = '/api/v1/auth'

export function getCsrfToken() {
  return request<ApiResponse<string>>(`${endpoint}/csrf`).then((response) => {
    setCsrfToken(response.data)
    return response
  })
}

export function login(payload: LoginPayload) {
  return request<AuthResponse>(`${endpoint}/login`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getCurrentUser() {
  return request<ApiResponse<AuthUser>>(`${endpoint}/me`)
}

export function logout() {
  return request<ApiResponse<null>>(`${endpoint}/logout`, { method: 'POST' })
}
