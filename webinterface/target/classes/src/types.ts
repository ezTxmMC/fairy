export type GroupType = 'LOBBY' | 'GAME' | 'PROXY' | 'SERVICE'

export interface Group {
  name: string
  type: GroupType
  minServices: number
  maxServices: number
  templates: string[]
  autoScale: boolean
}

export interface Template {
  id: string
  name: string
  type: GroupType
  ramMB: number
  diskMB: number
  javaVersion: string
}

export type ServiceState = 'STARTING' | 'RUNNING' | 'STOPPED' | 'CRASHED'

export interface Service {
  id: string
  name: string
  groupName: string
  templateName: string
  state: ServiceState
  players: number
  maxPlayers: number
  host: string
  port: number
  startedAt: string
}

export interface Stats {
  totalGroups: number
  totalTemplates: number
  totalServices: number
  onlineServices: number
  playersOnline: number
}
