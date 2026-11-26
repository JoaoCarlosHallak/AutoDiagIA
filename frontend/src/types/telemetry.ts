export interface ObdValue {
  name: string
  value: number | null
  unit: string
}

export interface ObdReading {
  pid: string
  values: ObdValue[]
  timestamp: string
}

export type IgnitionType = 'SPARK_IGNITION' | 'COMPRESSION_IGNITION'

export type MonitorType =
  | 'MISFIRE'
  | 'FUEL_SYSTEM'
  | 'COMPREHENSIVE_COMPONENTS'
  | 'CATALYST'
  | 'HEATED_CATALYST'
  | 'EVAPORATIVE_SYSTEM'
  | 'SECONDARY_AIR_SYSTEM'
  | 'OXYGEN_SENSOR'
  | 'OXYGEN_SENSOR_HEATER'
  | 'EGR_AND_VVT'

export interface MonitorDTO {
  type: MonitorType
  supported: boolean
  complete: boolean | null
}

export interface MonitorStatusDTO {
  milOn: boolean
  DtcS: number
  ignitionType: IgnitionType
  monitors: MonitorDTO[]
}

export interface MonitorStatusCycleDTO {
  ignitionType: IgnitionType
  monitors: MonitorDTO[]
}

export interface TelemetrySample {
  receivedAt: number
  value: number | null
}
