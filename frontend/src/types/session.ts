export type SessionStatus = 'CREATED' | 'RUNNING' | 'FINISHED' | 'FAILED'
export type SessionEndReason =
  | 'TRIGGER_REACHED'
  | 'MAX_DURATION_REACHED'
  | 'USER_STOPPED'
  | 'ERROR'

export type TriggerOperator = 'GT' | 'GTE' | 'LT' | 'LTE' | 'EQ'

export interface TriggerConfig {
  pid: string
  valueIndex: number
  operator: TriggerOperator
  triggerValue: number
}

export interface SessionDTO {
  sessionId: number
  vehicle: string
  startReason: string
  status: SessionStatus
  intervalMs: number
  maxDurationMs: number
  triggerConfig: TriggerConfig | null
  createdAt: string
  startedAt: string | null
  finishedAt: string | null
  endReason: SessionEndReason | null
}

export interface LogDTO {
  id: number
  sessionId: number
  pid: string
  valueName: string
  value: number | null
  unit: string
  timestamp: string
}

export interface CreateSessionRequest {
  vehicle: string
  pids: string[]
  intervalMs: number
  startReason: string
  maxDurationMs: number
  trigger: TriggerConfig | null
}

export interface SessionTemplateFields {
  name: string
  startReason: string
  pids: string[]
  includeSessionSettings: boolean
  intervalMs: number | null
  maxDurationMs: number | null
  trigger: TriggerConfig | null
}

export interface SessionTemplateDTO extends SessionTemplateFields {
  templateId: number
}

export interface SessionTemplatePreset extends SessionTemplateFields {
  key: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  last: boolean
  first: boolean
}
