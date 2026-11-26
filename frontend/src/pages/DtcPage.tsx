import { useRef, useState } from 'react'
import { clearDtcs, readDtcs } from '../api/dtcService'
import {
  readMonitorStatusSinceClear,
  readMonitorStatusThisCycle,
} from '../api/telemetryService'
import type { DtcReading, DtcType } from '../types/dtc'
import type {
  IgnitionType,
  MonitorDTO,
  MonitorStatusCycleDTO,
  MonitorStatusDTO,
  MonitorType,
} from '../types/telemetry'
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

const DIAGNOSTIC_TABS = [
  { id: 'dtc', label: 'Falhas e códigos' },
  { id: 'readiness', label: 'Monitores de prontidão' },
  { id: 'ecu-tests', label: 'Testes da ECU' },
] as const

type DiagnosticTab = (typeof DIAGNOSTIC_TABS)[number]['id']
type ReadinessContext = 'since-clear' | 'driving-cycle'
type ReadinessReading = MonitorStatusDTO | MonitorStatusCycleDTO

const MONITOR_LABELS: Record<MonitorType, string> = {
  MISFIRE: 'Misfire',
  FUEL_SYSTEM: 'Fuel System',
  COMPREHENSIVE_COMPONENTS: 'Comprehensive Components',
  CATALYST: 'Catalyst',
  HEATED_CATALYST: 'Heated Catalyst',
  EVAPORATIVE_SYSTEM: 'Evaporative System',
  SECONDARY_AIR_SYSTEM: 'Secondary Air System',
  OXYGEN_SENSOR: 'Oxygen Sensor',
  OXYGEN_SENSOR_HEATER: 'Oxygen Sensor Heater',
  EGR_AND_VVT: 'EGR/VVT',
}

function describeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocorreu um erro inesperado ao consultar o veículo.'
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

function summarizeMonitors(reading: ReadinessReading | null) {
  if (!reading) {
    return null
  }

  const supported = reading.monitors.filter((monitor) => monitor.supported)
  return {
    supported: supported.length,
    complete: supported.filter((monitor) => monitor.complete === true).length,
    incomplete: supported.filter((monitor) => monitor.complete === false).length,
  }
}

function ignitionLabel(type: IgnitionType): string {
  return type === 'SPARK_IGNITION'
    ? 'Ignição por centelha'
    : 'Ignição por compressão'
}

function monitorState(monitor: MonitorDTO): {
  label: string
  className: string
} {
  if (!monitor.supported) {
    return { label: 'Não suportado', className: 'is-unsupported' }
  }
  if (monitor.complete === true) {
    return { label: 'Concluído', className: 'is-complete' }
  }
  if (monitor.complete === false) {
    return { label: 'Pendente', className: 'is-pending' }
  }
  return { label: 'Status indisponível', className: 'is-unknown' }
}

export function DtcPage() {
  const clearInFlightRef = useRef(false)
  const [activeTab, setActiveTab] = useState<DiagnosticTab>('dtc')
  const [readings, setReadings] = useState<Partial<Record<DtcType, DtcReading>>>({})
  const [loading, setLoading] = useState<DtcType[]>([])
  const [errors, setErrors] = useState<Partial<Record<DtcType, string>>>({})
  const [isClearing, setIsClearing] = useState(false)
  const [clearError, setClearError] = useState<string | null>(null)
  const [clearMessage, setClearMessage] = useState<string | null>(null)
  const [monitorSinceClear, setMonitorSinceClear] =
    useState<MonitorStatusDTO | null>(null)
  const [monitorThisCycle, setMonitorThisCycle] =
    useState<MonitorStatusCycleDTO | null>(null)
  const [monitorLoading, setMonitorLoading] = useState<ReadinessContext[]>([])
  const [monitorErrors, setMonitorErrors] =
    useState<Partial<Record<ReadinessContext, string>>>({})

  const hasDtcReading = Object.values(readings).some(
    (reading) => reading !== undefined,
  )
  const dtcCount = new Set(
    Object.values(readings).flatMap((reading) => reading?.codes ?? []),
  ).size
  const sinceClearSummary = summarizeMonitors(monitorSinceClear)

  async function handleRead(type: DtcType) {
    if (isClearing || loading.includes(type)) {
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
    if (isClearing) {
      return
    }
    await Promise.all(DTC_TYPES.map(({ type }) => handleRead(type)))
  }

  async function handleClearDtcs() {
    if (
      clearInFlightRef.current ||
      isClearing ||
      loading.length > 0 ||
      monitorLoading.length > 0
    ) {
      return
    }

    clearInFlightRef.current = true
    const confirmed = window.confirm(
      'Tem certeza de que deseja apagar os códigos de falha da ECU? ' +
        'Esta ação também reinicia os monitores de prontidão e não pode ser desfeita.',
    )
    if (!confirmed) {
      clearInFlightRef.current = false
      return
    }

    setIsClearing(true)
    setClearError(null)
    setClearMessage(null)
    try {
      await clearDtcs()
      setReadings({})
      setErrors({})
      setMonitorSinceClear(null)
      setMonitorThisCycle(null)
      setMonitorErrors({})
      setClearMessage(
        'A ECU confirmou a limpeza. Consulte novamente os DTCs e monitores para obter o estado atualizado.',
      )
    } catch (error) {
      setClearError(describeError(error))
    } finally {
      clearInFlightRef.current = false
      setIsClearing(false)
    }
  }

  async function handleReadMonitor(context: ReadinessContext) {
    if (isClearing || monitorLoading.includes(context)) {
      return
    }

    setMonitorLoading((current) => [...current, context])
    setMonitorErrors((current) => ({ ...current, [context]: undefined }))
    try {
      if (context === 'since-clear') {
        setMonitorSinceClear(await readMonitorStatusSinceClear())
      } else {
        setMonitorThisCycle(await readMonitorStatusThisCycle())
      }
    } catch (error) {
      setMonitorErrors((current) => ({
        ...current,
        [context]: describeError(error),
      }))
    } finally {
      setMonitorLoading((current) =>
        current.filter((item) => item !== context),
      )
    }
  }

  const readinessCards: Array<{
    context: ReadinessContext
    pid: string
    title: string
    description: string
    reading: ReadinessReading | null
  }> = [
    {
      context: 'since-clear',
      pid: '01',
      title: 'Desde a limpeza dos DTCs',
      description: 'Status dos monitores desde que os códigos foram apagados.',
      reading: monitorSinceClear,
    },
    {
      context: 'driving-cycle',
      pid: '41',
      title: 'Ciclo de condução atual',
      description: 'Status dos monitores no ciclo de condução atual.',
      reading: monitorThisCycle,
    },
  ]

  return (
    <>
      <SectionHeading section="dtc" />

      <section className="diagnostic-overview" aria-label="Resumo do diagnóstico">
        <article className="diagnostic-metric">
          <span>Falhas registradas</span>
          <strong>{hasDtcReading ? dtcCount : '—'}</strong>
          <small>
            {hasDtcReading
              ? 'DTCs únicos nas categorias consultadas'
              : 'Nenhuma consulta realizada'}
          </small>
        </article>
        <article className="diagnostic-metric">
          <span>Monitores suportados</span>
          <strong>{sinceClearSummary?.supported ?? '—'}</strong>
          <small>
            {monitorSinceClear
              ? 'PID 01 · desde a limpeza'
              : 'PID 01 ainda não consultado'}
          </small>
        </article>
        <article className="diagnostic-metric">
          <span>Monitores incompletos</span>
          <strong>{sinceClearSummary?.incomplete ?? '—'}</strong>
          <small>
            {monitorSinceClear
              ? 'Somente monitores suportados · PID 01'
              : 'PID 01 ainda não consultado'}
          </small>
        </article>
        <article className="diagnostic-metric">
          <span>Estado da MIL</span>
          <strong className="diagnostic-mil-value">
            <svg
              aria-hidden="true"
              className={`diagnostic-mil-icon ${
                monitorSinceClear
                  ? monitorSinceClear.milOn
                    ? 'is-on'
                    : 'is-off'
                  : 'is-unknown'
              }`}
              fill="none"
              viewBox="0 0 48 36"
            >
              <path d="M7 14h6l4-5h11l4 5h5l4 4h4v11h-8l-4 4H15l-4-4H7z" />
              <path d="M19 9V5h8v4M17 19v6m10-6v6M7 19H3v7h4" />
            </svg>
            <span>
              {monitorSinceClear
                ? monitorSinceClear.milOn
                  ? 'Acesa'
                  : 'Apagada'
                : '—'}
            </span>
          </strong>
          <small>
            {monitorSinceClear
              ? `${monitorSinceClear.DtcS} DTC(s) solicitando MIL · PID 01`
              : 'PID 01 ainda não consultado'}
          </small>
        </article>
      </section>

      <div className="diagnostic-tabs" role="tablist" aria-label="Áreas do diagnóstico">
        {DIAGNOSTIC_TABS.map((tab) => (
          <button
            aria-controls={`diagnostic-panel-${tab.id}`}
            aria-selected={activeTab === tab.id}
            className="diagnostic-tab"
            id={`diagnostic-tab-${tab.id}`}
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            role="tab"
            type="button"
          >
            {tab.label}
          </button>
        ))}
      </div>

      <section
        aria-labelledby={`diagnostic-tab-${activeTab}`}
        className="diagnostic-panel"
        id={`diagnostic-panel-${activeTab}`}
        role="tabpanel"
      >
        {activeTab === 'dtc' && (
          <>
            <div className="dtc-toolbar">
              <div>
                <p className="dtc-toolbar-kicker">Falhas e códigos</p>
                <h3 className="dtc-toolbar-title">Códigos de falha</h3>
              </div>
              <button
                className="button-primary"
                disabled={loading.length > 0 || monitorLoading.length > 0 || isClearing}
                onClick={() => void handleReadAll()}
                type="button"
              >
                {loading.length > 0 ? 'Consultando…' : 'Consultar todos'}
              </button>
              <button
                className="button-danger dtc-clear-button"
                disabled={loading.length > 0 || monitorLoading.length > 0 || isClearing}
                onClick={() => void handleClearDtcs()}
                type="button"
              >
                {isClearing ? 'Apagando…' : 'Apagar códigos da ECU'}
              </button>
            </div>

            {clearError && (
              <p className="dtc-error" role="alert">{clearError}</p>
            )}
            {clearMessage && (
              <p className="dtc-success" role="status">{clearMessage}</p>
            )}

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
                      <span className="dtc-count">
                        {reading ? codes.length : '—'}
                      </span>
                    </div>

                    <p className="dtc-description">{description}</p>

                    {error && (
                      <p className="dtc-error" role="alert">{error}</p>
                    )}

                    {isLoading ? (
                      <div className="dtc-empty" role="status">
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
                      disabled={isLoading || loading.length > 0 || isClearing}
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
        )}

        {activeTab === 'readiness' && (
          <>
            <div className="readiness-heading">
              <div>
                <p className="dtc-toolbar-kicker">Mode 01</p>
                <h3>Monitores de prontidão</h3>
              </div>
              <p>
                Os dados desde a limpeza (PID 01) e do ciclo atual (PID 41)
                são consultados e apresentados separadamente.
              </p>
            </div>

            <div className="readiness-notice">
              Concluído significa que o teste do monitor foi executado; não
              significa que o componente foi aprovado. Monitores não suportados
              não são contabilizados como pendentes.
            </div>

            <div className="readiness-grid">
              {readinessCards.map((card) => {
                const summary = summarizeMonitors(card.reading)
                const isLoading = monitorLoading.includes(card.context)
                const error = monitorErrors[card.context]
                const milStatus =
                  card.reading && 'milOn' in card.reading
                    ? card.reading
                    : null

                return (
                  <article className="readiness-card" key={card.context}>
                    <div className="readiness-card-heading">
                      <div>
                        <p className="dtc-card-kicker">PID {card.pid}</p>
                        <h3>{card.title}</h3>
                      </div>
                      <span className="readiness-context">
                        {card.pid === '01' ? 'Desde a limpeza' : 'Ciclo atual'}
                      </span>
                    </div>
                    <p className="readiness-description">{card.description}</p>

                    {error && <p className="dtc-error" role="alert">{error}</p>}

                    {card.reading && (
                      <>
                        <div className="readiness-summary">
                          <span>
                            <strong>{summary?.supported ?? 0}</strong>
                            Suportados
                          </span>
                          <span>
                            <strong>{summary?.complete ?? 0}</strong>
                            Concluídos
                          </span>
                          <span>
                            <strong>{summary?.incomplete ?? 0}</strong>
                            Pendentes
                          </span>
                        </div>
                        <div className="readiness-meta">
                          <span>
                            Ignição: {ignitionLabel(card.reading.ignitionType)}
                          </span>
                          {milStatus && (
                            <>
                              <span>MIL: {milStatus.milOn ? 'Acesa' : 'Apagada'}</span>
                              <span>DTCs solicitando MIL: {milStatus.DtcS}</span>
                            </>
                          )}
                        </div>
                      </>
                    )}

                    {isLoading ? (
                      <div className="readiness-empty" role="status">
                        Consultando a ECU…
                      </div>
                    ) : error ? null : !card.reading ? (
                      <div className="readiness-empty">
                        Nenhuma consulta realizada.
                      </div>
                    ) : card.reading.monitors.length === 0 ? (
                      <div className="readiness-empty" role="status">
                        Consulta realizada, sem monitores retornados.
                      </div>
                    ) : (
                      <ul className="readiness-monitor-list">
                        {card.reading.monitors.map((monitor) => {
                          const state = monitorState(monitor)
                          return (
                            <li
                              className="readiness-monitor"
                              key={monitor.type}
                            >
                              <span>{MONITOR_LABELS[monitor.type]}</span>
                              <span className={`monitor-state ${state.className}`}>
                                {state.label}
                              </span>
                            </li>
                          )
                        })}
                      </ul>
                    )}

                    <button
                      className="button-secondary dtc-read-button"
                      disabled={isLoading || isClearing}
                      onClick={() => void handleReadMonitor(card.context)}
                      type="button"
                    >
                      {isLoading
                        ? 'Consultando…'
                        : card.reading
                          ? 'Consultar novamente'
                          : 'Consultar'}
                    </button>
                  </article>
                )
              })}
            </div>

            <p className="readiness-ignition-note">
              O backend atual interpreta a tabela de monitores para ignição por
              centelha. Em motores por compressão, a consulta pode retornar erro
              até que essa interpretação seja implementada.
            </p>
          </>
        )}

        {activeTab === 'ecu-tests' && (
          <section className="ecu-tests-unavailable">
            <span className="dtc-card-kicker">Mode 06</span>
            <h3>Testes internos da ECU</h3>
            <p>
              Esta área está reservada para resultados de testes de diagnóstico
              disponibilizados pela unidade de controle.
            </p>
            <span className="ecu-tests-status">Ainda não implementado no backend</span>
            <p className="ecu-tests-note">
              Nenhuma consulta é enviada e nenhum resultado é simulado.
            </p>
          </section>
        )}
      </section>
    </>
  )
}
