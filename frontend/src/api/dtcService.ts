import { requestApi } from './http'
import type { DtcReading, DtcType } from '../types/dtc'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isDtcReading(value: unknown): value is DtcReading {
  return (
    isRecord(value) &&
    typeof value.mode === 'string' &&
    Array.isArray(value.codes) &&
    value.codes.every((code) => typeof code === 'string') &&
    typeof value.instant === 'string'
  )
}

export async function readDtcs(type: DtcType): Promise<DtcReading> {
  const response = await requestApi(`/dtc/${encodeURIComponent(type)}`)
  if (!isDtcReading(response)) {
    throw new Error('A API retornou um formato inesperado para os códigos DTC.')
  }
  return response
}

export async function clearDtcs(): Promise<void> {
  await requestApi('/dtc/clear', { method: 'POST' })
}
