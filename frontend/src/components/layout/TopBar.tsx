import { SECTION_CONTENT, type SectionId } from '../../sections'

interface TopBarProps {
  section: SectionId
}

export function TopBar({ section }: TopBarProps) {
  return (
    <header className="topbar">
      <div>
        <span className="topbar-kicker">Central de diagnóstico</span>
        <h1 className="topbar-heading">{SECTION_CONTENT[section].label}</h1>
      </div>
      <div className="topbar-context" aria-label="Interface de diagnóstico OBD-II">
        <span className="context-mark" aria-hidden="true" />
        <span>OBD-II</span>
      </div>
    </header>
  )
}
