export type DtcType = 'confirmado' | 'pendente' | 'permanente'

export interface DtcReading {
  mode: string
  codes: string[]
  instant: string
}
