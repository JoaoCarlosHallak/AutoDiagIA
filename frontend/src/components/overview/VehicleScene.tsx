import { useEffect, useRef } from 'react'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
import type { PidOption } from '../../data/obdPids'
import {
  PID_LOCATIONS,
  PID_ZONE_COLORS,
  VEHICLE_COMPONENTS,
} from '../../data/pidLocations'

interface VehicleSceneProps {
  pids: PidOption[]
  selectedComponentId: string | null
  onSelectComponent: (componentId: string) => void
}

function addBox(
  parent: THREE.Group,
  size: [number, number, number],
  position: [number, number, number],
  color: string,
  options: { opacity?: number; metalness?: number; roughness?: number } = {},
) {
  const transparent = options.opacity !== undefined
  const mesh = new THREE.Mesh(
    new THREE.BoxGeometry(...size),
    new THREE.MeshStandardMaterial({
      color,
      metalness: options.metalness ?? 0.35,
      roughness: options.roughness ?? 0.4,
      transparent,
      opacity: options.opacity ?? 1,
      depthWrite: !transparent,
      side: transparent ? THREE.DoubleSide : THREE.FrontSide,
    }),
  )
  mesh.position.set(...position)
  parent.add(mesh)
  return mesh
}

function addCylinder(
  parent: THREE.Group,
  radiusTop: number,
  radiusBottom: number,
  height: number,
  position: [number, number, number],
  color: string,
  segments = 14,
  rotation: [number, number, number] = [0, 0, 0],
) {
  const mesh = new THREE.Mesh(
    new THREE.CylinderGeometry(
      radiusTop,
      radiusBottom,
      height,
      segments,
    ),
    new THREE.MeshStandardMaterial({
      color,
      metalness: 0.68,
      roughness: 0.34,
    }),
  )
  mesh.position.set(...position)
  mesh.rotation.set(...rotation)
  parent.add(mesh)
  return mesh
}

function addTube(
  parent: THREE.Group,
  points: [number, number, number][],
  radius: number,
  color: string,
) {
  const curve = new THREE.CatmullRomCurve3(
    points.map(([x, y, z]) => new THREE.Vector3(x, y, z)),
  )
  const mesh = new THREE.Mesh(
    new THREE.TubeGeometry(curve, 18, radius, 8, false),
    new THREE.MeshStandardMaterial({
      color,
      metalness: 0.55,
      roughness: 0.36,
    }),
  )
  parent.add(mesh)
  return mesh
}

function addWheel(parent: THREE.Group, x: number, z: number) {
  const tire = new THREE.Mesh(
    new THREE.CylinderGeometry(0.39, 0.39, 0.27, 24),
    new THREE.MeshStandardMaterial({ color: '#10171b', roughness: 0.82 }),
  )
  tire.rotation.z = Math.PI / 2
  tire.position.set(x, 0.43, z)
  parent.add(tire)

  const hub = new THREE.Mesh(
    new THREE.CylinderGeometry(0.2, 0.2, 0.285, 20),
    new THREE.MeshStandardMaterial({
      color: '#82969b',
      metalness: 0.72,
      roughness: 0.3,
    }),
  )
  hub.rotation.z = Math.PI / 2
  hub.position.set(x, 0.43, z)
  parent.add(hub)
}

function addShellOutline(
  parent: THREE.Group,
  geometry: THREE.BufferGeometry,
  position: [number, number, number],
  rotation: [number, number, number] = [0, 0, 0],
) {
  const outline = new THREE.LineSegments(
    new THREE.EdgesGeometry(geometry),
    new THREE.LineBasicMaterial({
      color: '#78d8c8',
      transparent: true,
      opacity: 0.46,
    }),
  )
  outline.position.set(...position)
  outline.rotation.set(...rotation)
  parent.add(outline)
}

function createVehicle(scene: THREE.Scene) {
  const vehicle = new THREE.Group()
  const shellOptions = { opacity: 0.12, metalness: 0.24, roughness: 0.22 }

  const bodyGeometry = new THREE.BoxGeometry(1.95, 0.52, 4.12)
  const body = new THREE.Mesh(
    bodyGeometry,
    new THREE.MeshPhysicalMaterial({
      color: '#50aa9d',
      transparent: true,
      opacity: 0.13,
      roughness: 0.2,
      metalness: 0.24,
      side: THREE.DoubleSide,
      depthWrite: false,
    }),
  )
  body.position.set(0, 0.75, 0)
  vehicle.add(body)
  addShellOutline(vehicle, bodyGeometry, [0, 0.75, 0])

  addBox(vehicle, [1.86, 0.16, 1.12], [0, 1.0, -1.42], '#55bdad', shellOptions)
  addBox(vehicle, [1.84, 0.16, 0.82], [0, 0.98, 1.55], '#55bdad', shellOptions)
  addBox(vehicle, [1.58, 0.1, 1.34], [0, 1.67, -0.07], '#76d5ca', shellOptions)
  addBox(vehicle, [1.9, 0.12, 0.18], [0, 0.88, 2.03], '#b6d8d1', shellOptions)
  addBox(vehicle, [1.9, 0.12, 0.18], [0, 0.88, -2.03], '#b6d8d1', shellOptions)

  const cabinShape = new THREE.Shape()
  cabinShape.moveTo(-1.28, 1.0)
  cabinShape.lineTo(-0.88, 1.59)
  cabinShape.lineTo(0.56, 1.59)
  cabinShape.lineTo(1.2, 1.0)
  cabinShape.closePath()
  const cabinGeometry = new THREE.ExtrudeGeometry(cabinShape, {
    depth: 1.5,
    bevelEnabled: true,
    bevelSegments: 2,
    bevelSize: 0.025,
    bevelThickness: 0.025,
  })
  cabinGeometry.translate(0, 0, -0.75)
  const cabin = new THREE.Mesh(
    cabinGeometry,
    new THREE.MeshPhysicalMaterial({
      color: '#70bbb5',
      transparent: true,
      opacity: 0.11,
      metalness: 0.18,
      roughness: 0.16,
      side: THREE.DoubleSide,
      depthWrite: false,
    }),
  )
  cabin.rotation.y = Math.PI / 2
  vehicle.add(cabin)
  addShellOutline(vehicle, cabinGeometry, [0, 0, 0], [0, Math.PI / 2, 0])

  addBox(vehicle, [1.62, 0.14, 3.65], [0, 0.42, 0], '#26383d')
  addBox(vehicle, [0.12, 0.16, 3.7], [-0.67, 0.47, 0], '#52676b')
  addBox(vehicle, [0.12, 0.16, 3.7], [0.67, 0.47, 0], '#52676b')
  addTube(vehicle, [[-0.45, 0.37, -1.55], [-0.45, 0.37, -0.4], [-0.45, 0.37, 0.85]], 0.035, '#87969a')
  addTube(vehicle, [[0.45, 0.37, -1.55], [0.45, 0.37, -0.4], [0.45, 0.37, 0.85]], 0.035, '#87969a')

  for (const x of [-0.96, 0.96]) {
    addBox(vehicle, [0.06, 0.07, 0.48], [x, 0.88, -1.78], '#e7e5bf')
    addBox(vehicle, [0.06, 0.07, 0.42], [x, 0.88, 1.84], '#d06b63')
  }

  for (const x of [-1.03, 1.03]) {
    addWheel(vehicle, x, -1.26)
    addWheel(vehicle, x, 1.28)
  }

  scene.add(vehicle)
}

function makeSensor(
  group: THREE.Group,
  accent: string,
  { longProbe = false }: { longProbe?: boolean } = {},
) {
  addCylinder(group, 0.045, 0.055, longProbe ? 0.25 : 0.17, [0, 0, 0], '#aab8bb')
  addCylinder(group, 0.082, 0.082, 0.07, [0, 0.1, 0], '#c3cdd0', 6)
  addBox(group, [0.12, 0.1, 0.13], [0, 0.21, 0], accent)
  addTube(
    group,
    [[0, 0.25, 0], [0.03, 0.34, 0.02], [0.1, 0.37, 0.04]],
    0.018,
    '#171e22',
  )
  addBox(group, [0.1, 0.075, 0.09], [0.14, 0.37, 0.04], '#303a3e')
}

function createComponentModel(
  componentId: string,
  accent: string,
): THREE.Group {
  const component = VEHICLE_COMPONENTS[componentId]
  if (!component) {
    throw new Error(`Componente 3D desconhecido: ${componentId}.`)
  }

  const group = new THREE.Group()
  group.position.set(...component.position)
  group.userData.componentId = component.id
  group.userData.accent = accent

  switch (component.kind) {
    case 'ecu':
      addBox(group, [0.38, 0.2, 0.28], [0, 0, 0], '#60747a')
      addBox(group, [0.32, 0.06, 0.22], [0, 0.12, 0], '#84969a')
      for (let index = 0; index < 5; index += 1) {
        addBox(group, [0.025, 0.018, 0.16], [-0.12 + index * 0.06, 0.155, 0], accent)
      }
      addBox(group, [0.1, 0.08, 0.12], [0, -0.12, -0.19], '#36464c')
      break
    case 'engine':
      addBox(group, [0.82, 0.52, 0.88], [0, 0, 0], '#63777a')
      addBox(group, [0.64, 0.15, 0.67], [0, 0.3, 0], '#849497')
      for (let index = 0; index < 4; index += 1) {
        addBox(group, [0.1, 0.09, 0.45], [-0.24 + index * 0.16, 0.39, 0], '#4a5b60')
      }
      addCylinder(group, 0.09, 0.09, 0.27, [0.47, -0.08, 0], '#b3c0c1', 12, [0, 0, Math.PI / 2])
      break
    case 'coolant-sensor':
      makeSensor(group, accent)
      break
    case 'map-sensor':
      addBox(group, [0.24, 0.14, 0.2], [0, 0.07, 0], '#343f44')
      addCylinder(group, 0.055, 0.04, 0.14, [0, -0.08, 0], '#aab7b9')
      addBox(group, [0.12, 0.07, 0.1], [0, 0.16, 0], accent)
      break
    case 'crank-wheel': {
      addCylinder(group, 0.22, 0.22, 0.065, [0, 0, 0], '#a9b5b7', 16, [Math.PI / 2, 0, 0])
      addCylinder(group, 0.08, 0.08, 0.08, [0, 0, 0], '#526166', 12, [Math.PI / 2, 0, 0])
      for (let tooth = 0; tooth < 16; tooth += 1) {
        const angle = (tooth / 16) * Math.PI * 2
        const gearTooth = addBox(
          group,
          [0.07, 0.05, 0.05],
          [Math.cos(angle) * 0.225, Math.sin(angle) * 0.225, 0],
          '#c4cece',
        )
        gearTooth.rotation.z = angle
      }
      addBox(group, [0.13, 0.15, 0.12], [0.28, 0, 0], accent)
      break
    }
    case 'speed-sensor':
      addCylinder(group, 0.07, 0.09, 0.25, [0, 0, 0], '#929fa3')
      addBox(group, [0.16, 0.1, 0.15], [0, 0.15, 0], accent)
      addCylinder(group, 0.12, 0.12, 0.06, [0, -0.14, 0], '#b7c1c1', 10)
      break
    case 'ignition-coil':
      for (let index = 0; index < 3; index += 1) {
        addBox(group, [0.12, 0.2, 0.12], [-0.16 + index * 0.16, 0.04, 0], '#53676c')
        addCylinder(group, 0.045, 0.045, 0.18, [-0.16 + index * 0.16, -0.13, 0], accent, 10)
      }
      addBox(group, [0.52, 0.07, 0.16], [0, 0.17, 0], '#77898d')
      break
    case 'intake-temp':
      makeSensor(group, accent)
      break
    case 'maf-sensor':
      addCylinder(group, 0.12, 0.12, 0.42, [0, 0, 0], '#39494e', 18, [Math.PI / 2, 0, 0])
      addBox(group, [0.25, 0.18, 0.2], [0, 0.17, 0], '#6d7f82')
      addBox(group, [0.13, 0.07, 0.13], [0, 0.28, 0], accent)
      break
    case 'throttle-body': {
      const ring = new THREE.Mesh(
        new THREE.TorusGeometry(0.2, 0.055, 10, 24),
        new THREE.MeshStandardMaterial({ color: '#9eacad', metalness: 0.78, roughness: 0.28 }),
      )
      group.add(ring)
      const plate = new THREE.Mesh(
        new THREE.CylinderGeometry(0.17, 0.17, 0.035, 24),
        new THREE.MeshStandardMaterial({ color: '#59696d', metalness: 0.55, roughness: 0.34 }),
      )
      plate.rotation.x = Math.PI / 2
      plate.rotation.z = 0.28
      group.add(plate)
      addBox(group, [0.14, 0.23, 0.13], [0.24, 0, 0], accent)
      addCylinder(group, 0.04, 0.04, 0.3, [0.31, 0, 0], '#bac6c6', 10, [0, 0, Math.PI / 2])
      break
    }
    case 'lambda-probe':
      makeSensor(group, accent, { longProbe: true })
      group.rotation.z = Math.PI / 2
      break
    case 'fuel-rail':
      addCylinder(group, 0.055, 0.055, 0.58, [0, 0.08, 0], '#aebcbd', 16, [Math.PI / 2, 0, 0])
      for (let index = 0; index < 4; index += 1) {
        const z = -0.2 + index * 0.13
        addCylinder(group, 0.04, 0.04, 0.18, [0, -0.1, z], '#74868a')
        addBox(group, [0.08, 0.06, 0.08], [0, -0.2, z], accent)
      }
      break
    case 'evap-valve':
      addCylinder(group, 0.12, 0.12, 0.27, [0, 0, 0], '#4b5c60', 14, [0, 0, Math.PI / 2])
      addBox(group, [0.16, 0.18, 0.18], [0, 0.13, 0], accent)
      addTube(group, [[-0.22, -0.04, 0], [-0.3, -0.04, 0], [-0.36, 0.02, 0]], 0.025, '#26383c')
      addTube(group, [[0.22, -0.04, 0], [0.3, -0.04, 0], [0.36, 0.02, 0]], 0.025, '#26383c')
      break
    case 'fuel-tank':
      addBox(group, [0.52, 0.22, 0.58], [0, 0, 0], '#57676b')
      addBox(group, [0.41, 0.06, 0.45], [0, 0.13, 0], '#7a8b8e')
      addCylinder(group, 0.1, 0.1, 0.08, [0.12, 0.18, 0.08], accent, 12)
      addTube(group, [[-0.24, 0.05, -0.19], [-0.33, 0.17, -0.27], [-0.4, 0.18, -0.38]], 0.025, '#29393d')
      break
    case 'evap-pressure':
      addBox(group, [0.2, 0.13, 0.18], [0, 0.04, 0], '#596b70')
      addCylinder(group, 0.05, 0.04, 0.14, [0, -0.09, 0], '#9ba9aa')
      addBox(group, [0.1, 0.06, 0.1], [0, 0.15, 0], accent)
      break
    case 'catalyst':
      addCylinder(group, 0.13, 0.13, 0.42, [0, 0, 0], '#9aa8a9', 18, [Math.PI / 2, 0, 0])
      addCylinder(group, 0.075, 0.075, 0.26, [0, 0, -0.32], '#aeb8b8', 14, [Math.PI / 2, 0, 0])
      addCylinder(group, 0.075, 0.075, 0.26, [0, 0, 0.32], '#aeb8b8', 14, [Math.PI / 2, 0, 0])
      addCylinder(group, 0.045, 0.045, 0.08, [0, 0.15, 0], accent)
      break
    case 'battery':
      addBox(group, [0.42, 0.25, 0.3], [0, 0, 0], '#35464b')
      addBox(group, [0.36, 0.055, 0.26], [0, 0.15, 0], '#819195')
      addCylinder(group, 0.045, 0.045, 0.06, [-0.12, 0.2, 0], '#c7c5a5')
      addCylinder(group, 0.045, 0.045, 0.06, [0.12, 0.2, 0], accent)
      break
    case 'ambient-sensor':
      addBox(group, [0.2, 0.12, 0.16], [0, 0, 0], '#59696d')
      addCylinder(group, 0.04, 0.05, 0.17, [0, -0.11, 0], '#aebbbb')
      addBox(group, [0.08, 0.05, 0.08], [0, 0.09, 0], accent)
      break
    case 'accelerator-pedal': {
      addBox(group, [0.12, 0.05, 0.3], [0, -0.1, 0], '#526166')
      addBox(group, [0.08, 0.34, 0.08], [0, 0.08, 0.08], '#9aa7a9')
      const pedal = addBox(group, [0.24, 0.08, 0.16], [0, 0.25, 0.12], accent)
      pedal.rotation.x = -0.25
      break
    }
    default:
      break
  }

  const hitTarget = new THREE.Mesh(
    new THREE.SphereGeometry(component.kind === 'engine' ? 0.38 : 0.25, 10, 8),
    new THREE.MeshBasicMaterial({
      transparent: true,
      opacity: 0,
      colorWrite: false,
      depthWrite: false,
    }),
  )
  group.add(hitTarget)
  return group
}

function findComponentId(object: THREE.Object3D): string | null {
  let current: THREE.Object3D | null = object
  while (current) {
    if (typeof current.userData.componentId === 'string') {
      return current.userData.componentId
    }
    current = current.parent
  }
  return null
}

function setComponentHighlight(
  components: THREE.Group[],
  selectedId: string | null,
  hoveredId: string | null = null,
) {
  for (const component of components) {
    const active =
      component.userData.componentId === selectedId ||
      component.userData.componentId === hoveredId
    if (component.userData.isActive === active) {
      continue
    }

    component.userData.isActive = active
    component.scale.setScalar(active ? 1.13 : 1)
    component.traverse((object) => {
      if (!(object instanceof THREE.Mesh)) {
        return
      }
      const materials = Array.isArray(object.material)
        ? object.material
        : [object.material]
      for (const material of materials) {
        if (material instanceof THREE.MeshStandardMaterial) {
          material.emissive.set(active ? component.userData.accent : '#000000')
          material.emissiveIntensity = active ? 0.28 : 0
        }
      }
    })
  }
}

export function VehicleScene({
  pids,
  selectedComponentId,
  onSelectComponent,
}: VehicleSceneProps) {
  const mountRef = useRef<HTMLDivElement>(null)
  const controlsRef = useRef<OrbitControls | null>(null)
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null)
  const componentsRef = useRef<THREE.Group[]>([])
  const selectedComponentRef = useRef(selectedComponentId)
  const invalidPid = pids.find((pid) => {
    const location = PID_LOCATIONS[pid.code]
    return !location || !VEHICLE_COMPONENTS[location.componentId]
  })
  const sceneError = invalidPid
    ? `O PID ${invalidPid.code} não possui um componente mapeado para o modelo.`
    : null

  useEffect(() => {
    selectedComponentRef.current = selectedComponentId
    setComponentHighlight(componentsRef.current, selectedComponentId)
  }, [selectedComponentId])

  useEffect(() => {
    const mount = mountRef.current
    if (!mount || sceneError) {
      return
    }

    const scene = new THREE.Scene()
    scene.background = new THREE.Color('#0d171d')

    const camera = new THREE.PerspectiveCamera(36, 1, 0.1, 100)
    camera.position.set(5.3, 3.7, 6.3)
    cameraRef.current = camera

    let renderer: THREE.WebGLRenderer
    try {
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
    } catch {
      cameraRef.current = null
      const fallback = document.createElement('p')
      fallback.className = 'vehicle-scene-error'
      fallback.setAttribute('role', 'alert')
      fallback.textContent =
        'Seu navegador não disponibilizou WebGL para exibir o modelo 3D.'
      mount.classList.add('is-unavailable')
      mount.appendChild(fallback)
      return () => {
        fallback.remove()
        mount.classList.remove('is-unavailable')
      }
    }

    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.2
    renderer.domElement.setAttribute('aria-hidden', 'true')
    renderer.domElement.className = 'vehicle-canvas'
    mount.appendChild(renderer.domElement)

    scene.add(new THREE.HemisphereLight('#e4f6f1', '#1d292e', 2.2))
    const keyLight = new THREE.DirectionalLight('#ffffff', 3.1)
    keyLight.position.set(-4, 8, 5)
    scene.add(keyLight)
    const fillLight = new THREE.DirectionalLight('#83e1d0', 1.5)
    fillLight.position.set(5, 4, -4)
    scene.add(fillLight)

    const ground = new THREE.GridHelper(12, 24, '#28413f', '#1b2b30')
    ground.position.y = 0.035
    scene.add(ground)
    createVehicle(scene)

    const componentIds = [
      ...new Set(pids.map((pid) => PID_LOCATIONS[pid.code].componentId)),
    ]
    const componentGroups = componentIds.map((componentId) => {
      const component = VEHICLE_COMPONENTS[componentId]
      if (!component) {
        throw new Error(`Componente 3D desconhecido: ${componentId}.`)
      }
      const group = createComponentModel(
        componentId,
        PID_ZONE_COLORS[component.zone],
      )
      scene.add(group)
      return group
    })
    componentsRef.current = componentGroups
    setComponentHighlight(componentGroups, selectedComponentRef.current)

    const controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.07
    controls.minDistance = 4.7
    controls.maxDistance = 11
    controls.maxPolarAngle = Math.PI * 0.49
    controls.target.set(0, 0.88, 0)
    controls.update()
    controls.saveState()
    controlsRef.current = controls

    const resize = () => {
      const width = mount.clientWidth
      const height = mount.clientHeight
      if (width === 0 || height === 0) {
        return
      }
      camera.aspect = width / height
      camera.updateProjectionMatrix()
      renderer.setSize(width, height)
    }
    const resizeObserver = new ResizeObserver(resize)
    resizeObserver.observe(mount)
    resize()

    let animationFrame = 0
    const animate = () => {
      controls.update()
      renderer.render(scene, camera)
      animationFrame = window.requestAnimationFrame(animate)
    }
    animate()

    const raycaster = new THREE.Raycaster()
    const pointer = new THREE.Vector2()
    let pointerStart: { x: number; y: number } | null = null
    let hoveredComponentId: string | null = null
    const getComponentAtPointer = (event: PointerEvent) => {
      const rect = renderer.domElement.getBoundingClientRect()
      pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
      pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
      raycaster.setFromCamera(pointer, camera)
      const hits = raycaster.intersectObjects(componentGroups, true)
      for (const hit of hits) {
        const componentId = findComponentId(hit.object)
        if (componentId) {
          return componentId
        }
      }
      return null
    }
    const handlePointerDown = (event: PointerEvent) => {
      pointerStart = { x: event.clientX, y: event.clientY }
    }
    const handlePointerMove = (event: PointerEvent) => {
      const nextId = getComponentAtPointer(event)
      if (nextId !== hoveredComponentId) {
        hoveredComponentId = nextId
        renderer.domElement.style.cursor = hoveredComponentId ? 'pointer' : 'grab'
        setComponentHighlight(
          componentGroups,
          selectedComponentRef.current,
          hoveredComponentId,
        )
      }
    }
    const handlePointerUp = (event: PointerEvent) => {
      if (!pointerStart) {
        return
      }

      const movement = Math.hypot(
        event.clientX - pointerStart.x,
        event.clientY - pointerStart.y,
      )
      pointerStart = null
      if (movement > 6) {
        return
      }

      const componentId = getComponentAtPointer(event)
      if (componentId) {
        onSelectComponent(componentId)
      }
    }

    renderer.domElement.addEventListener('pointerdown', handlePointerDown)
    renderer.domElement.addEventListener('pointermove', handlePointerMove)
    renderer.domElement.addEventListener('pointerup', handlePointerUp)

    return () => {
      window.cancelAnimationFrame(animationFrame)
      resizeObserver.disconnect()
      renderer.domElement.removeEventListener('pointerdown', handlePointerDown)
      renderer.domElement.removeEventListener('pointermove', handlePointerMove)
      renderer.domElement.removeEventListener('pointerup', handlePointerUp)
      controls.dispose()
      controlsRef.current = null
      cameraRef.current = null
      componentsRef.current = []
      scene.traverse((object: THREE.Object3D) => {
        if (object instanceof THREE.Mesh || object instanceof THREE.LineSegments) {
          object.geometry.dispose()
          const materials = Array.isArray(object.material)
            ? object.material
            : [object.material]
          for (const material of materials) {
            material.dispose()
          }
        }
      })
      renderer.dispose()
      mount.removeChild(renderer.domElement)
    }
  }, [onSelectComponent, pids, sceneError])

  function zoom(factor: number) {
    const controls = controlsRef.current
    const camera = cameraRef.current
    if (!controls || !camera) {
      return
    }

    const offset = camera.position.clone().sub(controls.target).multiplyScalar(factor)
    camera.position.copy(controls.target.clone().add(offset))
    controls.update()
  }

  return (
    <div className="vehicle-scene-frame">
      <div
        aria-label="Carroceria transparente com componentes 3D interativos; arraste para girar."
        className="vehicle-scene-canvas"
        ref={mountRef}
        role="img"
      />
      {sceneError && (
        <p className="vehicle-scene-error" role="alert">
          {sceneError}
        </p>
      )}
      {!sceneError && (
        <div className="vehicle-scene-controls" aria-label="Controles do modelo 3D">
          <button
            aria-label="Afastar modelo"
            onClick={() => zoom(1.18)}
            type="button"
          >
            −
          </button>
          <button
            aria-label="Aproximar modelo"
            onClick={() => zoom(0.85)}
            type="button"
          >
            +
          </button>
          <button
            className="vehicle-reset-button"
            onClick={() => controlsRef.current?.reset()}
            type="button"
          >
            Restaurar vista
          </button>
        </div>
      )}
      <span className="vehicle-scene-help">Arraste para girar · role para aproximar</span>
    </div>
  )
}
