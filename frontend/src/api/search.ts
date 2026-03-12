/**
 * Search API
 */
import client from './client'
import type { SearchRequest, SearchResult } from '@/types'

/**
 * Search tables
 */
export const searchTables = async (params: SearchRequest): Promise<SearchResult> => {
  const response = await client.get<SearchResult>('/api/v1/search', { params })
  return response.data
}

/**
 * Get search suggestions
 */
export const getSuggestions = async (prefix: string, limit: number = 10): Promise<string[]> => {
  const response = await client.get<string[]>('/api/v1/search/suggest', {
    params: { prefix, limit }
  })
  return response.data
}

/**
 * Filter tables with advanced criteria
 */
export const filterTables = async (filters: Record<string, any>): Promise<SearchResult> => {
  const response = await client.post<SearchResult>('/api/v1/search/filter', filters)
  return response.data
}
