const rawBaseUrl = import.meta.env.VITE_API_BASE_URL
const isVercel =
  typeof window !== 'undefined' &&
  (window.location.hostname.endsWith('vercel.app') || window.location.hostname.includes('vercel'))
const API_BASE_URL = isVercel
  ? ''
  : typeof rawBaseUrl === 'string' && rawBaseUrl.length > 0
    ? rawBaseUrl.replace(/\/+$/, '')
    : (import.meta.env.DEV ? '' : 'http://localhost:18080')

let unauthorizedHandler: (() => void) | null = null
let csrfTokenValue: string | null = null

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler
}

export function setCsrfToken(token: string | null) {
  csrfTokenValue = token
}

export class ApiError extends Error {
  readonly status: number
  readonly requestId: string | null
  readonly details: Record<string, string> | null

  constructor(
    message: string,
    status: number,
    requestId: string | null = null,
    details: Record<string, string> | null = null,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.requestId = requestId
    this.details = details
  }
}

function readCookie(name: string) {
  const prefix = `${name}=`
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith(prefix))
    ?.slice(prefix.length) || null
}

async function resolveCsrfToken(forceRefresh = false) {
  const cookieToken = readCookie('XSRF-TOKEN')
  if (!forceRefresh && csrfTokenValue) return csrfTokenValue
  if (!forceRefresh && cookieToken) {
    csrfTokenValue = decodeURIComponent(cookieToken)
    return csrfTokenValue
  }

  // The API runs on another local port, so its CSRF cookie is not readable
  // from the frontend. Fetch the token explicitly when a page was reloaded.
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) return null

  const body = await response.json()
  const token = typeof body?.data === 'string' ? body.data : null
  if (token) csrfTokenValue = token
  return token
}

async function refreshCsrfToken() {
  csrfTokenValue = null
  return resolveCsrfToken(true)
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const method = (init.method || 'GET').toUpperCase()
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    const csrfToken = await resolveCsrfToken()
    if (csrfToken && !headers.has('X-XSRF-TOKEN')) {
      headers.set('X-XSRF-TOKEN', csrfToken)
    }
  }

  const fetchRequest = () =>
    fetch(`${API_BASE_URL}${path}`, {
      ...init,
      credentials: 'include',
      headers,
    })

  let response = await fetchRequest()
  // A restarted backend or an old tab can leave a stale CSRF token in memory.
  // Refresh it once and replay the write request before showing a 403 error.
  if (response.status === 403 && ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    const freshToken = await refreshCsrfToken()
    if (freshToken) {
      headers.set('X-XSRF-TOKEN', freshToken)
      response = await fetchRequest()
    }
  }

  const requestId = response.headers.get('X-Request-Id')
  const contentType = response.headers.get('content-type') || ''
  const body = contentType.includes('application/json') ? await response.json() : null

  if (!response.ok) {
    const details = body?.data && typeof body.data === 'object' ? body.data : null
    if (response.status === 401) unauthorizedHandler?.()
    throw new ApiError(body?.message || `Request failed with status ${response.status}`, response.status, requestId, details)
  }

  return body as T
}
