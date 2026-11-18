import type {
  ObdReading,
  ObdValue,
  TelemetrySample,
} from '../../types/telemetry'

interface ReadingCardProps {
  reading: ObdReading
  historyByMetric: TelemetrySample[][]
  isLiveUpdating: boolean
}

const HISTORY_WINDOW_MS = 60_000

type VisualKind =
  | 'rpm'
  | 'speed'
  | 'temperature'
  | 'percent'
  | 'fuel-trim'
  | 'voltage'
  | 'temperature-number'
  | 'number'

const VALUE_LABELS: Record<string, string> = {
  fuelSystemBank1Status: 'Combustível · Banco 1',
  fuelSystemBank2Status: 'Combustível · Banco 2',
  voltage: 'Tensão',
  fuelTrim: 'Ajuste de combustível',
  equivalenceRatio: 'Razão de equivalência',
  bitmask: 'Máscara de bits',
}

function visualKind(pid: string, value: ObdValue): VisualKind {
  if (pid === '0C') return 'rpm'
  if (pid === '0D') return 'speed'
  if (['05', '0F', '46'].includes(pid)) return 'temperature'
  if (value.unit === '°C') return 'temperature-number'
  if (
    value.unit === '%' &&
    /fuel.?trim|ajuste de combustível/i.test(value.name)
  ) {
    return 'fuel-trim'
  }
  if (value.unit === '%') return 'percent'
  if (value.unit === 'V' && pid === '42') return 'voltage'
  return 'number'
}

function formatValue(value: number | null): string {
  if (value === null) return 'Sem leitura'

  return new Intl.NumberFormat('pt-BR', {
    maximumFractionDigits: 2,
  }).format(value)
}

function formatTimestamp(timestamp: string): string {
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) return timestamp

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(date)
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value))
}

interface ChartPoint {
  x: number
  y: number
}

function createSegments(points: Array<ChartPoint | null>): string[] {
  const segments: string[] = []
  let segment: string[] = []

  for (const point of points) {
    if (point === null) {
      if (segment.length > 1) segments.push(segment.join(' '))
      segment = []
      continue
    }

    segment.push(`${segment.length === 0 ? 'M' : 'L'} ${point.x} ${point.y}`)
  }

  if (segment.length > 1) segments.push(segment.join(' '))
  return segments
}

function Sparkline({
  name,
  unit,
  samples,
  isLiveUpdating,
}: {
  name: string
  unit: string
  samples: TelemetrySample[]
  isLiveUpdating: boolean
}) {
  const chartAt = samples.reduce(
    (latest, sample) => Math.max(latest, sample.receivedAt),
    0,
  )
  const recentSamples = samples.filter(
    (sample) => sample.receivedAt >= chartAt - HISTORY_WINDOW_MS,
  )
  if (recentSamples.length === 0) return null

  const values = recentSamples.flatMap((sample) =>
    sample.value === null ? [] : [sample.value],
  )
  const min = values.length > 0 ? Math.min(...values) : null
  const max = values.length > 0 ? Math.max(...values) : null
  const range = min === null || max === null ? 0 : max - min
  const points = recentSamples.map((sample) => {
    if (sample.value === null || min === null || max === null) return null

    return {
      x: Number(
        (
          4 +
          clamp(
            (sample.receivedAt - (chartAt - HISTORY_WINDOW_MS)) /
              HISTORY_WINDOW_MS,
            0,
            1,
          ) *
            172
        ).toFixed(2),
      ),
      y:
        range === 0
          ? 19
          : Number((4 + ((max - sample.value) / range) * 30).toFixed(2)),
    }
  })
  const segments = createSegments(points)
  const latestNumericIndex = points.reduce(
    (latest, point, index) => (point === null ? latest : index),
    -1,
  )

  return (
    <div className="reading-sparkline">
      <div className="sparkline-heading">
        <span>Histórico ao vivo</span>
        <span>{isLiveUpdating ? 'Últimos 60 s' : 'Pausado · 60 s'}</span>
      </div>
      {min !== null && max !== null ? (
        <>
          <svg
            className="sparkline-chart"
            viewBox="0 0 180 38"
            role="img"
            aria-label={`Histórico de ${name}: ${values.length} amostras nos últimos 60 segundos, entre ${formatValue(min)} e ${formatValue(max)} ${unit}.`}
          >
            <path className="sparkline-baseline" d="M4 35.5H176" />
            {segments.map((segment, index) => (
              <path
                className="sparkline-line"
                d={segment}
                key={`${segment}-${index}`}
              />
            ))}
            {points.map(
              (point, index) =>
                point !== null && (
                  <circle
                    className={
                      index === latestNumericIndex
                        ? 'sparkline-point is-latest'
                        : 'sparkline-point'
                    }
                    cx={point.x}
                    cy={point.y}
                    key={index}
                    r={index === latestNumericIndex ? 2.4 : 1.5}
                  />
                ),
            )}
          </svg>
          <div className="sparkline-range">
            <span>
              {formatValue(min)} – {formatValue(max)} {unit}
            </span>
            {segments.length === 0 && (
              <span className="sparkline-message">
                Aguardando próxima leitura
              </span>
            )}
          </div>
        </>
      ) : (
        <p className="sparkline-message">Sem amostras numéricas.</p>
      )}
    </div>
  )
}

function Gauge({
  value,
  unit,
  max,
  scaleLabel,
}: {
  value: number | null
  unit: string
  max: number
  scaleLabel: string
}) {
  const ratio = value === null ? 0 : clamp(value / max, 0, 1)
  const angle = Math.PI + ratio * Math.PI
  const needleX = 90 + Math.cos(angle) * 48
  const needleY = 94 + Math.sin(angle) * 48
  const overScale = value !== null && value > max

  return (
    <div className="gauge-visual">
      <svg
        className="dial"
        viewBox="0 0 180 116"
        role="img"
        aria-label={`Indicador ${formatValue(value)} ${unit}`}
      >
        <path
          className="dial-track"
          d="M22 94 A68 68 0 0 1 158 94"
          pathLength="100"
        />
        <path
          className="dial-progress"
          d="M22 94 A68 68 0 0 1 158 94"
          pathLength="100"
          strokeDasharray={`${ratio * 100} 100`}
        />
        {[0, 0.25, 0.5, 0.75, 1].map((tick) => {
          const tickAngle = Math.PI + tick * Math.PI
          const innerX = 90 + Math.cos(tickAngle) * 56
          const innerY = 94 + Math.sin(tickAngle) * 56
          const outerX = 90 + Math.cos(tickAngle) * 62
          const outerY = 94 + Math.sin(tickAngle) * 62

          return (
            <line
              className="dial-tick"
              key={tick}
              x1={innerX}
              x2={outerX}
              y1={innerY}
              y2={outerY}
            />
          )
        })}
        {value !== null && (
          <>
            <line
              className="dial-needle"
              x1="90"
              x2={needleX}
              y1="94"
              y2={needleY}
            />
            <circle className="dial-hub" cx="90" cy="94" r="4" />
          </>
        )}
      </svg>
      <p className="visual-value">
        {formatValue(value)} <span>{unit}</span>
      </p>
      <div className="gauge-scale">
        <span>0</span>
        <span>{overScale ? 'Acima da escala' : scaleLabel}</span>
      </div>
    </div>
  )
}

function TemperatureVisual({ value }: { value: number | null }) {
  const ratio =
    value === null ? 0 : clamp((value + 40) / 255, 0, 1)

  return (
    <div className="temperature-visual">
      <div className="thermometer" aria-hidden="true">
        <div className="thermometer-track">
          <span style={{ height: `${ratio * 100}%` }} />
        </div>
        <span className="thermometer-bulb" />
      </div>
      <div className="temperature-reading">
        <p className="visual-value">
          {formatValue(value)} <span>°C</span>
        </p>
        <div className="temperature-scale">
          <span>Escala OBD: -40 a 215 °C</span>
          <span>Leitura do sensor</span>
        </div>
      </div>
    </div>
  )
}

function PercentVisual({
  value,
  signed = false,
}: {
  value: number | null
  signed?: boolean
}) {
  const normalized = value === null ? 0 : clamp(value, signed ? -100 : 0, 100)
  const width = signed ? Math.abs(normalized) / 2 : clamp(normalized, 0, 100)
  const overScale =
    value !== null &&
    (signed ? value < -100 || value > 100 : value < 0 || value > 100)

  return (
    <div className={signed ? 'percent-visual is-signed' : 'percent-visual'}>
      <p className="visual-value">
        {formatValue(value)} <span>%</span>
      </p>
      {signed ? (
        <div className="trim-track" aria-hidden="true">
          <span
            className={`trim-fill ${normalized < 0 ? 'is-negative' : ''}`}
            style={{
              left: normalized < 0 ? `${50 - width}%` : '50%',
              width: `${width}%`,
            }}
          />
          <span className="trim-center" />
        </div>
      ) : (
        <div
          className="percent-track"
          role="img"
          aria-label={`Escala de 0 a 100 por cento; valor ${formatValue(value)} por cento`}
        >
          <span style={{ width: `${width}%` }} />
        </div>
      )}
      <p className="visual-scale">
        {overScale
          ? 'Acima da escala visual'
          : signed
            ? '-100%  ·  0  ·  +100%'
            : 'Escala visual: 0–100%'}
      </p>
    </div>
  )
}

function VoltageVisual({ value }: { value: number | null }) {
  return (
    <div className="voltage-visual">
      <svg viewBox="0 0 32 32" fill="none" aria-hidden="true">
        <rect
          x="4"
          y="8"
          width="22"
          height="16"
          rx="3"
          stroke="currentColor"
          strokeWidth="1.7"
        />
        <path
          d="M27 13h2v6h-2M10 16h10M15 12v8"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="1.7"
        />
      </svg>
      <p className="visual-value">
        {formatValue(value)} <span>V</span>
      </p>
    </div>
  )
}

function TemperatureNumberVisual({ value }: { value: number | null }) {
  return (
    <div className="temperature-number-visual">
      <svg viewBox="0 0 32 32" fill="none" aria-hidden="true">
        <path
          d="M13 18.5V7a3 3 0 0 1 6 0v11.5a6 6 0 1 1-6 0Z"
          stroke="currentColor"
          strokeLinejoin="round"
          strokeWidth="1.7"
        />
        <path
          d="M16 21V10"
          stroke="currentColor"
          strokeLinecap="round"
          strokeWidth="1.7"
        />
        <circle cx="16" cy="24" r="2" fill="currentColor" />
      </svg>
      <p className="visual-value">
        {formatValue(value)} <span>°C</span>
      </p>
    </div>
  )
}

function NumberVisual({ value, unit }: { value: number | null; unit: string }) {
  return (
    <div className="number-visual">
      <span className="number-glyph" aria-hidden="true">
        {unit === 'kPa' || unit === 'Pa' ? 'P' : unit === 'bitmask' ? 'B' : '·'}
      </span>
      <p className="visual-value">
        {formatValue(value)} {unit && <span>{unit}</span>}
      </p>
    </div>
  )
}

function MetricVisual({
  pid,
  value,
}: {
  pid: string
  value: ObdValue
}) {
  const kind = visualKind(pid, value)

  if (kind === 'rpm') {
    return <Gauge value={value.value} unit={value.unit} max={8000} scaleLabel="8.000 rpm" />
  }

  if (kind === 'speed') {
    return <Gauge value={value.value} unit={value.unit} max={255} scaleLabel="255 km/h" />
  }

  if (kind === 'temperature') {
    return <TemperatureVisual value={value.value} />
  }

  if (kind === 'percent') {
    return <PercentVisual value={value.value} />
  }

  if (kind === 'fuel-trim') {
    return <PercentVisual value={value.value} signed />
  }

  if (kind === 'voltage') {
    return <VoltageVisual value={value.value} />
  }

  if (kind === 'temperature-number') {
    return <TemperatureNumberVisual value={value.value} />
  }

  return <NumberVisual value={value.value} unit={value.unit} />
}

function metricName(name: string): string {
  return VALUE_LABELS[name] ?? name
}

export function ReadingCard({
  reading,
  historyByMetric,
  isLiveUpdating,
}: ReadingCardProps) {
  return (
    <article className="reading-card">
      <header className="reading-card-heading">
        <span className="reading-name">
          {reading.values.length === 1
            ? metricName(reading.values[0].name)
            : 'Leitura composta'}
        </span>
        <code className="pid-code">{reading.pid}</code>
      </header>
      <div className="reading-metrics">
        {reading.values.map((value, index) => (
          <section className="reading-metric" key={`${value.name}-${index}`}>
            {reading.values.length > 1 && (
              <h4 className="reading-metric-name">{metricName(value.name)}</h4>
            )}
            <MetricVisual pid={reading.pid} value={value} />
            <Sparkline
              isLiveUpdating={isLiveUpdating}
              name={metricName(value.name)}
              samples={historyByMetric[index] ?? []}
              unit={value.unit}
            />
          </section>
        ))}
      </div>
      <p className="reading-timestamp">
        Atualizado {formatTimestamp(reading.timestamp)}
      </p>
    </article>
  )
}
