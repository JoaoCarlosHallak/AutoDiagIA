import { requestApi } from './http'
import type { ObdReading, ObdValue } from '../types/telemetry'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isObdValue(value: unknown): value is ObdValue {
  return (
    isRecord(value) &&
    typeof value.name === 'string' &&
    (value.value === null ||
      (typeof value.value === 'number' && Number.isFinite(value.value))) &&
    typeof value.unit === 'string'
  )
}

function isObdReading(value: unknown): value is ObdReading {
  return (
    isRecord(value) &&
    typeof value.pid === 'string' &&
    Array.isArray(value.values) &&
    value.values.every(isObdValue) &&
    typeof value.timestamp === 'string'
  )
}

function invalidResponse(): Error {
  return new Error('A API retornou um formato inesperado para a leitura OBD.')
}

export async function readPids(pids: string[]): Promise<ObdReading[]> {
  const response = await requestApi('/telemetry/pid', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(pids),
  })

  if (!Array.isArray(response) || !response.every(isObdReading)) {
    throw invalidResponse()
  }

  return response
}

export async function readPid(pid: string): Promise<ObdReading> {
  const response = await requestApi(
    `/telemetry/pid?name=${encodeURIComponent(pid)}`,
  )

  if (!isObdReading(response)) {
    throw invalidResponse()
  }

  return response
}
