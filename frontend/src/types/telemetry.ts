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

export interface TelemetrySample {
  receivedAt: number
  value: number | null
}
