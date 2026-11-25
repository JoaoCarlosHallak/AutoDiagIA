export type PidZone =
  | 'motor'
  | 'combustivel'
  | 'emissoes'
  | 'ecu'
  | 'cabine'
  | 'rodagem'
  | 'ambiente'

export type ComponentKind =
  | 'ecu'
  | 'engine'
  | 'sensor'
  | 'coolant-sensor'
  | 'map-sensor'
  | 'crank-wheel'
  | 'speed-sensor'
  | 'ignition-coil'
  | 'intake-temp'
  | 'maf-sensor'
  | 'throttle-body'
  | 'lambda-probe'
  | 'fuel-rail'
  | 'evap-valve'
  | 'fuel-tank'
  | 'evap-pressure'
  | 'catalyst'
  | 'battery'
  | 'ambient-sensor'
  | 'accelerator-pedal'

export interface VehicleComponent {
  id: string
  label: string
  description: string
  kind: ComponentKind
  position: [number, number, number]
  zone: PidZone
}

export interface PidLocation {
  componentId: string
  zone: PidZone
  area: string
}

export const PID_ZONE_LABELS: Record<PidZone, string> = {
  motor: 'Motor e admissão',
  combustivel: 'Combustível e EVAP',
  emissoes: 'Escape e emissões',
  ecu: 'ECU e monitores',
  cabine: 'Cabine e acelerador',
  rodagem: 'Transmissão e rodagem',
  ambiente: 'Ambiente externo',
}

export const PID_ZONE_COLORS: Record<PidZone, string> = {
  motor: '#61d6bd',
  combustivel: '#f4c46b',
  emissoes: '#ed9871',
  ecu: '#9daeff',
  cabine: '#c4a0ff',
  rodagem: '#83bdf5',
  ambiente: '#7cd7e4',
}

export const VEHICLE_COMPONENTS: Record<string, VehicleComponent> = {
  ecu: {
    id: 'ecu',
    label: 'Módulo de controle (ECU)',
    description:
      'Leituras calculadas ou estados informados pela central; não representam um sensor físico individual.',
    kind: 'ecu',
    position: [-0.62, 0.94, 0.48],
    zone: 'ecu',
  },
  engine: {
    id: 'engine',
    label: 'Bloco do motor',
    description:
      'Parâmetros calculados para o funcionamento e a carga do motor.',
    kind: 'engine',
    position: [0, 0.83, -1.08],
    zone: 'motor',
  },
  coolant: {
    id: 'coolant',
    label: 'Sensor de temperatura do líquido',
    description:
      'Sensor instalado na passagem do líquido de arrefecimento do motor.',
    kind: 'coolant-sensor',
    position: [0.55, 1.02, -1.12],
    zone: 'motor',
  },
  manifold: {
    id: 'manifold',
    label: 'Sensor MAP / coletor de admissão',
    description:
      'O sensor MAP mede a pressão no coletor de admissão.',
    kind: 'map-sensor',
    position: [-0.56, 1.07, -0.91],
    zone: 'motor',
  },
  crank: {
    id: 'crank',
    label: 'Roda fônica do virabrequim',
    description:
      'A ECU calcula a rotação do motor a partir do sinal de posição do virabrequim.',
    kind: 'crank-wheel',
    position: [0.47, 0.65, -1.08],
    zone: 'motor',
  },
  speed: {
    id: 'speed',
    label: 'Sensor de velocidade da transmissão',
    description:
      'O PID informa a velocidade do veículo reportada pela ECU, geralmente derivada do sistema de velocidade.',
    kind: 'speed-sensor',
    position: [0.63, 0.69, 0.14],
    zone: 'rodagem',
  },
  ignition: {
    id: 'ignition',
    label: 'Bobina / sistema de ignição',
    description: 'O avanço de ignição é comandado e informado pela ECU.',
    kind: 'ignition-coil',
    position: [0.38, 1.03, -1.22],
    zone: 'motor',
  },
  intake: {
    id: 'intake',
    label: 'Sensor de temperatura do ar',
    description: 'Mede a temperatura do ar no duto de admissão.',
    kind: 'intake-temp',
    position: [-0.78, 0.98, -1.55],
    zone: 'motor',
  },
  maf: {
    id: 'maf',
    label: 'Sensor MAF',
    description:
      'Mede a massa de ar que atravessa o duto de admissão.',
    kind: 'maf-sensor',
    position: [-0.78, 1.02, -1.37],
    zone: 'motor',
  },
  throttle: {
    id: 'throttle',
    label: 'Corpo de borboleta',
    description:
      'A posição real e a posição comandada da borboleta são informadas pela ECU.',
    kind: 'throttle-body',
    position: [0, 1.05, -1.63],
    zone: 'motor',
  },
  lambdaB1Pre: {
    id: 'lambdaB1Pre',
    label: 'Sonda lambda — banco 1, antes do catalisador',
    description:
      'Sonda de oxigênio no escape antes do catalisador do banco 1.',
    kind: 'lambda-probe',
    position: [-0.48, 0.57, -0.77],
    zone: 'emissoes',
  },
  lambdaB1Post: {
    id: 'lambdaB1Post',
    label: 'Sonda lambda — banco 1, depois do catalisador',
    description:
      'Sonda de oxigênio no escape depois do catalisador do banco 1.',
    kind: 'lambda-probe',
    position: [-0.5, 0.57, 0.08],
    zone: 'emissoes',
  },
  lambdaB2Pre: {
    id: 'lambdaB2Pre',
    label: 'Sonda lambda — banco 2, antes do catalisador',
    description:
      'Posição representativa para leitura de oxigênio de banda larga no escape.',
    kind: 'lambda-probe',
    position: [0.48, 0.57, -0.77],
    zone: 'emissoes',
  },
  lambdaB2Post: {
    id: 'lambdaB2Post',
    label: 'Sonda lambda — banco 2, depois do catalisador',
    description:
      'Sonda de oxigênio no escape depois do catalisador do banco 2.',
    kind: 'lambda-probe',
    position: [0.5, 0.57, 0.08],
    zone: 'emissoes',
  },
  fuelRail: {
    id: 'fuelRail',
    label: 'Rail de combustível',
    description: 'O PID reporta a pressão do rail de combustível.',
    kind: 'fuel-rail',
    position: [0.58, 0.94, -0.98],
    zone: 'combustivel',
  },
  evap: {
    id: 'evap',
    label: 'Válvula de purga EVAP',
    description:
      'Atuador que controla a passagem de vapores do sistema EVAP para o motor.',
    kind: 'evap-valve',
    position: [-0.74, 0.76, 0.82],
    zone: 'combustivel',
  },
  tank: {
    id: 'tank',
    label: 'Tanque e sensor de nível',
    description:
      'O sensor de nível mede o combustível; dados de tipo são informados pela ECU.',
    kind: 'fuel-tank',
    position: [0.1, 0.48, 1.42],
    zone: 'combustivel',
  },
  evapPressure: {
    id: 'evapPressure',
    label: 'Sensor de pressão do EVAP',
    description:
      'Mede a pressão ou o vácuo no sistema de vapores do tanque.',
    kind: 'evap-pressure',
    position: [0.42, 0.61, 1.29],
    zone: 'combustivel',
  },
  catB1: {
    id: 'catB1',
    label: 'Catalisador — banco 1',
    description:
      'Componente do escape cuja temperatura é reportada pela ECU.',
    kind: 'catalyst',
    position: [-0.44, 0.43, -0.34],
    zone: 'emissoes',
  },
  catB2: {
    id: 'catB2',
    label: 'Catalisador — banco 2',
    description:
      'Componente do escape cuja temperatura é reportada pela ECU.',
    kind: 'catalyst',
    position: [0.44, 0.43, -0.34],
    zone: 'emissoes',
  },
  battery: {
    id: 'battery',
    label: 'Bateria',
    description:
      'A tensão do módulo é uma leitura elétrica reportada pela ECU.',
    kind: 'battery',
    position: [0.75, 0.76, -1.55],
    zone: 'ecu',
  },
  ambient: {
    id: 'ambient',
    label: 'Sensor de temperatura ambiente',
    description:
      'Representado na região frontal, onde normalmente fica a medição do ar externo.',
    kind: 'ambient-sensor',
    position: [0.88, 0.81, -2.02],
    zone: 'ambiente',
  },
  pedal: {
    id: 'pedal',
    label: 'Pedal do acelerador',
    description:
      'Os canais D e E são sensores redundantes do pedal eletrônico.',
    kind: 'accelerator-pedal',
    position: [-0.45, 0.78, 0.57],
    zone: 'cabine',
  },
}

export const PID_LOCATIONS: Record<string, PidLocation> = {
  '01': { componentId: 'ecu', zone: 'ecu', area: 'Módulo de controle' },
  '03': { componentId: 'ecu', zone: 'combustivel', area: 'Controle de combustível pela ECU' },
  '04': { componentId: 'engine', zone: 'motor', area: 'Carga calculada do motor' },
  '05': { componentId: 'coolant', zone: 'motor', area: 'Arrefecimento do motor' },
  '06': { componentId: 'ecu', zone: 'motor', area: 'Correção de mistura — banco 1' },
  '07': { componentId: 'ecu', zone: 'motor', area: 'Correção de mistura — banco 1' },
  '08': { componentId: 'ecu', zone: 'motor', area: 'Correção de mistura — banco 2' },
  '09': { componentId: 'ecu', zone: 'motor', area: 'Correção de mistura — banco 2' },
  '0B': { componentId: 'manifold', zone: 'motor', area: 'Coletor de admissão' },
  '0C': { componentId: 'crank', zone: 'motor', area: 'Virabrequim e roda fônica' },
  '0D': { componentId: 'speed', zone: 'rodagem', area: 'Transmissão / sensor de velocidade' },
  '0E': { componentId: 'ignition', zone: 'motor', area: 'Sistema de ignição' },
  '0F': { componentId: 'intake', zone: 'motor', area: 'Duto de admissão' },
  '10': { componentId: 'maf', zone: 'motor', area: 'Duto de admissão / sensor MAF' },
  '11': { componentId: 'throttle', zone: 'motor', area: 'Corpo de borboleta' },
  '13': { componentId: 'ecu', zone: 'ecu', area: 'Configuração de sensores na ECU' },
  '14': { componentId: 'lambdaB1Pre', zone: 'emissoes', area: 'Antes do catalisador — banco 1' },
  '15': { componentId: 'lambdaB1Post', zone: 'emissoes', area: 'Depois do catalisador — banco 1' },
  '19': { componentId: 'lambdaB2Post', zone: 'emissoes', area: 'Depois do catalisador — banco 2' },
  '1C': { componentId: 'ecu', zone: 'ecu', area: 'Padrão de diagnóstico na ECU' },
  '1F': { componentId: 'ecu', zone: 'ecu', area: 'Tempo calculado pela ECU' },
  '21': { componentId: 'ecu', zone: 'ecu', area: 'Distância calculada pela ECU' },
  '23': { componentId: 'fuelRail', zone: 'combustivel', area: 'Rail de combustível' },
  '2E': { componentId: 'evap', zone: 'combustivel', area: 'Válvula de purga EVAP' },
  '2F': { componentId: 'tank', zone: 'combustivel', area: 'Tanque e sensor de nível' },
  '30': { componentId: 'ecu', zone: 'ecu', area: 'Contagem de ciclos na ECU' },
  '31': { componentId: 'ecu', zone: 'ecu', area: 'Distância calculada pela ECU' },
  '32': { componentId: 'evapPressure', zone: 'combustivel', area: 'Sistema de vapores EVAP' },
  '33': { componentId: 'ecu', zone: 'ecu', area: 'Pressão atmosférica reportada pela ECU' },
  '34': { componentId: 'lambdaB1Pre', zone: 'emissoes', area: 'Sonda de banda larga — banco 1' },
  '38': { componentId: 'lambdaB2Pre', zone: 'emissoes', area: 'Sonda de banda larga — posição 5' },
  '3C': { componentId: 'catB1', zone: 'emissoes', area: 'Catalisador — banco 1' },
  '3D': { componentId: 'catB2', zone: 'emissoes', area: 'Catalisador — banco 2' },
  '41': { componentId: 'ecu', zone: 'ecu', area: 'Monitores de emissões na ECU' },
  '42': { componentId: 'battery', zone: 'ecu', area: 'Alimentação elétrica' },
  '43': { componentId: 'engine', zone: 'motor', area: 'Carga calculada pela ECU' },
  '44': { componentId: 'ecu', zone: 'combustivel', area: 'Mistura comandada pela ECU' },
  '45': { componentId: 'throttle', zone: 'motor', area: 'Corpo de borboleta' },
  '46': { componentId: 'ambient', zone: 'ambiente', area: 'Região frontal do veículo' },
  '47': { componentId: 'throttle', zone: 'motor', area: 'Segundo canal da borboleta' },
  '49': { componentId: 'pedal', zone: 'cabine', area: 'Pedal do acelerador' },
  '4A': { componentId: 'pedal', zone: 'cabine', area: 'Pedal do acelerador' },
  '4C': { componentId: 'throttle', zone: 'motor', area: 'Atuador do corpo de borboleta' },
  '51': { componentId: 'ecu', zone: 'ecu', area: 'Tipo de combustível configurado' },
  '56': { componentId: 'ecu', zone: 'emissoes', area: 'Correção secundária — banco 1' },
  '58': { componentId: 'ecu', zone: 'emissoes', area: 'Correção secundária — banco 2' },
}
