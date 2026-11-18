import { SECTION_CONTENT, type SectionId } from '../sections'
import { SectionHeading } from './SectionHeading'

interface SectionPlaceholderProps {
  section: SectionId
}

export function SectionPlaceholder({ section }: SectionPlaceholderProps) {
  const content = SECTION_CONTENT[section]

  return (
    <>
      <SectionHeading section={section} />

      <section className="placeholder-card" aria-labelledby="placeholder-title">
        <div className="placeholder-art" aria-hidden="true">
          <div className="instrument">
            <svg viewBox="0 0 48 48" fill="none">
              <path
                d="M8 31a16 16 0 1 1 32 0M24 24l8-8M14 31h20"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <circle cx="24" cy="24" r="2" fill="currentColor" />
              <path
                d="M12.5 20.5 15 22M35.5 20.5 33 22M24 10v3"
                stroke="currentColor"
                strokeLinecap="round"
              />
            </svg>
          </div>
        </div>
        <div className="placeholder-copy">
          <span className="placeholder-label">Estrutura inicial</span>
          <h3 className="placeholder-title" id="placeholder-title">
            {content.placeholderTitle}
          </h3>
          <p className="placeholder-description">
            {content.placeholderDescription}
          </p>
          <div className="placeholder-note">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <circle
                cx="10"
                cy="10"
                r="7.25"
                stroke="currentColor"
                strokeWidth="1.5"
              />
              <path
                d="M10 9v4M10 6.5h.01"
                stroke="currentColor"
                strokeLinecap="round"
                strokeWidth="1.5"
              />
            </svg>
            <span>{content.note}</span>
          </div>
        </div>
      </section>
    </>
  )
}
