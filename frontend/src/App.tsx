import { useState } from 'react'
import { Sidebar } from './components/layout/Sidebar'
import { TopBar } from './components/layout/TopBar'
import { TelemetryPage } from './pages/TelemetryPage'
import { SectionPlaceholder } from './pages/SectionPlaceholder'
import { type SectionId } from './sections'
import './App.css'

function App() {
  const [activeSection, setActiveSection] = useState<SectionId>('telemetry')

  return (
    <div className="app-shell">
      <Sidebar
        activeSection={activeSection}
        onSectionChange={setActiveSection}
      />
      <div className="app-main">
        <TopBar section={activeSection} />
        <main className="page-content" id="main-content">
          {activeSection === 'telemetry' ? (
            <TelemetryPage />
          ) : (
            <SectionPlaceholder section={activeSection} />
          )}
        </main>
      </div>
    </div>
  )
}

export default App
