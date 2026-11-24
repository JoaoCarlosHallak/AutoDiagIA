import { requestApi } from './http'
import type {
  CreateSessionRequest,
  LogDTO,
  PageResponse,
  SessionDTO,
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

export async function listSessions(): Promise<SessionDTO[]> {
  const response = await requestApi('/session')
  if (!Array.isArray(response) || !response.every(isSession)) {
    throw new Error('A API retornou um formato inesperado para as sessões.')
  }
  return response
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
