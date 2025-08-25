import type { Group, Service, Stats, Template } from '@/types'

const API_BASE = import.meta.env.VITE_API_BASE as string | undefined

async function safeFetch<T>(path: string, fallback: () => T | Promise<T>): Promise<T> {
  if (!API_BASE) return await fallback()
  try {
    const res = await fetch(API_BASE + path)
    if (!res.ok) throw new Error(String(res.status))
    return (await res.json()) as T
  } catch {
    return await fallback()
  }
}

function randomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

const mock = {
  groups(): Group[] {
    return [
      { name: 'Lobby', type: 'LOBBY', minServices: 2, maxServices: 6, templates: ['lobby-1'], autoScale: true },
      { name: 'CityBuild', type: 'GAME', minServices: 1, maxServices: 3, templates: ['cb-1', 'cb-2'], autoScale: true },
      { name: 'Bungee', type: 'PROXY', minServices: 1, maxServices: 2, templates: ['proxy'], autoScale: false },
    ]
  },
  templates(): Template[] {
    return [
      { id: 'lobby-1', name: 'Lobby Default', type: 'LOBBY', ramMB: 1024, diskMB: 512, javaVersion: '21' },
      { id: 'cb-1', name: 'CityBuild A', type: 'GAME', ramMB: 2048, diskMB: 2048, javaVersion: '21' },
      { id: 'cb-2', name: 'CityBuild B', type: 'GAME', ramMB: 3072, diskMB: 4096, javaVersion: '21' },
      { id: 'proxy', name: 'Proxy', type: 'PROXY', ramMB: 512, diskMB: 128, javaVersion: '21' },
    ]
  },
  services(): Service[] {
    const states: Service['state'][] = ['STARTING', 'RUNNING', 'STOPPED', 'CRASHED']
    const groups = mock.groups()
    const templates = mock.templates()
    const arr: Service[] = []
    let id = 1
    for (const g of groups) {
      const count = randomInt(g.minServices, Math.max(g.minServices, g.maxServices))
      for (let i = 0; i < count; i++) {
        const t = templates.find((x) => x.id === g.templates[i % g.templates.length]) || templates[0]
        const state = states[randomInt(0, 3)]
        arr.push({
          id: String(id++),
          name: `${g.name}-${100 + i}`,
          groupName: g.name,
          templateName: t.name,
          state,
          players: state === 'RUNNING' ? randomInt(0, 30) : 0,
          maxPlayers: 100,
          host: '127.0.0.1',
          port: 20000 + id,
          startedAt: new Date(Date.now() - randomInt(10_000, 3_000_000)).toISOString(),
        })
      }
    }
    return arr
  },
}

export async function getStats(): Promise<Stats> {
  return safeFetch<Stats>('/stats', () => {
    const services = mock.services()
    return {
      totalGroups: mock.groups().length,
      totalTemplates: mock.templates().length,
      totalServices: services.length,
      onlineServices: services.filter((s) => s.state === 'RUNNING').length,
      playersOnline: services.reduce((a, b) => a + b.players, 0),
    }
  })
}

export async function listGroups(): Promise<Group[]> {
  return safeFetch<Group[]>('/groups', () => mock.groups())
}

export async function listTemplates(): Promise<Template[]> {
  return safeFetch<Template[]>('/templates', () => mock.templates())
}

export async function listServices(): Promise<Service[]> {
  return safeFetch<Service[]>('/services', () => mock.services())
}

export async function getService(id: string): Promise<Service | null> {
  return safeFetch<Service | null>(`/services/${encodeURIComponent(id)}`, () => {
    const all = mock.services()
    return all.find((s) => s.id === id) || all[0] || null
  })
}
