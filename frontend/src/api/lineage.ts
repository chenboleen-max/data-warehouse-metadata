/**
 * Lineage API
 */
import client from './client'
import type {
  Lineage,
  LineageGraph,
  ImpactReport,
  TableMetadata,
  LineageDirection
} from '@/types'

/**
 * Create lineage relationship
 */
export const createLineage = async (data: {
  sourceTableId: string
  targetTableId: string
  lineageType: 'DIRECT' | 'INDIRECT'
  transformationLogic?: string
}): Promise<Lineage> => {
  const response = await client.post<Lineage>('/api/v1/lineage', data)
  return response.data
}

/**
 * Delete lineage relationship
 */
export const deleteLineage = async (id: string): Promise<void> => {
  await client.delete(`/api/v1/lineage/${id}`)
}

/**
 * Get upstream tables
 */
export const getUpstreamTables = async (tableId: string): Promise<TableMetadata[]> => {
  const response = await client.get<TableMetadata[]>(`/api/v1/lineage/upstream/${tableId}`)
  return response.data
}

/**
 * Get downstream tables
 */
export const getDownstreamTables = async (tableId: string): Promise<TableMetadata[]> => {
  const response = await client.get<TableMetadata[]>(`/api/v1/lineage/downstream/${tableId}`)
  return response.data
}

/**
 * Get lineage graph
 */
export const getLineageGraph = async (
  tableId: string,
  direction: LineageDirection = 'both',
  depth: number = 3
): Promise<LineageGraph> => {
  const response = await client.get<LineageGraph>(`/api/v1/lineage/graph/${tableId}`, {
    params: { direction, depth }
  })
  return response.data
}

/**
 * Analyze impact
 */
export const analyzeImpact = async (tableId: string): Promise<ImpactReport> => {
  const response = await client.post<ImpactReport>('/api/v1/lineage/impact', { tableId })
  return response.data
}

/**
 * Parse SQL for lineage extraction
 */
export const parseSqlLineage = async (sql: string): Promise<Lineage[]> => {
  const response = await client.post<Lineage[]>('/api/v1/lineage/parse-sql', { sql })
  return response.data
}
