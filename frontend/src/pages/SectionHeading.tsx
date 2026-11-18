import { SECTION_CONTENT, type SectionId } from '../sections'

interface SectionHeadingProps {
  section: SectionId
}

export function SectionHeading({ section }: SectionHeadingProps) {
  const content = SECTION_CONTENT[section]

  return (
    <section className="page-intro" aria-labelledby="page-title">
      <p className="page-eyebrow">AutoDiagIA / {content.label}</p>
      <h2 className="page-title" id="page-title">
        {content.label}
      </h2>
      <p className="page-description">{content.description}</p>
    </section>
  )
}
