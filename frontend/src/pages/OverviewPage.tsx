import { lazy, Suspense, useCallback, useMemo, useRef, useState } from 'react'
import { readPid } from '../api/telemetryService'
import { PID_OPTIONS } from '../data/obdPids'
import {
  PID_LOCATIONS,
  PID_ZONE_COLORS,
  PID_ZONE_LABELS,
  type PidZone,
  VEHICLE_COMPONENTS,
} from '../data/pidLocations'
import type { ObdReading } from '../types/telemetry'
import { SectionHeading } from './SectionHeading'
import './OverviewPage.css'

const VehicleScene = lazy(() =>
  import('../components/overview/VehicleScene').then((module) => ({
    default: module.VehicleScene,
  })),
)

const PID_ZONES = Object.keys(PID_ZONE_LABELS) as PidZone[]

function describeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocorreu um erro inesperado ao consultar a telemetria.'
}

function formatTimestamp(timestamp: string): string {
  const date = new Date(timestamp)
  return Number.isNaN(date.getTime())
    ? timestamp
    : new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'medium',
      }).format(date)
}

export function OverviewPage() {
  const [selectedPidCode, setSelectedPidCode] = useState<string | null>(null)
  const [selectedComponentId, setSelectedComponentId] = useState<string | null>(
    null,
  )
  const [filter, setFilter] = useState('')
  const [reading, setReading] = useState<ObdReading | null>(null)
  const [isReading, setIsReading] = useState(false)
  const [readingError, setReadingError] = useState<string | null>(null)
  const requestInFlightRef = useRef(false)

  const selectedPid = PID_OPTIONS.find((pid) => pid.code === selectedPidCode)
  const selectedComponent = selectedComponentId
    ? VEHICLE_COMPONENTS[selectedComponentId]
    : undefined
  const componentPids = selectedComponentId
    ? PID_OPTIONS.filter(
        (pid) =>
          PID_LOCATIONS[pid.code]?.componentId === selectedComponentId,
      )
    : []
  const visiblePids = useMemo(() => {
    const query = filter.trim().toLocaleLowerCase('pt-BR')
    if (!query) {
      return PID_OPTIONS
    }

    return PID_OPTIONS.filter((pid) => {
      const location = PID_LOCATIONS[pid.code]
      return `${pid.code} ${pid.name} ${pid.description} ${location.area}`
        .toLocaleLowerCase('pt-BR')
        .includes(query)
    })
  }, [filter])

  const handleSelectPid = useCallback((code: string) => {
    if (requestInFlightRef.current) {
      return
    }

    const pid = PID_OPTIONS.find((option) => option.code === code)
    const location = PID_LOCATIONS[code]
    if (!pid || !location) {
      setReadingError(`O PID ${code} não está no catálogo disponível.`)
      return
    }

    requestInFlightRef.current = true
    setSelectedPidCode(code)
    setSelectedComponentId(location.componentId)
    setReading(null)
    setReadingError(null)
    setIsReading(true)

    void readPid(code)
      .then(setReading)
      .catch((error: unknown) => setReadingError(describeError(error)))
      .finally(() => {
        requestInFlightRef.current = false
        setIsReading(false)
      })
  }, [])

  const handleSelectComponent = useCallback(
    (componentId: string) => {
      if (requestInFlightRef.current) {
        return
      }

      const component = VEHICLE_COMPONENTS[componentId]
      const relatedPids = PID_OPTIONS.filter(
        (pid) => PID_LOCATIONS[pid.code]?.componentId === componentId,
      )
      if (!component || relatedPids.length === 0) {
        setReadingError(
          `Não foi possível encontrar PIDs associados ao componente ${componentId}.`,
        )
        return
      }

      setSelectedComponentId(componentId)
      setSelectedPidCode(null)
      setReading(null)
      setReadingError(null)

      if (relatedPids.length === 1) {
        handleSelectPid(relatedPids[0].code)
      }
    },
    [handleSelectPid],
  )

  return (
    <>
      <SectionHeading section="overview" />

      <div className="overview-layout">
        <section className="overview-panel overview-vehicle-panel">
          <div className="overview-panel-heading">
            <div>
              <p className="overview-kicker">Mapa interativo</p>
              <h3 className="overview-panel-title">Componentes do veículo</h3>
            </div>
            <span className="overview-total">{PID_OPTIONS.length} PIDs</span>
          </div>

          <Suspense
            fallback={
              <div className="vehicle-scene-loading" role="status">
                Carregando modelo 3D…
              </div>
            }
          >
            <VehicleScene
              onSelectComponent={handleSelectComponent}
              pids={PID_OPTIONS}
              selectedComponentId={selectedComponentId}
            />
          </Suspense>

          <div className="overview-legend" aria-label="Legenda dos sistemas">
            {PID_ZONES.map((zone) => (
              <span className="overview-legend-item" key={zone}>
                <span
                  className="overview-legend-dot"
                  style={{ backgroundColor: PID_ZONE_COLORS[zone] }}
                  aria-hidden="true"
                />
                {PID_ZONE_LABELS[zone]}
              </span>
            ))}
          </div>
          <p className="overview-local-note">
            As peças do modelo são clicáveis. Componentes associados a mais de
            um PID mostram as leituras disponíveis antes da consulta.
          </p>
        </section>

        <aside className="overview-side">
          <section
            className="overview-panel overview-reading-panel"
            aria-labelledby="overview-reading-title"
          >
            <div className="overview-panel-heading">
              <div>
                <p className="overview-kicker">Componente selecionado</p>
                <h3
                  className="overview-panel-title"
                  id="overview-reading-title"
                >
                  Peça e leitura
                </h3>
              </div>
              {isReading && (
                <span className="overview-reading-indicator" role="status">
                  Consultando
                </span>
              )}
            </div>

            {selectedComponent && (
              <div className="overview-component-details">
                <div className="overview-component-heading">
                  <span
                    className="overview-component-mark"
                    style={{
                      borderColor:
                        PID_ZONE_COLORS[selectedComponent.zone],
                      color: PID_ZONE_COLORS[selectedComponent.zone],
                    }}
                    aria-hidden="true"
                  >
                    {selectedComponent.kind === 'lambda-probe'
                      ? 'λ'
                      : selectedComponent.kind === 'catalyst'
                        ? 'CAT'
                        : selectedComponent.kind === 'ecu'
                          ? 'ECU'
                          : '◆'}
                  </span>
                  <div>
                    <h4>{selectedComponent.label}</h4>
                    <span>{PID_ZONE_LABELS[selectedComponent.zone]}</span>
                  </div>
                </div>
                <p className="overview-pid-info">
                  {selectedComponent.description}
                </p>

                {componentPids.length > 1 && (
                  <div className="overview-component-pids">
                    <span>Escolha uma leitura deste componente</span>
                    {componentPids.map((pid) => (
                      <button
                        disabled={isReading}
                        key={pid.code}
                        onClick={() => handleSelectPid(pid.code)}
                        type="button"
                      >
                        <code>{pid.code}</code>
                        <span>{pid.name}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}

            {selectedPid && (
              <div className="overview-selected-pid">
                <div className="overview-selected-heading">
                  <span
                    className="overview-pid-code"
                    style={{
                      borderColor: PID_ZONE_COLORS[PID_LOCATIONS[selectedPid.code].zone],
                      color: PID_ZONE_COLORS[PID_LOCATIONS[selectedPid.code].zone],
                    }}
                  >
                    {selectedPid.code}
                  </span>
                  <div>
                    <h4>{selectedPid.name}</h4>
                    <span>{PID_LOCATIONS[selectedPid.code].area}</span>
                  </div>
                </div>
                <p className="overview-pid-info">{selectedPid.info}</p>

                {readingError && (
                  <p className="overview-error" role="alert">
                    {readingError}
                  </p>
                )}

                {reading && reading.values.length > 0 && (
                  <div className="overview-values">
                    {reading.values.map((value, index) => (
                      <div
                        className="overview-value-row"
                        key={`${value.name}-${index}`}
                      >
                        <span>{value.name}</span>
                        <strong>
                          {value.value === null
                            ? 'Indisponível'
                            : `${value.value.toLocaleString('pt-BR')} ${value.unit}`}
                        </strong>
                      </div>
                    ))}
                    <time
                      className="overview-timestamp"
                      dateTime={reading.timestamp}
                    >
                      Consultado às {formatTimestamp(reading.timestamp)}
                    </time>
                  </div>
                )}

                {reading && reading.values.length === 0 && (
                  <p className="overview-reading-empty" role="status">
                    A ECU não retornou valores para este PID.
                  </p>
                )}

                {!isReading && !reading && !readingError && (
                  <p className="overview-reading-empty">
                    Selecione novamente este PID para consultar o veículo.
                  </p>
                )}
              </div>
            )}

            {!selectedComponent && (
              <div className="overview-reading-empty">
                <span className="overview-empty-mark" aria-hidden="true">
                  ⚙
                </span>
                <p>Selecione uma peça realçada no modelo 3D.</p>
                <span>O resultado aparecerá aqui após a consulta.</span>
              </div>
            )}
          </section>

          <section
            className="overview-panel overview-pid-panel"
            aria-labelledby="overview-pids-title"
          >
            <div className="overview-panel-heading">
              <div>
                <p className="overview-kicker">Catálogo OBD-II</p>
                <h3 className="overview-panel-title" id="overview-pids-title">
                  PIDs disponíveis
                </h3>
              </div>
              <span className="overview-count">
                {visiblePids.length}/{PID_OPTIONS.length}
              </span>
            </div>

            <label className="overview-filter">
              <span className="visually-hidden">
                Filtrar PIDs por nome, código ou componente
              </span>
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <circle
                  cx="8.75"
                  cy="8.75"
                  r="5.75"
                  stroke="currentColor"
                  strokeWidth="1.5"
                />
                <path
                  d="m13 13 4 4"
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeWidth="1.5"
                />
              </svg>
              <input
                onChange={(event) => setFilter(event.currentTarget.value)}
                placeholder="Buscar PID ou componente"
                type="search"
                value={filter}
              />
            </label>

            <div className="overview-pid-list" aria-label="Lista de PIDs">
              {visiblePids.map((pid) => {
                const location = PID_LOCATIONS[pid.code]
                const selected = selectedPidCode === pid.code

                return (
                  <button
                    aria-pressed={selected}
                    className="overview-pid-option"
                    disabled={isReading}
                    key={pid.code}
                    onClick={() => handleSelectPid(pid.code)}
                    type="button"
                  >
                    <span
                      className="overview-pid-option-code"
                      style={{
                        borderColor: PID_ZONE_COLORS[location.zone],
                        color: PID_ZONE_COLORS[location.zone],
                      }}
                    >
                      {pid.code}
                    </span>
                    <span className="overview-pid-option-copy">
                      <span>{pid.name}</span>
                      <span>{location.area}</span>
                    </span>
                    <span className="overview-pid-option-arrow" aria-hidden="true">
                      {selected ? '•' : '↗'}
                    </span>
                  </button>
                )
              })}
              {visiblePids.length === 0 && (
                <p className="overview-no-results">
                  Nenhum PID corresponde à busca.
                </p>
              )}
            </div>
          </section>
        </aside>
      </div>
    </>
  )
}
