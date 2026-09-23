export interface AuthUser {
  username: string
  role: string
  authenticated: boolean
}

export interface AuthResponse {
  success: boolean
  statusCode: number
  message: string
  data: AuthUser
}

export interface LoginPayload {
  username: string
  password: string
}
