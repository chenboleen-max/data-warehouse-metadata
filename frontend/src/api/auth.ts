/**
 * Authentication API
 */
import client from './client'
import type { LoginRequest, TokenResponse, User } from '@/types'

/**
 * User login
 */
export const login = async (data: LoginRequest): Promise<TokenResponse> => {
  const response = await client.post<TokenResponse>('/api/v1/auth/login', data)
  return response.data
}

/**
 * User logout
 */
export const logout = async (): Promise<void> => {
  await client.post('/api/v1/auth/logout')
}

/**
 * Refresh access token
 */
export const refreshToken = async (refreshToken: string): Promise<TokenResponse> => {
  const response = await client.post<TokenResponse>('/api/v1/auth/refresh', null, {
    params: { refreshToken }
  })
  return response.data
}

/**
 * Get current user information
 */
export const getCurrentUser = async (): Promise<User> => {
  const response = await client.get<User>('/api/v1/auth/me')
  return response.data
}
