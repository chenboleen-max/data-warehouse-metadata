/**
 * TypeScript type definitions for Kiro Metadata Management System
 */

// ==================== User Types ====================
export type UserRole = 'ADMIN' | 'DEVELOPER' | 'GUEST'

export interface User {
  id: string
  username: string
  email: string
  role: UserRole
  isActive: boolean
  createdAt: string
  updatedAt: string
  lastLoginAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

// ==================== Table Types ====================
export type TableType = 'TABLE' | 'VIEW' | 'EXTERNAL'

export interface TableMetadata {
  id: string
  databaseName: string
  tableName: string
  tableType: TableType
  description?: string
  storageFormat?: string
  storageLocation?: string
  dataSizeBytes?: number
  createdAt: string
  updatedAt: string
  lastAccessedAt?: string
  ownerId: string
  owner?: User
  columns?: Column[]
}

export interface TableCreateRequest {
  databaseName: string
  tableName: string
  tableType: TableType
  description?: string
  storageFormat?: string
  storageLocation?: string
  dataSizeBytes?: number
}

export interface TableUpdateRequest {
  description?: string
  storageFormat?: string
  storageLocation?: string
  dataSizeBytes?: number
}

// ==================== Column Types ====================
export interface Column {
  id: string
  tableId: string
  columnName: string
  dataType: string
  columnOrder: number
  isNullable: boolean
  isPartitionKey: boolean
  description?: string
  createdAt: string
  updatedAt: string
}

export interface ColumnCreateRequest {
  tableId: string
  columnName: string
  dataType: string
  columnOrder: number
  isNullable?: boolean
  isPartitionKey?: boolean
  description?: string
}

export interface ColumnUpdateRequest {
  columnName?: string
  dataType?: string
  columnOrder?: number
  isNullable?: boolean
  isPartitionKey?: boolean
  description?: string
}

export interface ReorderColumnsRequest {
  tableId: string
  columnOrders: Array<{ columnId: string; columnOrder: number }>
}

// ==================== Lineage Types ====================
export type LineageType = 'DIRECT' | 'INDIRECT'
export type LineageDirection = 'upstream' | 'downstream' | 'both'

export interface Lineage {
  id: string
  sourceTableId: string
  targetTableId: string
  lineageType: LineageType
  transformationLogic?: string
  createdAt: string
  updatedAt: string
  createdBy: string
}

export interface LineageNode {
  id: string
  name: string
  depth: number
  type: string
}

export interface LineageEdge {
  source: string
  target: string
  type: LineageType
}

export interface LineageGraph {
  nodes: LineageNode[]
  edges: LineageEdge[]
}

export interface ImpactReport {
  affectedTables: string[]
  maxDepth: number
  totalCount: number
}

// ==================== Catalog Types ====================
export interface Catalog {
  id: string
  name: string
  description?: string
  parentId?: string
  level: number
  path: string
  createdAt: string
  updatedAt: string
  createdBy: string
  children?: Catalog[]
  tables?: TableMetadata[]
}

export interface CatalogCreateRequest {
  name: string
  description?: string
  parentId?: string
}

// ==================== Quality Types ====================
export interface QualityMetrics {
  id: string
  tableId: string
  recordCount?: number
  nullRate?: number
  updateFrequency?: string
  dataFreshnessHours?: number
  measuredAt: string
  createdAt: string
}

export interface QualitySnapshot {
  measuredAt: string
  recordCount: number
  nullRate: number
  dataFreshnessHours: number
}

// ==================== History Types ====================
export type OperationType = 'CREATE' | 'UPDATE' | 'DELETE'

export interface ChangeHistory {
  id: string
  entityType: string
  entityId: string
  operation: OperationType
  fieldName?: string
  oldValue?: string
  newValue?: string
  changedAt: string
  changedBy: string
}

// ==================== Search Types ====================
export interface SearchRequest {
  keyword: string
  filters?: Record<string, any>
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface SearchResult {
  items: TableMetadata[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ==================== Import/Export Types ====================
export type ExportType = 'CSV' | 'JSON'
export type TaskStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export interface ExportTask {
  id: string
  taskType: ExportType
  filters?: string
  status: TaskStatus
  filePath?: string
  recordCount?: number
  errorMessage?: string
  createdAt: string
  createdBy: string
  startedAt?: string
  completedAt?: string
}

export interface ImportResult {
  successCount: number
  failureCount: number
  errors: Array<{ row: number; message: string }>
}

// ==================== Pagination Types ====================
export interface Pagination {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

export interface PagedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ==================== API Response Types ====================
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface ApiError {
  code: number
  message: string
  details?: any
  timestamp: string
  requestId?: string
}
