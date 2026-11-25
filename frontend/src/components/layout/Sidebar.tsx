import { SECTION_CONTENT, type SectionId } from '../../sections'

interface SidebarProps {
  activeSection: SectionId
  onSectionChange: (section: SectionId) => void
}

const sectionIds: SectionId[] = ['overview', 'telemetry', 'sessions', 'dtc']

function SectionIcon({ section }: { section: SectionId }) {
  if (section === 'overview') {
    return (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="m4 14 1.4-5.1a2 2 0 0 1 1.9-1.5h9.4a2 2 0 0 1 1.9 1.5L20 14v4h-2v-2H6v2H4v-4Z"
          stroke="currentColor"
          strokeLinejoin="round"
          strokeWidth="1.6"
        />
        <path
          d="M5 13h14M7.5 10.5h.01M16.5 10.5h.01"
          stroke="currentColor"
          strokeLinecap="round"
          strokeWidth="1.6"
        />
      </svg>
    )
  }

  if (section === 'telemetry') {
    return (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M4.6 18.2a9.5 9.5 0 1 1 14.8 0M12 12l4.2-4.2M7.5 18.2h9"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="1.6"
        />
        <circle cx="12" cy="12" r="1.3" fill="currentColor" />
      </svg>
    )
  }

  if (section === 'sessions') {
    return (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <rect
          x="4"
          y="4.5"
          width="16"
          height="15"
          rx="2.5"
          stroke="currentColor"
          strokeWidth="1.6"
        />
        <path
          d="M8 9h8M8 12.5h5M8 16h3"
          stroke="currentColor"
          strokeLinecap="round"
          strokeWidth="1.6"
        />
      </svg>
    )
  }

  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 4.3 21 19.7H3L12 4.3Z"
        stroke="currentColor"
        strokeLinejoin="round"
        strokeWidth="1.6"
      />
      <path
        d="M12 9v4.5M12 16.5h.01"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="1.8"
      />
    </svg>
  )
}

export function Sidebar({ activeSection, onSectionChange }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <path
              d="M4 13.5h2.2l1.5-4 2.5 7 2.1-5 1.4 2h2.1"
              stroke="currentColor"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="1.8"
            />
            <path
              d="M12 2.8 20 6v5.6c0 4.3-3.1 7.6-8 9.6-4.9-2-8-5.3-8-9.6V6l8-3.2Z"
              stroke="currentColor"
              strokeLinejoin="round"
              strokeWidth="1.3"
            />
          </svg>
        </span>
        <span>
          <span className="brand-name">AutoDiagIA</span>
          <span className="brand-caption">Diagnóstico veicular</span>
        </span>
      </div>

      <p className="sidebar-section-label">Módulos</p>
      <nav className="sidebar-nav" aria-label="Navegação principal">
        {sectionIds.map((section) => (
          <button
            key={section}
            className="nav-item"
            type="button"
            aria-pressed={activeSection === section}
            onClick={() => onSectionChange(section)}
          >
            <span className="nav-icon">
              <SectionIcon section={section} />
            </span>
            <span className="nav-copy">
              <span className="nav-title">{SECTION_CONTENT[section].label}</span>
              <span className="nav-description">
                {{
                  overview: 'Mapa do veículo',
                  telemetry: 'Dados do veículo',
                  sessions: 'Coleta e histórico',
                  dtc: 'Códigos de falha',
                }[section]}
              </span>
            </span>
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <span className="footer-mark" aria-hidden="true">
          OBD
        </span>
        <span>
          <span className="footer-label">Painel de diagnóstico</span>
          <span className="footer-caption">Interface AutoDiagIA</span>
        </span>
      </div>
    </aside>
  )
}
