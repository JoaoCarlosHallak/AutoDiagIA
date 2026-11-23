import { useState } from 'react'
import { readDtcs } from '../api/dtcService'
import type { DtcReading, DtcType } from '../types/dtc'
import { SectionHeading } from './SectionHeading'
import './DtcPage.css'

const DTC_TYPES: Array<{
  type: DtcType
  label: string
  mode: string
  description: string
}> = [
  {
    type: 'confirmado',
    label: 'Confirmados',
    mode: '03',
    description: 'Falhas verificadas pelo módulo de controle.',
  },
  {
    type: 'pendente',
    label: 'Pendentes',
    mode: '07',
    description: 'Falhas detectadas, ainda não confirmadas.',
  },
  {
    type: 'permanente',
    label: 'Permanentes',
    mode: '0A',
    description: 'Falhas confirmadas que continuam registradas.',
  },
]

function describeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocorreu um erro inesperado ao consultar os DTCs.'
}

function categoryName(code: string): string {
  switch (code[0]) {
    case 'P':
      return 'Powertrain'
    case 'C':
      return 'Chassis'
    case 'B':
      return 'Carroceria'
    case 'U':
      return 'Rede'
    default:
      return 'Categoria'
  }
}

export function DtcPage() {
  const [readings, setReadings] = useState<Partial<Record<DtcType, DtcReading>>>({})
  const [loading, setLoading] = useState<DtcType[]>([])
  const [errors, setErrors] = useState<Partial<Record<DtcType, string>>>({})

  async function handleRead(type: DtcType) {
    if (loading.includes(type)) {
      return
    }

    setLoading((current) => [...current, type])
    setErrors((current) => ({ ...current, [type]: undefined }))
    try {
      const reading = await readDtcs(type)
      setReadings((current) => ({ ...current, [type]: reading }))
    } catch (error) {
      setErrors((current) => ({ ...current, [type]: describeError(error) }))
    } finally {
      setLoading((current) => current.filter((item) => item !== type))
    }
  }

  async function handleReadAll() {
    await Promise.all(DTC_TYPES.map(({ type }) => handleRead(type)))
  }

  return (
    <>
      <SectionHeading section="dtc" />

      <div className="dtc-toolbar">
        <div>
          <p className="dtc-toolbar-kicker">Diagnóstico OBD-II</p>
          <h3 className="dtc-toolbar-title">Códigos de falha</h3>
        </div>
        <button
          className="button-primary"
          disabled={loading.length > 0}
          onClick={() => void handleReadAll()}
          type="button"
        >
          {loading.length > 0 ? 'Consultando…' : 'Consultar todos'}
        </button>
      </div>

      <section className="dtc-info-box" aria-labelledby="dtc-format-title">
        <div className="dtc-info-symbol" aria-hidden="true">i</div>
        <div>
          <h3 id="dtc-format-title">Como ler um código DTC</h3>
          <p>
            Um DTC segue o padrão <strong className="dtc-example">P0XXX</strong>
            : a primeira letra indica a área, o primeiro dígito informa se é
            padrão SAE ou específico do fabricante, e os últimos dígitos
            identificam o circuito ou a condição exata.
          </p>
          <div className="dtc-format-grid">
            <span><b>P</b> Powertrain</span>
            <span><b>C</b> Chassis</span>
            <span><b>B</b> Carroceria</span>
            <span><b>U</b> Rede</span>
            <span><b>0</b> Padrão SAE</span>
            <span><b>1</b> Específico do fabricante</span>
          </div>
        </div>
      </section>

      <div className="dtc-grid">
        {DTC_TYPES.map(({ type, label, mode, description }) => {
          const reading = readings[type]
          const codes = reading?.codes ?? []
          const isLoading = loading.includes(type)
          const error = errors[type]

          return (
            <section className="dtc-card" key={type}>
              <div className="dtc-card-heading">
                <div>
                  <p className="dtc-card-kicker">Modo {mode}</p>
                  <h3>{label}</h3>
                </div>
                <span className="dtc-count">{codes.length}</span>
              </div>

              <p className="dtc-description">{description}</p>

              {error && (
                <p className="dtc-error" role="alert">{error}</p>
              )}

              {isLoading ? (
                <div className="dtc-empty">
                  <span className="dtc-empty-glyph" aria-hidden="true">•••</span>
                  <p>Consultando o veículo…</p>
                </div>
              ) : !reading ? (
                <div className="dtc-empty">
                  <span className="dtc-empty-glyph" aria-hidden="true">—</span>
                  <p>Nenhuma consulta realizada.</p>
                </div>
              ) : codes.length === 0 ? (
                <div className="dtc-empty">
                  <span className="dtc-empty-glyph is-clean" aria-hidden="true">✓</span>
                  <p>Nenhum DTC registrado.</p>
                </div>
              ) : (
                <div className="dtc-code-list">
                  {codes.map((code) => (
                    <article className="dtc-code-row" key={code}>
                      <span className={`dtc-code-letter dtc-${code[0]?.toLowerCase() ?? 'x'}`}>
                        {code[0] ?? '?'}
                      </span>
                      <div>
                        <strong>{code}</strong>
                        <span>{categoryName(code)}</span>
                      </div>
                    </article>
                  ))}
                </div>
              )}

              <button
                className="button-secondary dtc-read-button"
                disabled={isLoading || loading.length > 0}
                onClick={() => void handleRead(type)}
                type="button"
              >
                {isLoading ? 'Consultando…' : reading ? 'Consultar novamente' : 'Consultar'}
              </button>
            </section>
          )
        })}
      </div>
    </>
  )
}
