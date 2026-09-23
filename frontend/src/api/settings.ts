import { request } from './http'
import type { ApiResponse } from '../types/product'
import type { Settings, SettingsUpdatePayload } from '../types/settings'

const endpoint = '/api/v1/settings'

export function getSettings() {
  return request<ApiResponse<Settings>>(endpoint)
}

export function updateSettings(payload: SettingsUpdatePayload) {
  return request<ApiResponse<Settings>>(endpoint, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
