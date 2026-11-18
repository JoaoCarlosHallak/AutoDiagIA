export interface PidOption {
  code: string
  name: string
  description: string
}

export const PID_OPTIONS: PidOption[] = [
  { code: '01', name: 'Status do Monitor', description: 'máscara de bits' },
  {
    code: '03',
    name: 'Status do Sistema de Combustível',
    description: 'status',
  },
  { code: '04', name: 'Carga Calculada do Motor', description: '%' },
  {
    code: '05',
    name: 'Temperatura do Líquido de Arrefecimento do Motor',
    description: '°C',
  },
  {
    code: '06',
    name: 'Ajuste de Combustível de Curto Prazo Banco 1',
    description: '%',
  },
  {
    code: '07',
    name: 'Ajuste de Combustível de Longo Prazo Banco 1',
    description: '%',
  },
  {
    code: '08',
    name: 'Ajuste de Combustível de Curto Prazo Banco 2',
    description: '%',
  },
  {
    code: '09',
    name: 'Ajuste de Combustível de Longo Prazo Banco 2',
    description: '%',
  },
  { code: '0B', name: 'Pressão do Coletor de Admissão', description: 'kPa' },
  { code: '0C', name: 'Rotação do Motor', description: 'rpm' },
  { code: '0D', name: 'Velocidade do Veículo', description: 'km/h' },
  { code: '0E', name: 'Avanço de Ignição', description: '°' },
  { code: '0F', name: 'Temperatura do Ar de Admissão', description: '°C' },
  { code: '10', name: 'Fluxo de Ar MAF', description: 'g/s' },
  { code: '11', name: 'Posição da Borboleta', description: '%' },
  {
    code: '13',
    name: 'Sensores de O2 Presentes',
    description: 'máscara de bits',
  },
  {
    code: '14',
    name: 'Sensor de O2 Pré-Catalisador (Lado 1)',
    description: 'misto',
  },
  {
    code: '15',
    name: 'Sensor de O2 Pós-Catalisador (Lado 1)',
    description: 'misto',
  },
  {
    code: '19',
    name: 'Sensor de O2 Pós-Catalisador (Lado 2)',
    description: 'misto',
  },
  { code: '1C', name: 'Padrão OBD', description: 'código' },
  { code: '1F', name: 'Tempo de Funcionamento do Motor', description: 's' },
  { code: '21', name: 'Distância com a MIL Acesa', description: 'km' },
  {
    code: '23',
    name: 'Pressão do Rail de Combustível',
    description: 'kPa',
  },
  { code: '2E', name: 'Purga EVAP Comandada', description: '%' },
  { code: '2F', name: 'Nível de Combustível', description: '%' },
  { code: '30', name: 'Aquecimentos Desde a Limpeza', description: 'contagem' },
  { code: '31', name: 'Distância Desde a Limpeza', description: 'km' },
  { code: '32', name: 'Pressão de Vapor do EVAP', description: 'Pa' },
  { code: '33', name: 'Pressão Barométrica', description: 'kPa' },
  { code: '34', name: 'Sensor de O2 1 Banda Larga', description: 'misto' },
  { code: '38', name: 'Sensor de O2 5 Banda Larga', description: 'misto' },
  { code: '3C', name: 'Temp. do Catalisador (Lado 1)', description: '°C' },
  { code: '3D', name: 'Temp. do Catalisador (Lado 2)(V)', description: '°C' },
  {
    code: '41',
    name: 'Status do Monitor neste Ciclo de Condução',
    description: 'máscara de bits',
  },
  { code: '42', name: 'Tensão do Módulo de Controle', description: 'V' },
  { code: '43', name: 'Carga Absoluta', description: '%' },
  {
    code: '44',
    name: 'Razão de Equivalência Comandada',
    description: 'razão',
  },
  { code: '45', name: 'Posição Relativa da Borboleta', description: '%' },
  { code: '46', name: 'Temperatura do Ar Ambiente', description: '°C' },
  { code: '47', name: 'Posição da Borboleta B', description: '%' },
  {
    code: '49',
    name: 'Posição do Pedal do Acelerador D',
    description: '%',
  },
  {
    code: '4A',
    name: 'Posição do Pedal do Acelerador E',
    description: '%',
  },
  { code: '4C', name: 'Atuador da Borboleta Comandado', description: '%' },
  { code: '51', name: 'Tipo de Combustível', description: 'código' },
  {
    code: '56',
    name: 'Ajuste Secundário de O2 de Curto Prazo B1',
    description: '%',
  },
  {
    code: '58',
    name: 'Ajuste Secundário de O2 de Curto Prazo B2',
    description: '%',
  },
]
