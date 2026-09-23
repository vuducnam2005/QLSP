export type Currency = 'VND' | 'USD'
export type DateFormat = 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD'

export interface SettingsAccount {
  username: string
  role: string
}

export interface Settings {
  id: number
  username: string
  lowStockThreshold: number
  productCodePrefix: string
  allowNegativeStock: boolean
  workspaceName: string
  currency: Currency
  dateFormat: DateFormat
  account: SettingsAccount
}

export interface PasswordChangePayload {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface SettingsUpdatePayload {
  lowStockThreshold: number
  productCodePrefix: string
  allowNegativeStock: boolean
  workspaceName: string
  currency: Currency
  dateFormat: DateFormat
  password?: PasswordChangePayload
}
