/**
 * Table Metadata API
 */
import client from './client'
import type {
  TableMetadata,
  TableCreateRequest,
  TableUpdateRequest,
  PagedResponse,
  Column
} from '@/types'

/**
 * Get table list with pagination and filters
 */
export const listTables = async (params?: {
  page?: number
  pageSize?: number
  databaseName?: string
  tableName?: string
  tableType?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}): Promise<PagedResponse<TableMetadata>> => {
  const response = await client.get<PagedResponse<TableMetadata>>('/api/v1/tables', { params })
  return response.data
}

/**
 * Get table by ID
 */
export const getTableById = async (id: string): Promise<TableMetadata> => {
  const response = await client.get<TableMetadata>(`/api/v1/tables/${id}`)
  return response.data
}

/**
 * Create new table
 */
export const createTable = async (data: TableCreateRequest): Promise<TableMetadata> => {
  const response = await client.post<TableMetadata>('/api/v1/tables', data)
  return response.data
}

/**
 * Update table
 */
export const updateTable = async (id: string, data: TableUpdateRequest): Promise<TableMetadata> => {
  const response = await client.put<TableMetadata>(`/api/v1/tables/${id}`, data)
  return response.data
}

/**
 * Delete table
 */
export const deleteTable = async (id: string): Promise<void> => {
  await client.delete(`/api/v1/tables/${id}`)
}

/**
 * Get table columns
 */
export const getTableColumns = async (tableId: string): Promise<Column[]> => {
  const response = await client.get<Column[]>(`/api/v1/tables/${tableId}/columns`)
  return response.data
}
