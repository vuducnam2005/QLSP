import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ApiError } from '../api/http'
import { getSettings, updateSettings } from '../api/settings'
import type { Settings, SettingsUpdatePayload } from '../types/settings'

export const useSettingsStore = defineStore('settings', () => {
  const settings = ref<Settings | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<ApiError | null>(null)

  const lowStockThreshold = computed(() => settings.value?.lowStockThreshold ?? 10)
  const allowNegativeStock = computed(() => settings.value?.allowNegativeStock ?? false)
  const productCodePrefix = computed(() => settings.value?.productCodePrefix ?? 'PRD-')
  const currency = computed(() => settings.value?.currency ?? 'VND')
  const dateFormat = computed(() => settings.value?.dateFormat ?? 'DD/MM/YYYY')

  async function fetchSettings() {
    loading.value = true
    error.value = null
    try {
      settings.value = (await getSettings()).data
      return settings.value
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to load settings', 0)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  async function save(payload: SettingsUpdatePayload) {
    saving.value = true
    error.value = null
    try {
      settings.value = (await updateSettings(payload)).data
      return settings.value
    } catch (caught) {
      error.value = caught instanceof ApiError ? caught : new ApiError('Unable to save settings', 0)
      throw error.value
    } finally {
      saving.value = false
    }
  }

  function clear() {
    settings.value = null
    error.value = null
  }

  return {
    settings,
    loading,
    saving,
    error,
    lowStockThreshold,
    allowNegativeStock,
    productCodePrefix,
    currency,
    dateFormat,
    fetchSettings,
    save,
    clear,
  }
})
