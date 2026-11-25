import { useEffect, useMemo, useState } from 'react'
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { sampleCorrelation } from 'simple-statistics'
import {
  analyseSession,
  createSession,
  createSessionTemplate,
  deleteSessionTemplate,
  getSession,
  getSessionLogs,
  listSessions,
  listSessionTemplates,
  startSession,
  stopSession,
} from '../api/sessionService'
import { PID_OPTIONS } from '../data/obdPids'
import { SESSION_TEMPLATE_PRESETS } from '../data/sessionTemplates'
import type {
  CreateSessionRequest,
  LogDTO,
  SessionDTO,
  SessionTemplateDTO,
  TriggerOperator,
} from '../types/session'
import { SectionHeading } from './SectionHeading'
import './SessionsPage.css'

type View = 'history' | 'create' | 'running' | 'explore'
type ExploreMode =
  | 'dashboard'
  | 'individual'
  | 'overlay'
  | 'table'
  | 'statistics'
  | 'correlation'
  | 'ai'
type RunningMode = 'cards' | 'individual' | 'overlay' | 'table'

const STATUS_LABEL: Record<SessionDTO['status'], string> = {
  CREATED: 'Criada',
  RUNNING: 'Em execução',
  FINISHED: 'Finalizada',
  FAILED: 'Falhou',
}

const END_REASON_LABEL: Record<NonNullable<SessionDTO['endReason']>, string> = {
  TRIGGER_REACHED: 'Trigger atingido',
  MAX_DURATION_REACHED: 'Duração máxima atingida',
  USER_STOPPED: 'Encerrada pelo usuário',
  ERROR: 'Erro durante a aquisição',
}

function describeError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : 'Ocorreu um erro inesperado.'
}

function formatDate(value: string | null): string {
  if (!value) return 'Não informado'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function formatDuration(milliseconds: number): string {
  const seconds = Math.floor(milliseconds / 1000)
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remaining = seconds % 60
  return hours > 0
    ? `${hours}h ${minutes}m ${remaining}s`
    : `${minutes}m ${remaining}s`
}

function deduplicateChronologically(logs: LogDTO[]): LogDTO[] {
  const unique = new Map(logs.map((log) => [log.id, log]))
  return [...unique.values()].sort(
    (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
  )
}

async function loadAllLogs(id: number): Promise<LogDTO[]> {
  const first = await getSessionLogs(id, 0, 500)
  const logs = [...first.content]
  for (let page = 1; page < first.totalPages; page += 1) {
    const next = await getSessionLogs(id, page, 500)
    logs.push(...next.content)
  }
  return deduplicateChronologically(logs)
}

export function SessionsPage() {
  const [sessions, setSessions] = useState<SessionDTO[]>([])
  const [selected, setSelected] = useState<SessionDTO | null>(null)
  const [view, setView] = useState<View>('history')
  const [logs, setLogs] = useState<LogDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [elapsedMs, setElapsedMs] = useState(0)
  const [visiblePids, setVisiblePids] = useState<string[]>([])
  const [chartPid, setChartPid] = useState('')
  const [scatterX, setScatterX] = useState('')
  const [scatterY, setScatterY] = useState('')
  const [exploreMode, setExploreMode] = useState<ExploreMode>('dashboard')
  const [runningMode, setRunningMode] = useState<RunningMode>('cards')
  const [lowerLimit, setLowerLimit] = useState('')
  const [upperLimit, setUpperLimit] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState('')
  const [aiError, setAiError] = useState<string | null>(null)

  const [vehicle, setVehicle] = useState('')
  const [startReason, setStartReason] = useState('')
  const [selectedPids, setSelectedPids] = useState<string[]>(['0C', '0D'])
  const [intervalMs, setIntervalMs] = useState(1000)
  const [maxDurationMs, setMaxDurationMs] = useState(300000)
  const [triggerEnabled, setTriggerEnabled] = useState(false)
  const [triggerPid, setTriggerPid] = useState('0C')
  const [triggerIndex, setTriggerIndex] = useState(0)
  const [triggerOperator, setTriggerOperator] =
    useState<TriggerOperator>('GTE')
  const [triggerValue, setTriggerValue] = useState(3000)
  const [sessionTemplates, setSessionTemplates] = useState<SessionTemplateDTO[]>([])
  const [templatesLoading, setTemplatesLoading] = useState(true)
  const [templateError, setTemplateError] = useState<string | null>(null)
  const [selectedTemplateKey, setSelectedTemplateKey] = useState('')
  const [templateEditorOpen, setTemplateEditorOpen] = useState(false)
  const [templateName, setTemplateName] = useState('')
  const [includeTemplateSettings, setIncludeTemplateSettings] = useState(true)
  const [templateSaving, setTemplateSaving] = useState(false)

  async function refreshSessions() {
    setLoading(true)
    setError(null)
    try {
      const result = await listSessions()
      setSessions([...result].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      ))
    } catch (caught) {
      setError(describeError(caught))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let active = true

    async function loadInitialSessions() {
      try {
        const result = await listSessions()
        if (!active) return
        setSessions([...result].sort(
          (a, b) =>
            new Date(b.createdAt).getTime() -
            new Date(a.createdAt).getTime(),
        ))
      } catch (caught) {
        if (active) setError(describeError(caught))
      } finally {
        if (active) setLoading(false)
      }
    }

    void loadInitialSessions()
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true

    async function loadTemplates() {
      try {
        const result = await listSessionTemplates()
        if (active) setSessionTemplates(result)
      } catch (caught) {
        if (active) setTemplateError(describeError(caught))
      } finally {
        if (active) setTemplatesLoading(false)
      }
    }

    void loadTemplates()
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const sessionId = selected?.sessionId
    const sessionStatus = selected?.status
    const acquisitionInterval = selected?.intervalMs
    if (!sessionId || sessionStatus !== 'RUNNING') return
    let active = true
    const poll = async () => {
      try {
        const [fresh, records] = await Promise.all([
          getSession(sessionId),
          loadAllLogs(sessionId),
        ])
        if (!active) return
        setSelected(fresh)
        setLogs(records)
        if (fresh.status !== 'RUNNING') {
          setView(fresh.status === 'FINISHED' ? 'explore' : 'history')
        }
      } catch (caught) {
        if (active) setError(describeError(caught))
      }
    }
    void poll()
    const id = window.setInterval(
      poll,
      Math.max(acquisitionInterval ?? 1000, 1000),
    )
    return () => {
      active = false
      window.clearInterval(id)
    }
  }, [
    selected?.sessionId,
    selected?.status,
    selected?.intervalMs,
  ])

  useEffect(() => {
    if (!selected?.startedAt || selected.status !== 'RUNNING') return
    const update = () => {
      setElapsedMs(Math.max(0, Date.now() - new Date(selected.startedAt!).getTime()))
    }
    update()
    const id = window.setInterval(update, 1000)
    return () => window.clearInterval(id)
  }, [selected?.startedAt, selected?.status])

  const pidGroups = useMemo(() => {
    const result = new Map<string, LogDTO[]>()
    logs.forEach((log) => {
      result.set(log.pid, [...(result.get(log.pid) ?? []), log])
    })
    return result
  }, [logs])

  const pidOptions = useMemo(
    () => [...pidGroups.keys()].sort(),
    [pidGroups],
  )

  const minReference = lowerLimit.trim() === '' ? null : Number(lowerLimit)
  const maxReference = upperLimit.trim() === '' ? null : Number(upperLimit)
  const referenceRangeInvalid =
    minReference !== null &&
    maxReference !== null &&
    minReference > maxReference

  const referenceLimitEvents = useMemo(() => {
    if (referenceRangeInvalid) return []
    return (pidGroups.get(chartPid) ?? []).filter((log) => {
      if (log.value === null) return false
      return (minReference !== null && Number.isFinite(minReference) && log.value < minReference) ||
        (maxReference !== null && Number.isFinite(maxReference) && log.value > maxReference)
    })
  }, [chartPid, maxReference, minReference, pidGroups, referenceRangeInvalid])

  const dataSummary = useMemo(() => {
    const validValues = logs.filter((log) => log.value !== null).length
    const duration = logs.length > 1
      ? new Date(logs[logs.length - 1].timestamp).getTime() -
        new Date(logs[0].timestamp).getTime()
      : 0
    return {
      records: logs.length,
      pids: pidGroups.size,
      missingValues: logs.length - validValues,
      duration,
    }
  }, [logs, pidGroups])

  useEffect(() => {
    if (pidOptions.length === 0) return
    if (!chartPid) setChartPid(pidOptions[0])
    if (visiblePids.length === 0) setVisiblePids(pidOptions)
  }, [pidOptions, chartPid, visiblePids])

  useEffect(() => {
    setLowerLimit('')
    setUpperLimit('')
  }, [chartPid])

  async function selectSession(session: SessionDTO) {
    setLoading(true)
    setError(null)
    setAiResult('')
    setAiError(null)
    setLowerLimit('')
    setUpperLimit('')
    try {
      const fresh = await getSession(session.sessionId)
      setSelected(fresh)
      if (fresh.status === 'CREATED') setView('history')
      if (fresh.status === 'RUNNING') setView('running')
      if (fresh.status === 'FINISHED') {
        const records = await loadAllLogs(fresh.sessionId)
        setLogs(records)
        const pids = [...new Set(records.map((log) => log.pid))]
        setVisiblePids(pids)
        setChartPid(pids[0] ?? '')
        setScatterX(pids[0] ?? '')
        setScatterY(pids[1] ?? pids[0] ?? '')
        setExploreMode('dashboard')
        setView('explore')
      }
      if (fresh.status === 'FAILED') setView('history')
    } catch (caught) {
      setError(describeError(caught))
    } finally {
      setLoading(false)
    }
  }

  function openCreate(session?: SessionDTO) {
    setSelectedTemplateKey('')
    setTemplateEditorOpen(false)
    setTemplateName('')
    setTemplateError(null)
    if (session) {
      setVehicle(session.vehicle ?? '')
      setStartReason(session.startReason ?? '')
      setIntervalMs(session.intervalMs)
      setMaxDurationMs(session.maxDurationMs)
      if (session.triggerConfig) {
        setTriggerEnabled(true)
        setTriggerPid(session.triggerConfig.pid)
        setTriggerIndex(session.triggerConfig.valueIndex)
        setTriggerOperator(session.triggerConfig.operator)
        setTriggerValue(session.triggerConfig.triggerValue)
      } else {
        setTriggerEnabled(false)
      }
      void getSessionLogs(session.sessionId, 0, 500).then((page) => {
        setSelectedPids([...new Set(page.content.map((log) => log.pid))])
      }).catch(() => undefined)
    } else {
      setVehicle('')
      setStartReason('')
      setSelectedPids(['0C', '0D'])
      setIntervalMs(1000)
      setMaxDurationMs(300000)
      setTriggerEnabled(false)
    }
    setView('create')
  }

  function applySessionTemplate(key: string) {
    setSelectedTemplateKey(key)
    setTemplateError(null)
    const template = SESSION_TEMPLATE_PRESETS.find((item) => item.key === key)
      ?? sessionTemplates.find((item) => `custom:${item.templateId}` === key)
    if (!template) return

    setStartReason(template.startReason)
    setSelectedPids([...template.pids])
    if (template.includeSessionSettings) {
      setIntervalMs(template.intervalMs ?? 1000)
      setMaxDurationMs(template.maxDurationMs ?? 300000)
      setTriggerEnabled(template.trigger !== null)
      if (template.trigger) {
        setTriggerPid(template.trigger.pid)
        setTriggerIndex(template.trigger.valueIndex)
        setTriggerOperator(template.trigger.operator)
        setTriggerValue(template.trigger.triggerValue)
      }
    }
  }

  async function handleSaveTemplate() {
    if (!templateName.trim() || !startReason.trim() || selectedPids.length === 0) {
      setTemplateError('Informe o nome e o objetivo, e selecione pelo menos um PID.')
      return
    }
    if (includeTemplateSettings && (intervalMs <= 0 || maxDurationMs <= 0)) {
      setTemplateError('Intervalo e duração máxima do modelo devem ser positivos.')
      return
    }

    setTemplateSaving(true)
    setTemplateError(null)
    try {
      const saved = await createSessionTemplate({
        name: templateName.trim(),
        startReason: startReason.trim(),
        pids: selectedPids,
        includeSessionSettings: includeTemplateSettings,
        intervalMs: includeTemplateSettings ? intervalMs : null,
        maxDurationMs: includeTemplateSettings ? maxDurationMs : null,
        trigger: includeTemplateSettings && triggerEnabled
          ? {
              pid: triggerPid,
              valueIndex: triggerIndex,
              operator: triggerOperator,
              triggerValue,
            }
          : null,
      })
      setSessionTemplates((current) =>
        [...current, saved].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')),
      )
      setSelectedTemplateKey(`custom:${saved.templateId}`)
      setTemplateName('')
      setTemplateEditorOpen(false)
    } catch (caught) {
      setTemplateError(describeError(caught))
    } finally {
      setTemplateSaving(false)
    }
  }

  async function handleDeleteSelectedTemplate() {
    const template = sessionTemplates.find(
      (item) => `custom:${item.templateId}` === selectedTemplateKey,
    )
    if (!template || !window.confirm(`Excluir o modelo "${template.name}"?`)) return

    setTemplateError(null)
    try {
      await deleteSessionTemplate(template.templateId)
      setSessionTemplates((current) =>
        current.filter((item) => item.templateId !== template.templateId),
      )
      setSelectedTemplateKey('')
    } catch (caught) {
      setTemplateError(describeError(caught))
    }
  }

  async function handleCreate() {
    if (!vehicle.trim() || selectedPids.length === 0) {
      setError('Informe o veículo e selecione pelo menos um PID.')
      return
    }
    if (intervalMs <= 0 || maxDurationMs <= 0) {
      setError('Intervalo e duração máxima devem ser positivos.')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const request: CreateSessionRequest = {
        vehicle: vehicle.trim(),
        pids: selectedPids,
        intervalMs,
        startReason: startReason.trim(),
        maxDurationMs,
        trigger: triggerEnabled
          ? {
              pid: triggerPid,
              valueIndex: triggerIndex,
              operator: triggerOperator,
              triggerValue,
            }
          : null,
      }
      const created = await createSession(request)
      setSessions((current) => [created, ...current])
      setSelected(created)
      setView('history')
    } catch (caught) {
      setError(describeError(caught))
    } finally {
      setLoading(false)
    }
  }

  async function handleStart() {
    if (!selected) return
    setLoading(true)
    setError(null)
    try {
      const started = await startSession(selected.sessionId)
      setSelected(started)
      setLogs([])
      setLowerLimit('')
      setUpperLimit('')
      setRunningMode('cards')
      setView('running')
      await refreshSessions()
    } catch (caught) {
      setError(describeError(caught))
    } finally {
      setLoading(false)
    }
  }

  async function handleStop() {
    if (!selected) return
    setLoading(true)
    try {
      const stopped = await stopSession(selected.sessionId)
      setSelected(stopped)
      await refreshSessions()
      setView('history')
    } catch (caught) {
      setError(describeError(caught))
    } finally {
      setLoading(false)
    }
  }

  async function handleAnalyse() {
    if (!selected) return
    setAiLoading(true)
    setAiError(null)
    setAiResult('')
    try {
      setAiResult(await analyseSession(selected.sessionId))
    } catch (caught) {
      setAiError(describeError(caught))
    } finally {
      setAiLoading(false)
    }
  }

  function togglePid(pid: string) {
    setSelectedPids((current) =>
      current.includes(pid)
        ? current.filter((item) => item !== pid)
        : [...current, pid],
    )
  }

  const chartRecords = useMemo(() => {
    if (!chartPid) return []
    return (pidGroups.get(chartPid) ?? []).map((log) => ({
      timestamp: new Date(log.timestamp).getTime(),
      label: formatDate(log.timestamp),
      value: log.value,
    }))
  }, [chartPid, pidGroups])

  const scatterRecords = useMemo(() => {
    if (!scatterX || !scatterY) return []
    const yValues = pidGroups.get(scatterY) ?? []
    return (pidGroups.get(scatterX) ?? [])
      .map((xLog) => {
        let nearest: LogDTO | undefined
        let nearestDistance = Number.POSITIVE_INFINITY
        yValues.forEach((yLog) => {
          const distance = Math.abs(
            new Date(yLog.timestamp).getTime() -
              new Date(xLog.timestamp).getTime(),
          )
          if (distance < nearestDistance) {
            nearestDistance = distance
            nearest = yLog
          }
        })
        if (!nearest || nearestDistance > 2000) return null
        return { x: xLog.value, y: nearest.value }
      })
      .filter(
        (point): point is { x: number; y: number } =>
          point !== null && point.x !== null && point.y !== null,
      )
  }, [pidGroups, scatterX, scatterY])

  const correlation = useMemo(() => {
    if (scatterRecords.length < 2) return null
    return sampleCorrelation(
      scatterRecords.map((point) => point.x),
      scatterRecords.map((point) => point.y),
    )
  }, [scatterRecords])

  const statistics = useMemo(() => {
    return visiblePids.map((pid) => {
      const values = (pidGroups.get(pid) ?? [])
        .map((log) => log.value)
        .filter((value): value is number => value !== null)
      const sorted = [...values].sort((a, b) => a - b)
      const average = values.length
        ? values.reduce((sum, value) => sum + value, 0) / values.length
        : null
      const median = sorted.length
        ? sorted.length % 2
          ? sorted[Math.floor(sorted.length / 2)]
          : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2
        : null
      const variance = average !== null && values.length
        ? values.reduce((sum, value) => sum + (value - average) ** 2, 0) /
          values.length
        : null
      return {
        pid,
        unit: pidGroups.get(pid)?.[0]?.unit ?? '',
        average,
        median,
        min: sorted[0] ?? null,
        max: sorted.at(-1) ?? null,
        deviation: variance === null ? null : Math.sqrt(variance),
        count: values.length,
      }
    })
  }, [pidGroups, visiblePids])

  function downloadLogsCsv() {
    const header = ['timestamp', 'pid', 'value_name', 'value', 'unit']
    const rows = logs.map((log) => [
      log.timestamp,
      log.pid,
      log.valueName,
      log.value ?? '',
      log.unit,
    ])
    const escapeCsv = (value: string | number) =>
      `"${String(value).replaceAll('"', '""')}"`
    const csv = [header, ...rows]
      .map((row) => row.map(escapeCsv).join(','))
      .join('\n')
    const url = URL.createObjectURL(
      new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' }),
    )
    const link = document.createElement('a')
    link.href = url
    link.download = `sessao-${selected?.sessionId ?? 'dados'}.csv`
    link.click()
    URL.revokeObjectURL(url)
  }

  function renderCollectionInsights() {
    return (
      <>
        <section className="collection-summary">
          <div><span>Registros</span><strong>{dataSummary.records}</strong></div>
          <div><span>PIDs coletados</span><strong>{dataSummary.pids}</strong></div>
          <div><span>Valores ausentes</span><strong>{dataSummary.missingValues}</strong></div>
          <div><span>Período registrado</span><strong>{formatDuration(dataSummary.duration)}</strong></div>
        </section>

        <section className="sessions-panel reference-panel">
          <div className="sessions-panel-heading">
            <div>
              <p className="sessions-kicker">Referência manual</p>
              <h3>Marcar valores fora dos limites definidos</h3>
            </div>
            <span className="selection-count">{referenceLimitEvents.length} ocorrência(s)</span>
          </div>
          <p className="reference-note">
            Defina limites de acordo com a especificação do veículo ou do teste.
            Eles não são limites automotivos automáticos nem um diagnóstico.
          </p>
          <div className="reference-controls">
            <label>
              PID
              <select value={chartPid} onChange={(event) => setChartPid(event.target.value)}>
                {pidOptions.map((pid) => <option key={pid} value={pid}>{pid}</option>)}
              </select>
            </label>
            <label>
              Mínimo{pidGroups.get(chartPid)?.[0]?.unit ? ` (${pidGroups.get(chartPid)?.[0]?.unit})` : ''}
              <input type="number" value={lowerLimit} onChange={(event) => setLowerLimit(event.target.value)} placeholder="Opcional" />
            </label>
            <label>
              Máximo{pidGroups.get(chartPid)?.[0]?.unit ? ` (${pidGroups.get(chartPid)?.[0]?.unit})` : ''}
              <input type="number" value={upperLimit} onChange={(event) => setUpperLimit(event.target.value)} placeholder="Opcional" />
            </label>
          </div>
          {referenceRangeInvalid && (
            <p className="sessions-error" role="alert">O limite mínimo não pode ser maior que o máximo.</p>
          )}
          {referenceLimitEvents.length > 0 ? (
            <ol className="reference-events">
              {[...referenceLimitEvents].reverse().slice(0, 8).map((log) => (
                <li key={log.id}>
                  <time>{formatDate(log.timestamp)}</time>
                  <strong>{log.value} {log.unit}</strong>
                  <span>{minReference !== null && log.value !== null && log.value < minReference
                    ? 'Abaixo do mínimo definido'
                    : 'Acima do máximo definido'}</span>
                </li>
              ))}
            </ol>
          ) : (
            <p className="reference-empty">
              {pidOptions.length === 0
                ? 'Aguardando registros para selecionar um PID.'
                : referenceRangeInvalid
                  ? 'Corrija os limites para consultar as ocorrências.'
                  : minReference === null && maxReference === null
                    ? 'Informe pelo menos um limite para destacar ocorrências.'
                    : 'Nenhum registro fora dos limites informados.'}
            </p>
          )}
        </section>
      </>
    )
  }

  return (
    <>
      <SectionHeading section="sessions" />

      {error && <p className="sessions-error" role="alert">{error}</p>}

      {view === 'history' && (
        <div className="sessions-layout">
          <section className="sessions-panel history-panel">
            <div className="sessions-panel-heading">
              <div>
                <p className="sessions-kicker">Aquisições</p>
                <h3>Histórico de sessões</h3>
              </div>
              <button className="button-primary" onClick={() => openCreate()} type="button">
                Nova sessão
              </button>
            </div>

            {loading ? (
              <p className="sessions-status">Carregando sessões…</p>
            ) : sessions.length === 0 ? (
              <div className="sessions-empty">
                <span>—</span>
                <p>Nenhuma sessão encontrada.</p>
                <button className="button-secondary" onClick={() => openCreate()} type="button">
                  Criar primeira sessão
                </button>
              </div>
            ) : (
              <div className="session-list">
                {sessions.map((session) => (
                  <button
                    className={`session-row ${selected?.sessionId === session.sessionId ? 'is-selected' : ''}`}
                    key={session.sessionId}
                    onClick={() => void selectSession(session)}
                    type="button"
                  >
                    <span className={`status-dot status-${session.status.toLowerCase()}`} />
                    <div className="session-row-main">
                      <strong>{session.vehicle}</strong>
                      <span>{session.startReason || 'Sem descrição'}</span>
                    </div>
                    <div className="session-row-meta">
                      <span>{STATUS_LABEL[session.status]}</span>
                      <time>{formatDate(session.createdAt)}</time>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </section>

          {selected && (
            <section className="sessions-panel session-detail">
              <p className="sessions-kicker">Sessão #{selected.sessionId}</p>
              <h3>{selected.vehicle}</h3>
              <dl className="session-details">
                <div><dt>Status</dt><dd>{STATUS_LABEL[selected.status]}</dd></div>
                <div><dt>Criada em</dt><dd>{formatDate(selected.createdAt)}</dd></div>
                <div><dt>Iniciada em</dt><dd>{formatDate(selected.startedAt)}</dd></div>
                <div><dt>Intervalo</dt><dd>{selected.intervalMs} ms</dd></div>
                <div><dt>Duração máxima</dt><dd>{formatDuration(selected.maxDurationMs)}</dd></div>
                {selected.finishedAt && <div><dt>Finalizada em</dt><dd>{formatDate(selected.finishedAt)}</dd></div>}
                {selected.endReason && <div><dt>Encerramento</dt><dd>{END_REASON_LABEL[selected.endReason]}</dd></div>}
              </dl>
              <div className="session-detail-actions">
                {selected.status === 'CREATED' && (
                  <button className="button-primary" disabled={loading} onClick={() => void handleStart()} type="button">
                    Iniciar aquisição
                  </button>
                )}
                {selected.status === 'FINISHED' && (
                  <button className="button-primary" onClick={() => void selectSession(selected)} type="button">
                    Explorar dados
                  </button>
                )}
                {selected.status === 'RUNNING' && (
                  <button className="button-primary" onClick={() => setView('running')} type="button">
                    Acompanhar execução
                  </button>
                )}
                {selected.status === 'FAILED' && (
                  <p className="session-failed-note">
                    Registros anteriores podem permanecer disponíveis, mas não haverá nova aquisição automática.
                  </p>
                )}
                <button className="button-secondary" onClick={() => openCreate(selected)} type="button">
                  Usar como modelo
                </button>
              </div>
            </section>
          )}
        </div>
      )}

      {view === 'create' && (
        <section className="sessions-panel create-panel">
          <div className="sessions-panel-heading">
            <div>
              <p className="sessions-kicker">Nova aquisição</p>
              <h3>Configurar sessão</h3>
            </div>
            <button className="button-secondary" onClick={() => setView('history')} type="button">Cancelar</button>
          </div>

          <div className="session-template-picker">
            <label>
              Modelo por objetivo
              <select
                value={selectedTemplateKey}
                onChange={(event) => applySessionTemplate(event.target.value)}
              >
                <option value="">Escolher um modelo…</option>
                <optgroup label="Sugestões">
                  {SESSION_TEMPLATE_PRESETS.map((template) => (
                    <option key={template.key} value={template.key}>
                      {template.name}
                    </option>
                  ))}
                </optgroup>
                {sessionTemplates.length > 0 && (
                  <optgroup label="Modelos salvos nesta instalação">
                    {sessionTemplates.map((template) => (
                      <option
                        key={template.templateId}
                        value={`custom:${template.templateId}`}
                      >
                        {template.name}
                      </option>
                    ))}
                  </optgroup>
                )}
              </select>
              <small>
                Sugestões preenchem objetivo e PIDs. Modelos completos também aplicam
                intervalo, duração e trigger salvo.
              </small>
            </label>
            <div className="session-template-actions">
              <button
                className="button-secondary"
                onClick={() => {
                  setTemplateError(null)
                  setTemplateEditorOpen((open) => !open)
                }}
                type="button"
              >
                Salvar configuração como modelo
              </button>
              {selectedTemplateKey.startsWith('custom:') && (
                <button
                  className="button-secondary template-delete"
                  onClick={() => void handleDeleteSelectedTemplate()}
                  type="button"
                >
                  Excluir modelo selecionado
                </button>
              )}
            </div>
          </div>
          {templatesLoading && <p className="template-note" role="status">Carregando modelos salvos…</p>}
          {templateError && <p className="sessions-error" role="alert">{templateError}</p>}
          {templateEditorOpen && (
            <div className="session-template-editor">
              <label>
                Nome do modelo
                <input
                  maxLength={120}
                  value={templateName}
                  onChange={(event) => setTemplateName(event.target.value)}
                  placeholder="Ex.: Avaliação de marcha lenta"
                />
              </label>
              <label className="trigger-toggle">
                <input
                  type="checkbox"
                  checked={includeTemplateSettings}
                  onChange={(event) => setIncludeTemplateSettings(event.target.checked)}
                />
                Salvar também intervalo, duração máxima e trigger opcional
              </label>
              <p className="template-note">
                {includeTemplateSettings
                  ? 'O modelo salvará as configurações atuais da sessão.'
                  : 'O modelo salvará somente o objetivo e os PIDs; as demais configurações atuais serão mantidas ao selecioná-lo.'}
              </p>
              <button
                className="button-primary"
                disabled={templateSaving}
                onClick={() => void handleSaveTemplate()}
                type="button"
              >
                {templateSaving ? 'Salvando…' : 'Salvar modelo'}
              </button>
            </div>
          )}

          <div className="session-form-grid">
            <label>Veículo<input value={vehicle} onChange={(event) => setVehicle(event.target.value)} placeholder="Ex.: Gol 1.6 — 2018" /></label>
            <label>Descrição ou motivo da aquisição<textarea value={startReason} onChange={(event) => setStartReason(event.target.value)} placeholder="Ex.: investigar oscilação em marcha lenta" /></label>
            <label>Intervalo (ms)<input type="number" min="1" value={intervalMs} onChange={(event) => setIntervalMs(Number(event.target.value))} /></label>
            <label>Duração máxima (ms)<input type="number" min="1" value={maxDurationMs} onChange={(event) => setMaxDurationMs(Number(event.target.value))} /></label>
          </div>

          <div className="pid-create-section">
            <div className="sessions-panel-heading">
              <div><p className="sessions-kicker">Parâmetros</p><h3>PIDs monitorados</h3></div>
              <span className="selection-count">{selectedPids.length} selecionado(s)</span>
            </div>
            <div className="pid-create-grid">
              {PID_OPTIONS.map((pid) => (
                <label className="pid-create-option" key={pid.code}>
                  <input type="checkbox" checked={selectedPids.includes(pid.code)} onChange={() => togglePid(pid.code)} />
                  <code>{pid.code}</code>
                  <span><strong>{pid.name}</strong><small>{pid.description}</small></span>
                </label>
              ))}
            </div>
          </div>

          <label className="trigger-toggle">
            <input type="checkbox" checked={triggerEnabled} onChange={(event) => setTriggerEnabled(event.target.checked)} />
            Configurar trigger opcional de encerramento
          </label>

          {triggerEnabled && (
            <div className="trigger-grid">
              <label>
                PID
                <select
                  value={triggerPid}
                  onChange={(event) => setTriggerPid(event.target.value)}
                >
                  {PID_OPTIONS.map((pid) => (
                    <option key={pid.code} value={pid.code}>
                      {pid.code} — {pid.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>Índice do valor<input type="number" min="0" value={triggerIndex} onChange={(event) => setTriggerIndex(Number(event.target.value))} /></label>
              <label>Operador<select value={triggerOperator} onChange={(event) => setTriggerOperator(event.target.value as TriggerOperator)}><option value="GT">&gt;</option><option value="GTE">&gt;=</option><option value="LT">&lt;</option><option value="LTE">&lt;=</option><option value="EQ">=</option></select></label>
              <label>Valor<input type="number" value={triggerValue} onChange={(event) => setTriggerValue(Number(event.target.value))} /></label>
            </div>
          )}

          <button className="button-primary create-submit" disabled={loading} onClick={() => void handleCreate()} type="button">
            {loading ? 'Criando…' : 'Criar sessão'}
          </button>
        </section>
      )}

      {view === 'running' && selected && (
        <div className="running-layout">
          <section className="sessions-panel running-summary">
            <div className="running-heading">
              <div><span className="running-label"><i /> Sessão em execução</span><h3>{selected.vehicle}</h3></div>
              <button className="button-secondary" disabled={loading} onClick={() => void handleStop()} type="button">Encerrar aquisição</button>
            </div>
            <div className="running-metrics">
              <div><span>Tempo decorrido</span><strong>{formatDuration(elapsedMs)}</strong></div>
              <div><span>Duração máxima</span><strong>{formatDuration(selected.maxDurationMs)}</strong></div>
              <div><span>Intervalo</span><strong>{selected.intervalMs} ms</strong></div>
              <div><span>Última atualização</span><strong>{logs.length ? formatDate(logs.at(-1)?.timestamp ?? null) : 'Aguardando registros'}</strong></div>
            </div>
            <p className="running-pids">PIDs: {[...pidGroups.keys()].join(', ') || 'Aguardando primeiros registros'}</p>
          </section>

          <section className="sessions-panel running-values">
            <div className="running-values-heading">
              <div><p className="sessions-kicker">Dados recebidos</p><h3>Visualização ao vivo</h3></div>
              <div className="running-view-controls">
                <label>
                  Forma de visualizar
                  <select value={runningMode} onChange={(event) => setRunningMode(event.target.value as RunningMode)}>
                    <option value="cards">Valores atuais</option>
                    <option value="individual">Gráfico individual</option>
                    <option value="overlay">PIDs sobrepostos</option>
                    <option value="table">Tabela de registros</option>
                  </select>
                </label>
                {(runningMode === 'individual' || runningMode === 'overlay') && (
                  <label>
                    PID
                    <select value={chartPid} onChange={(event) => setChartPid(event.target.value)}>
                      {pidOptions.map((pid) => <option key={pid} value={pid}>{pid}</option>)}
                    </select>
                  </label>
                )}
                <button className="button-secondary" onClick={downloadLogsCsv} type="button" disabled={logs.length === 0}>Exportar CSV</button>
              </div>
              <span className="selection-count">{logs.length} registros</span>
            </div>

            {pidGroups.size === 0 ? (
              <div className="sessions-empty compact"><span>—</span><p>Aguardando telemetria real do backend.</p></div>
            ) : (
              <>
                {runningMode === 'cards' && (
                  <div className="value-card-grid">
                    {[...pidGroups.entries()].map(([pid, records]) => {
                      const latest = records.at(-1)!
                      const values = records.map((record) => record.value).filter((value): value is number => value !== null)
                      return (
                        <article className="value-card" key={pid}>
                          <code>{pid}</code>
                          <strong>{latest.value ?? '—'} <span>{latest.unit}</span></strong>
                          <small>{latest.valueName}</small>
                          {values.length > 0 && <div className="value-extremes"><span>Mín. {Math.min(...values)}{latest.unit}</span><span>Máx. {Math.max(...values)}{latest.unit}</span></div>}
                          <time>{formatDate(latest.timestamp)}</time>
                        </article>
                      )
                    })}
                  </div>
                )}

                {runningMode === 'individual' && (
                  <div className="chart-area">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={chartRecords}>
                        <CartesianGrid stroke="rgba(183,205,214,.09)" />
                        <XAxis dataKey="timestamp" type="number" domain={['dataMin', 'dataMax']} tickFormatter={(value) => new Date(value).toLocaleTimeString('pt-BR')} />
                        <YAxis />
                        <Tooltip labelFormatter={(value) => formatDate(new Date(Number(value)).toISOString())} />
                        <Legend />
                        {minReference !== null && Number.isFinite(minReference) && <ReferenceLine y={minReference} stroke="#f3c779" strokeDasharray="4 4" label="Mínimo definido" />}
                        {maxReference !== null && Number.isFinite(maxReference) && <ReferenceLine y={maxReference} stroke="#e78174" strokeDasharray="4 4" label="Máximo definido" />}
                        <Line type="monotone" dataKey="value" name={chartPid} stroke="#61d6bd" dot={false} isAnimationActive={false} />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                )}

                {runningMode === 'overlay' && (
                  <>
                    <div className="pid-toggle-list">
                      {pidOptions.map((pid) => <label key={pid}><input type="checkbox" checked={visiblePids.includes(pid)} onChange={() => setVisiblePids((current) => current.includes(pid) ? current.filter((item) => item !== pid) : [...current, pid])} />{pid}</label>)}
                    </div>
                    <div className="chart-area">
                      <ResponsiveContainer width="100%" height="100%">
                        <LineChart>
                          <CartesianGrid stroke="rgba(183,205,214,.09)" />
                          <XAxis dataKey="timestamp" type="number" allowDuplicatedCategory={false} domain={['dataMin', 'dataMax']} tickFormatter={(value) => new Date(value).toLocaleTimeString('pt-BR')} />
                          <YAxis yAxisId="left" />
                          <Tooltip />
                          <Legend />
                          {visiblePids.map((pid, index) => (
                            <Line key={pid} yAxisId="left" data={(pidGroups.get(pid) ?? []).map((log) => ({ timestamp: new Date(log.timestamp).getTime(), value: log.value }))} type="monotone" dataKey="value" name={pid} stroke={index % 2 ? '#f3c779' : '#61d6bd'} dot={false} isAnimationActive={false} />
                          ))}
                        </LineChart>
                      </ResponsiveContainer>
                    </div>
                  </>
                )}

                {runningMode === 'table' && (
                  <div className="raw-data-table-wrap">
                    <table className="raw-data-table">
                      <thead><tr><th>Data e hora</th><th>PID</th><th>Parâmetro</th><th>Valor</th><th>Unidade</th></tr></thead>
                      <tbody>{logs.map((log) => (
                        <tr key={log.id}>
                          <td>{formatDate(log.timestamp)}</td>
                          <td><code>{log.pid}</code></td>
                          <td>{log.valueName}</td>
                          <td>{log.value ?? '—'}</td>
                          <td>{log.unit}</td>
                        </tr>
                      ))}</tbody>
                    </table>
                  </div>
                )}
              </>
            )}
          </section>
          {renderCollectionInsights()}
        </div>
      )}

      {view === 'explore' && selected && (
        <div className="explore-layout">
          <section className="sessions-panel explore-toolbar">
            <div><p className="sessions-kicker">Sessão #{selected.sessionId}</p><h3>{selected.vehicle}</h3></div>
            <div className="explore-controls">
              <label>
                Forma de visualizar
                <select value={exploreMode} onChange={(event) => setExploreMode(event.target.value as ExploreMode)}>
                  <option value="dashboard">Visão geral</option>
                  <option value="individual">Gráfico individual</option>
                  <option value="overlay">PIDs sobrepostos</option>
                  <option value="table">Tabela de registros</option>
                  <option value="statistics">Estatísticas</option>
                  <option value="correlation">Dispersão e correlação</option>
                  <option value="ai">Análise inteligente</option>
                </select>
              </label>
              <label>PID individual<select value={chartPid} onChange={(event) => setChartPid(event.target.value)}>{pidOptions.map((pid) => <option key={pid} value={pid}>{pid}</option>)}</select></label>
              <button className="button-secondary" onClick={() => void loadAllLogs(selected.sessionId).then(setLogs)} type="button">Atualizar dados</button>
              <button className="button-secondary" onClick={downloadLogsCsv} type="button">Exportar CSV</button>
            </div>
          </section>

          {logs.length === 0 ? (
            <section className="sessions-panel sessions-empty compact"><span>—</span><p>Esta sessão não possui registros de telemetria.</p></section>
          ) : (
            <>
              {renderCollectionInsights()}
              {(exploreMode === 'dashboard' || exploreMode === 'individual') && <section className="sessions-panel chart-panel">
                <div className="sessions-panel-heading"><div><p className="sessions-kicker">Gráfico individual</p><h3>{chartPid}</h3></div></div>
                <div className="chart-area">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartRecords}>
                      <CartesianGrid stroke="rgba(183,205,214,.09)" />
                      <XAxis dataKey="timestamp" type="number" domain={['dataMin', 'dataMax']} tickFormatter={(value) => new Date(value).toLocaleTimeString('pt-BR')} />
                      <YAxis />
                      <Tooltip labelFormatter={(value) => formatDate(new Date(Number(value)).toISOString())} />
                      <Legend />
                      {minReference !== null && Number.isFinite(minReference) && <ReferenceLine y={minReference} stroke="#f3c779" strokeDasharray="4 4" label="Mínimo definido" />}
                      {maxReference !== null && Number.isFinite(maxReference) && <ReferenceLine y={maxReference} stroke="#e78174" strokeDasharray="4 4" label="Máximo definido" />}
                      <Line type="monotone" dataKey="value" name={chartPid} stroke="#61d6bd" dot={false} isAnimationActive={false} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </section>}

              {(exploreMode === 'dashboard' || exploreMode === 'overlay') && <section className="sessions-panel overlaid-panel">
                <div className="sessions-panel-heading"><div><p className="sessions-kicker">Sobreposição</p><h3>PIDs visíveis</h3></div></div>
                <div className="pid-toggle-list">
                  {pidOptions.map((pid) => <label key={pid}><input type="checkbox" checked={visiblePids.includes(pid)} onChange={() => setVisiblePids((current) => current.includes(pid) ? current.filter((item) => item !== pid) : [...current, pid])} />{pid}</label>)}
                </div>
                <div className="chart-area">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart>
                      <CartesianGrid stroke="rgba(183,205,214,.09)" />
                      <XAxis dataKey="timestamp" type="number" allowDuplicatedCategory={false} domain={['dataMin', 'dataMax']} tickFormatter={(value) => new Date(value).toLocaleTimeString('pt-BR')} />
                      <YAxis yAxisId="left" />
                      <Tooltip />
                      <Legend />
                      {visiblePids.map((pid, index) => (
                        <Line key={pid} yAxisId="left" data={(pidGroups.get(pid) ?? []).map((log) => ({ timestamp: new Date(log.timestamp).getTime(), value: log.value }))} type="monotone" dataKey="value" name={pid} stroke={index % 2 ? '#f3c779' : '#61d6bd'} dot={false} isAnimationActive={false} />
                      ))}
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </section>}

              {exploreMode === 'table' && (
                <section className="sessions-panel raw-data-panel">
                  <div className="sessions-panel-heading">
                    <div><p className="sessions-kicker">Dados brutos</p><h3>Registros da sessão</h3></div>
                    <span className="selection-count">{logs.length} registros</span>
                  </div>
                  <div className="raw-data-table-wrap">
                    <table className="raw-data-table">
                      <thead><tr><th>Data e hora</th><th>PID</th><th>Parâmetro</th><th>Valor</th><th>Unidade</th></tr></thead>
                      <tbody>{logs.map((log) => (
                        <tr key={log.id}>
                          <td>{formatDate(log.timestamp)}</td>
                          <td><code>{log.pid}</code></td>
                          <td>{log.valueName}</td>
                          <td>{log.value ?? '—'}</td>
                          <td>{log.unit}</td>
                        </tr>
                      ))}</tbody>
                    </table>
                  </div>
                </section>
              )}

              {(exploreMode === 'dashboard' || exploreMode === 'statistics') && <section className="sessions-panel stats-panel">
                <div className="sessions-panel-heading"><div><p className="sessions-kicker">Estatística descritiva</p><h3>Resumo por PID</h3></div></div>
                <div className="stats-table-wrap"><table className="stats-table"><thead><tr><th>PID</th><th>Média</th><th>Mediana</th><th>Mín.</th><th>Máx.</th><th>Desvio padrão</th><th>Amostras</th></tr></thead><tbody>{statistics.map((stat) => <tr key={stat.pid}><td><code>{stat.pid}</code></td><td>{stat.average?.toFixed(2) ?? '—'} {stat.unit}</td><td>{stat.median?.toFixed(2) ?? '—'} {stat.unit}</td><td>{stat.min?.toFixed(2) ?? '—'} {stat.unit}</td><td>{stat.max?.toFixed(2) ?? '—'} {stat.unit}</td><td>{stat.deviation?.toFixed(2) ?? '—'} {stat.unit}</td><td>{stat.count}</td></tr>)}</tbody></table></div>
              </section>}

              {(exploreMode === 'dashboard' || exploreMode === 'correlation') && <section className="sessions-panel correlation-panel">
                <div className="sessions-panel-heading"><div><p className="sessions-kicker">Correlação temporal</p><h3>Dispersão entre parâmetros</h3></div></div>
                <div className="correlation-controls"><label>Eixo X<select value={scatterX} onChange={(event) => setScatterX(event.target.value)}>{pidOptions.map((pid) => <option key={pid} value={pid}>{pid}</option>)}</select></label><label>Eixo Y<select value={scatterY} onChange={(event) => setScatterY(event.target.value)}>{pidOptions.map((pid) => <option key={pid} value={pid}>{pid}</option>)}</select></label></div>
                <p className="correlation-value">Correlação de Pearson: <strong>{correlation === null ? 'indisponível' : correlation.toFixed(3)}</strong></p>
                {scatterRecords.length < 2 ? <div className="sessions-empty compact"><span>—</span><p>Não há amostras correspondentes temporalmente.</p></div> : <div className="chart-area scatter-area"><ResponsiveContainer width="100%" height="100%"><ScatterChart><CartesianGrid stroke="rgba(183,205,214,.09)" /><XAxis dataKey="x" name={scatterX} type="number" /><YAxis dataKey="y" name={scatterY} type="number" /><Tooltip cursor={{ strokeDasharray: '3 3' }} /><Scatter name={`${scatterX} × ${scatterY}`} data={scatterRecords} fill="#61d6bd" /></ScatterChart></ResponsiveContainer></div>}
                <p className="correlation-note">A correlação indica associação temporal; não representa causalidade ou diagnóstico conclusivo.</p>
              </section>}

              {(exploreMode === 'dashboard' || exploreMode === 'ai') && <section className="sessions-panel ai-panel">
                <div className="sessions-panel-heading"><div><p className="sessions-kicker">Análise inteligente</p><h3>Resultado do backend</h3></div><button className="button-primary" disabled={aiLoading} onClick={() => void handleAnalyse()} type="button">{aiLoading ? 'Analisando…' : 'Solicitar análise'}</button></div>
                {aiLoading && <p className="sessions-status">Consultando o serviço de análise…</p>}
                {aiError && <p className="sessions-error" role="alert">{aiError}</p>}
                {!aiLoading && aiResult && <article className="ai-result"><p>{aiResult}</p></article>}
              </section>}
            </>
          )}
        </div>
      )}
    </>
  )
}
