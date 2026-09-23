const numberFormatter = new Intl.NumberFormat('vi-VN')

export function formatNumber(value: number) {
  return numberFormatter.format(value)
}

export function formatCurrency(value: number, currency: 'VND' | 'USD' = 'VND') {
  return new Intl.NumberFormat(currency === 'VND' ? 'vi-VN' : 'en-US', {
    style: 'currency',
    currency,
    maximumFractionDigits: currency === 'VND' ? 0 : 2,
  }).format(value)
}

export function formatDate(value: string, dateFormat: 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD' = 'DD/MM/YYYY') {
  const date = new Date(value)
  const parts = {
    day: String(date.getDate()).padStart(2, '0'),
    month: String(date.getMonth() + 1).padStart(2, '0'),
    year: String(date.getFullYear()),
  }
  if (dateFormat === 'MM/DD/YYYY') return `${parts.month}/${parts.day}/${parts.year}`
  if (dateFormat === 'YYYY-MM-DD') return `${parts.year}-${parts.month}-${parts.day}`
  return `${parts.day}/${parts.month}/${parts.year}`
}
