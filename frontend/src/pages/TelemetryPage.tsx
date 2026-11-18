import { useEffect, useRef, useState } from 'react'
import { PidSelector } from '../components/telemetry/PidSelector'
import { ReadingCard } from '../components/telemetry/ReadingCard'
import { readPid, readPids } from '../api/telemetryService'
import type { ObdReading, TelemetrySample } from '../types/telemetry'
import { SectionHeading } from './SectionHeading'
import './TelemetryPage.css'

const DEFAULT_PIDS = ['0C', '0D', '05', '04']
const MIN_UPDATE_INTERVAL_MS = 1000
const HISTORY_WINDOW_MS = 60_000
const MAX_HISTORY_POINTS = 60

type TelemetryHistory = Record<string, TelemetrySample[][]>

function appendHistory(
  history: TelemetryHistory,
  readings: ObdReading[],
  receivedAt: number,
): TelemetryHistory {
  const cutoff = receivedAt - HISTORY_WINDOW_MS
  const nextHistory: TelemetryHistory = {}

  for (const [pid, metrics] of Object.entries(history)) {
    const recentMetrics = metrics.map((samples) =>
      samples
        .filter((sample) => sample.receivedAt >= cutoff)
        .slice(-MAX_HISTORY_POINTS),
    )
    if (recentMetrics.some((samples) => samples.length > 0)) {
      nextHistory[pid] = recentMetrics
    }
  }

  for (const reading of readings) {
    const metrics = [...(nextHistory[reading.pid] ?? [])]
    reading.values.forEach((value, index) => {
      metrics[index] = [
        ...(metrics[index] ?? []),
        { receivedAt, value: value.value },
      ].slice(-MAX_HISTORY_POINTS)
    })
    nextHistory[reading.pid] = metrics
  }

  return nextHistory
}

function describeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocorreu um erro inesperado ao consultar a telemetria.'
}

export function TelemetryPage() {
  const [selectedPids, setSelectedPids] = useState<string[]>(DEFAULT_PIDS)
  const [intervalMs, setIntervalMs] = useState(1000)
  const [readings, setReadings] = useState<ObdReading[]>([])
  const [history, setHistory] = useState<TelemetryHistory>({})
  const [isReading, setIsReading] = useState(false)
  const [readingPid, setReadingPid] = useState<string | null>(null)
  const [readingError, setReadingError] = useState<string | null>(null)
  const [isLiveUpdating, setIsLiveUpdating] = useState(false)
  const [liveError, setLiveError] = useState<string | null>(null)
  const selectedPidsRef = useRef(selectedPids)
  const requestInFlightRef = useRef(false)

  const readingValueCount = readings.reduce(
    (count, reading) => count + reading.values.length,
    0,
  )
  const isIntervalValid =
    intervalMs >= MIN_UPDATE_INTERVAL_MS && Number.isInteger(intervalMs)

  function togglePid(pid: string) {
    const updatedPids = selectedPids.includes(pid)
      ? selectedPids.filter((selected) => selected !== pid)
      : [...selectedPids, pid]

    selectedPidsRef.current = updatedPids
    setSelectedPids(updatedPids)

    if (updatedPids.length === 0 && isLiveUpdating) {
      setIsLiveUpdating(false)
      setLiveError('Atualização ao vivo encerrada: selecione ao menos um PID.')
    }
  }

  async function handleReadSelected() {
    if (
      selectedPids.length === 0 ||
      isReading ||
      isLiveUpdating ||
      requestInFlightRef.current
    ) {
      return
    }

    requestInFlightRef.current = true
    setIsReading(true)
    setReadingError(null)
    try {
      setReadings(await readPids(selectedPids))
    } catch (error) {
      setReadingError(describeError(error))
    } finally {
      requestInFlightRef.current = false
      setIsReading(false)
    }
  }

  async function handleReadOne(pid: string) {
    if (isReading || isLiveUpdating || requestInFlightRef.current) {
      return
    }

    requestInFlightRef.current = true
    setIsReading(true)
    setReadingPid(pid)
    setReadingError(null)
    try {
      const reading = await readPid(pid)
      setReadings((current) => [
        ...current.filter((item) => item.pid !== reading.pid),
        reading,
      ])
    } catch (error) {
      setReadingError(describeError(error))
    } finally {
      requestInFlightRef.current = false
      setReadingPid(null)
      setIsReading(false)
    }
  }

  function toggleLiveUpdating() {
    if (isLiveUpdating) {
      setIsLiveUpdating(false)
      setLiveError(null)
      return
    }

    if (selectedPids.length === 0) {
      setLiveError('Selecione ao menos um PID para iniciar a atualização.')
      return
    }

    if (!isIntervalValid) {
      setLiveError('Informe um intervalo inteiro de pelo menos 1000 ms.')
      return
    }

    setLiveError(null)
    setReadingError(null)
    setIsLiveUpdating(true)
  }

  useEffect(() => {
    if (!isLiveUpdating) {
      return
    }

    let active = true
    const updateReadings = () => {
      const pids = selectedPidsRef.current
      if (!active || pids.length === 0 || requestInFlightRef.current) {
        return
      }

      requestInFlightRef.current = true
      void readPids(pids)
        .then((nextReadings) => {
          if (active) {
            const receivedAt = Date.now()
            setReadings(nextReadings)
            setHistory((current) =>
              appendHistory(current, nextReadings, receivedAt),
            )
            setReadingError(null)
            setLiveError(null)
          }
        })
        .catch((error: unknown) => {
          if (active) {
            setLiveError(describeError(error))
          }
        })
        .finally(() => {
          requestInFlightRef.current = false
        })
    }

    updateReadings()
    const intervalId = window.setInterval(updateReadings, intervalMs)

    return () => {
      active = false
      window.clearInterval(intervalId)
    }
  }, [intervalMs, isLiveUpdating])

  return (
    <>
      <SectionHeading section="telemetry" />

      <div className="telemetry-grid">
        <div className="telemetry-side">
          <PidSelector
            selectedPids={selectedPids}
            isReading={isReading}
            readingPid={readingPid}
            isLiveUpdating={isLiveUpdating}
            onTogglePid={togglePid}
            onReadPid={(pid) => void handleReadOne(pid)}
            onReadSelected={() => void handleReadSelected()}
          />

          <section className="telemetry-panel live-panel">
            <div className="telemetry-panel-heading">
              <div>
                <p className="telemetry-panel-kicker">Atualização automática</p>
                <h3 className="telemetry-panel-title">Acompanhar ao vivo</h3>
              </div>
              <span
                className={`live-state ${
                  isLiveUpdating ? 'is-active' : 'is-paused'
                }`}
              >
                <span className="state-dot" aria-hidden="true" />
                {isLiveUpdating ? 'Ativa' : 'Pausada'}
              </span>
            </div>

            <label className="interval-field">
              <span>Atualizar a cada</span>
              <span className="interval-input-wrap">
                <input
                  disabled={isLiveUpdating}
                  min={MIN_UPDATE_INTERVAL_MS}
                  onChange={(event) =>
                    setIntervalMs(Number(event.currentTarget.value))
                  }
                  step={250}
                  type="number"
                  value={intervalMs}
                />
                <span>ms</span>
              </span>
            </label>
            <p className="interval-hint">
              Mínimo de 1000 ms. Se uma consulta ainda estiver em andamento, a
              próxima tentativa será ignorada para evitar sobreposição.
            </p>
            {!isIntervalValid && (
              <p className="telemetry-error compact-error" role="alert">
                Informe um intervalo inteiro de pelo menos 1000 ms.
              </p>
            )}

            <button
              className={`${
                isLiveUpdating ? 'button-secondary' : 'button-primary'
              } live-toggle`}
              disabled={
                !isLiveUpdating &&
                (selectedPids.length === 0 ||
                  !isIntervalValid ||
                  isReading)
              }
              onClick={toggleLiveUpdating}
              type="button"
            >
              {isLiveUpdating ? 'Parar atualização' : 'Iniciar ao vivo'}
            </button>

            {liveError && (
              <p className="telemetry-error compact-error" role="alert">
                {liveError}
              </p>
            )}

            <p className="live-note">
              Ao iniciar, a tela consulta os PIDs selecionados e substitui os
              valores pelos retornos mais recentes. A frequência real também
              depende do tempo de resposta do adaptador.
            </p>
          </section>
        </div>

        <section className="telemetry-panel readings-panel">
          <div className="telemetry-panel-heading">
            <div>
              <p className="telemetry-panel-kicker">Resposta do veículo</p>
              <h3 className="telemetry-panel-title">Leituras</h3>
            </div>
            <span className="reading-count">{readingValueCount}</span>
          </div>

          {isReading && (
            <p className="reading-status" role="status">
              Consultando o adaptador OBD-II…
            </p>
          )}

          {readingError && (
            <p className="telemetry-error" role="alert">
              {readingError}
            </p>
          )}

          {readings.length === 0 ? (
            <div className="reading-empty">
              <span className="empty-glyph" aria-hidden="true">
                —
              </span>
              <p>Nenhuma leitura disponível.</p>
              <span>Faça uma leitura ou inicie a atualização ao vivo.</span>
            </div>
          ) : readingValueCount === 0 ? (
            <div className="reading-empty">
              <span className="empty-glyph" aria-hidden="true">
                —
              </span>
              <p>A resposta não contém valores para exibir.</p>
              <span>Os PIDs consultados não retornaram valores.</span>
            </div>
          ) : (
            <div className="reading-grid">
              {readings
                .filter((reading) => reading.values.length > 0)
                .map((reading) => (
                  <ReadingCard
                    historyByMetric={history[reading.pid] ?? []}
                    isLiveUpdating={isLiveUpdating}
                    key={reading.pid}
                    reading={reading}
                  />
                ))}
            </div>
          )}
        </section>
      </div>
    </>
  )
}
