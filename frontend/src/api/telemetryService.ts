import { requestApi } from './http'
import type {
  IgnitionType,
  MonitorDTO,
  MonitorStatusCycleDTO,
  MonitorStatusDTO,
  ObdReading,
  ObdValue,
} from '../types/telemetry'

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

const MONITOR_TYPES: readonly string[] = [
  'MISFIRE',
  'FUEL_SYSTEM',
  'COMPREHENSIVE_COMPONENTS',
  'CATALYST',
  'HEATED_CATALYST',
  'EVAPORATIVE_SYSTEM',
  'SECONDARY_AIR_SYSTEM',
  'OXYGEN_SENSOR',
  'OXYGEN_SENSOR_HEATER',
  'EGR_AND_VVT',
]

function isIgnitionType(value: unknown): value is IgnitionType {
  return value === 'SPARK_IGNITION' || value === 'COMPRESSION_IGNITION'
}

function isMonitor(value: unknown): value is MonitorDTO {
  return (
    isRecord(value) &&
    typeof value.type === 'string' &&
    MONITOR_TYPES.includes(value.type) &&
    typeof value.supported === 'boolean' &&
    (value.complete === null || typeof value.complete === 'boolean')
  )
}

function isMonitorStatus(value: unknown): value is MonitorStatusDTO {
  return (
    isRecord(value) &&
    typeof value.milOn === 'boolean' &&
    typeof value.DtcS === 'number' &&
    isIgnitionType(value.ignitionType) &&
    Array.isArray(value.monitors) &&
    value.monitors.every(isMonitor)
  )
}

function isMonitorStatusCycle(value: unknown): value is MonitorStatusCycleDTO {
  return (
    isRecord(value) &&
    !('milOn' in value) &&
    !('DtcS' in value) &&
    isIgnitionType(value.ignitionType) &&
    Array.isArray(value.monitors) &&
    value.monitors.every(isMonitor)
  )
}

function invalidResponse(): Error {
  return new Error('A API retornou um formato inesperado para a leitura OBD.')
}

function invalidMonitorResponse(pid: string): Error {
  return new Error(`A API retornou um formato inesperado para o PID ${pid}.`)
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

export async function readMonitorStatusSinceClear(): Promise<MonitorStatusDTO> {
  const response = await requestApi('/telemetry/pid?name=01')
  if (!isMonitorStatus(response)) {
    throw invalidMonitorResponse('01')
  }
  return response
}

export async function readMonitorStatusThisCycle(): Promise<MonitorStatusCycleDTO> {
  const response = await requestApi('/telemetry/pid?name=41')
  if (!isMonitorStatusCycle(response)) {
    throw invalidMonitorResponse('41')
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
