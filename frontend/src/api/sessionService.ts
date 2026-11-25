import { requestApi } from './http'
import type {
  CreateSessionRequest,
  LogDTO,
  PageResponse,
  SessionDTO,
  SessionTemplateDTO,
  SessionTemplateFields,
  TriggerConfig,
} from '../types/session'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isSession(value: unknown): value is SessionDTO {
  return (
    isRecord(value) &&
    typeof value.sessionId === 'number' &&
    typeof value.vehicle === 'string' &&
    typeof value.status === 'string'
  )
}

function isLog(value: unknown): value is LogDTO {
  return (
    isRecord(value) &&
    typeof value.id === 'number' &&
    typeof value.pid === 'string' &&
    typeof value.valueName === 'string' &&
    (value.value === null ||
      (typeof value.value === 'number' && Number.isFinite(value.value))) &&
    typeof value.unit === 'string' &&
    typeof value.timestamp === 'string'
  )
}

function isTriggerConfig(value: unknown): value is TriggerConfig | null {
  if (value === null) return true
  if (!isRecord(value)) return false
  const operators = new Set<string>(['GT', 'GTE', 'LT', 'LTE', 'EQ'])
  return (
    typeof value.pid === 'string' &&
    typeof value.valueIndex === 'number' &&
    Number.isInteger(value.valueIndex) &&
    value.valueIndex >= 0 &&
    typeof value.operator === 'string' &&
    operators.has(value.operator) &&
    typeof value.triggerValue === 'number' &&
    Number.isFinite(value.triggerValue)
  )
}

function isSessionTemplate(value: unknown): value is SessionTemplateDTO {
  return (
    isRecord(value) &&
    typeof value.templateId === 'number' &&
    typeof value.name === 'string' &&
    typeof value.startReason === 'string' &&
    Array.isArray(value.pids) &&
    value.pids.length > 0 &&
    value.pids.every((pid) => typeof pid === 'string') &&
    typeof value.includeSessionSettings === 'boolean' &&
    (value.intervalMs === null ||
      (typeof value.intervalMs === 'number' && Number.isFinite(value.intervalMs))) &&
    (value.maxDurationMs === null ||
      (typeof value.maxDurationMs === 'number' &&
        Number.isFinite(value.maxDurationMs))) &&
    isTriggerConfig(value.trigger)
  )
}

function assertSessionTemplate(value: unknown): SessionTemplateDTO {
  if (!isSessionTemplate(value)) {
    throw new Error('A API retornou um formato inesperado para o modelo de sessão.')
  }
  return value
}

export async function listSessions(): Promise<SessionDTO[]> {
  const response = await requestApi('/session')
  if (!Array.isArray(response) || !response.every(isSession)) {
    throw new Error('A API retornou um formato inesperado para as sessões.')
  }
  return response
}

export async function listSessionTemplates(): Promise<SessionTemplateDTO[]> {
  const response = await requestApi('/session/templates')
  if (!Array.isArray(response)) {
    throw new Error('A API retornou um formato inesperado para os modelos de sessão.')
  }
  return response.map(assertSessionTemplate)
}

export async function createSessionTemplate(
  request: SessionTemplateFields,
): Promise<SessionTemplateDTO> {
  const response = await requestApi('/session/templates', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return assertSessionTemplate(response)
}

export async function updateSessionTemplate(
  id: number,
  request: SessionTemplateFields,
): Promise<SessionTemplateDTO> {
  const response = await requestApi(`/session/templates/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return assertSessionTemplate(response)
}

export async function deleteSessionTemplate(id: number): Promise<void> {
  await requestApi(`/session/templates/${id}`, { method: 'DELETE' })
}

export async function getSession(id: number): Promise<SessionDTO> {
  const response = await requestApi(`/session/${id}`)
  if (!isSession(response)) {
    throw new Error('A API retornou um formato inesperado para a sessão.')
  }
  return response
}

export async function createSession(
  request: CreateSessionRequest,
): Promise<SessionDTO> {
  const response = await requestApi('/session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!isSession(response)) {
    throw new Error('A API retornou um formato inesperado ao criar a sessão.')
  }
  return response
}

export async function startSession(id: number): Promise<SessionDTO> {
  const response = await requestApi(`/session/start/${id}`, { method: 'POST' })
  if (!isSession(response)) {
    throw new Error('A API retornou um formato inesperado ao iniciar a sessão.')
  }
  return response
}

export async function stopSession(id: number): Promise<SessionDTO> {
  const response = await requestApi(`/session/stop/${id}`, { method: 'POST' })
  if (!isSession(response)) {
    throw new Error('A API retornou um formato inesperado ao encerrar a sessão.')
  }
  return response
}

export async function getSessionLogs(
  id: number,
  page = 0,
  size = 500,
): Promise<PageResponse<LogDTO>> {
  const response = await requestApi(
    `/session/${id}/logs?page=${page}&size=${size}&sort=timestamp,desc`,
  )
  if (
    !isRecord(response) ||
    !Array.isArray(response.content) ||
    !response.content.every(isLog)
  ) {
    throw new Error('A API retornou um formato inesperado para os registros.')
  }
  return response as unknown as PageResponse<LogDTO>
}

export async function analyseSession(id: number): Promise<string> {
  const response = await requestApi(`/session/analyse/${id}`)
  if (typeof response !== 'string' || response.trim().length === 0) {
    throw new Error('A API não retornou uma análise válida.')
  }
  return response
}
