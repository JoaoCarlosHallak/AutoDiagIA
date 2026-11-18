export type SectionId = 'telemetry' | 'sessions' | 'dtc'

export interface SectionContent {
  label: string
  description: string
  placeholderTitle: string
  placeholderDescription: string
  note: string
}

export const SECTION_CONTENT: Record<SectionId, SectionContent> = {
  telemetry: {
    label: 'Telemetria',
    description: 'Leituras e parâmetros do veículo em um só lugar.',
    placeholderTitle: 'Monitoramento em preparação',
    placeholderDescription:
      'A visualização de leituras OBD-II será conectada ao backend em uma próxima etapa.',
    note: 'Esta tela ainda não consulta o veículo nem apresenta leituras.',
  },
  sessions: {
    label: 'Sessões',
    description: 'Aquisição e análise de dados organizadas por sessão.',
    placeholderTitle: 'Sessões em preparação',
    placeholderDescription:
      'O histórico, a execução e os registros de sessões serão implementados em uma próxima etapa.',
    note: 'Nenhuma sessão está sendo carregada ou simulada nesta tela.',
  },
  dtc: {
    label: 'DTC',
    description: 'Códigos de falha e informações de diagnóstico.',
    placeholderTitle: 'Consulta DTC em preparação',
    placeholderDescription:
      'A consulta de códigos de falha será conectada ao backend em uma próxima etapa.',
    note: 'Nenhum código ou resultado de consulta é exibido nesta estrutura inicial.',
  },
}
