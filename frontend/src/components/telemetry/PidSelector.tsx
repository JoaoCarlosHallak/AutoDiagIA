import { useState } from 'react'
import { PID_OPTIONS } from '../../data/obdPids'

interface PidSelectorProps {
  selectedPids: string[]
  isReading: boolean
  readingPid: string | null
  isLiveUpdating: boolean
  onTogglePid: (pid: string) => void
  onReadPid: (pid: string) => void
  onReadSelected: () => void
}

export function PidSelector({
  selectedPids,
  isReading,
  readingPid,
  isLiveUpdating,
  onTogglePid,
  onReadPid,
  onReadSelected,
}: PidSelectorProps) {
  const [filter, setFilter] = useState('')
  const visiblePids = PID_OPTIONS.filter((pid) =>
    `${pid.code} ${pid.name} ${pid.description}`
      .toLocaleLowerCase('pt-BR')
      .includes(filter.trim().toLocaleLowerCase('pt-BR')),
  )

  return (
    <section className="telemetry-panel" aria-labelledby="pid-panel-title">
      <div className="telemetry-panel-heading">
        <div>
          <p className="telemetry-panel-kicker">Parâmetros OBD-II</p>
          <h3 className="telemetry-panel-title" id="pid-panel-title">
            Escolha os PIDs
          </h3>
        </div>
        <span className="selection-count">
          {selectedPids.length} selecionado
          {selectedPids.length === 1 ? '' : 's'}
        </span>
      </div>

      <label className="pid-filter">
        <span className="visually-hidden">
          Filtrar PIDs por nome, código ou unidade
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
          placeholder="Filtrar por nome ou código"
          type="search"
          value={filter}
        />
      </label>

      <fieldset className="pid-options">
        <legend className="visually-hidden">
          Parâmetros disponíveis para leitura
        </legend>
        {visiblePids.map((pid) => (
          <div className="pid-option" key={pid.code}>
            <label className="pid-select" htmlFor={`pid-${pid.code}`}>
              <input
                checked={selectedPids.includes(pid.code)}
                disabled={isReading}
                id={`pid-${pid.code}`}
                onChange={() => onTogglePid(pid.code)}
                type="checkbox"
              />
              <span className="pid-checkmark" aria-hidden="true" />
              <span className="pid-copy">
                <span className="pid-name">{pid.name}</span>
                <span className="pid-description">{pid.description}</span>
              </span>
              <code className="pid-code">{pid.code}</code>
            </label>
            <button
              className="button-quiet"
              disabled={isReading || isLiveUpdating}
              onClick={() => onReadPid(pid.code)}
              type="button"
            >
              {readingPid === pid.code ? 'Lendo…' : 'Ler'}
            </button>
          </div>
        ))}
        {visiblePids.length === 0 && (
          <p className="pid-no-results">Nenhum PID corresponde a esse filtro.</p>
        )}
      </fieldset>

      <div className="telemetry-panel-actions">
        <span className="selection-hint">
          {isLiveUpdating
            ? 'A atualização ao vivo consulta os PIDs selecionados.'
            : 'As leituras são solicitadas ao veículo quando você clica.'}
        </span>
        <button
          className="button-primary"
          disabled={
            selectedPids.length === 0 || isReading || isLiveUpdating
          }
          onClick={onReadSelected}
          type="button"
        >
          {isReading ? 'Consultando…' : 'Ler selecionados'}
        </button>
      </div>
    </section>
  )
}
