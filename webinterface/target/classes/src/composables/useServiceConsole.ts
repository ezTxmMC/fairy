import { onBeforeUnmount, ref } from 'vue'

export function useServiceConsole(serviceId: string) {
  const logs = ref<string[]>([])
  const connected = ref(false)
  const error = ref<string | null>(null)

  let timer: number | null = null
  let ws: WebSocket | null = null

  const API_BASE = import.meta.env.VITE_API_BASE as string | undefined
  const useWs = !!API_BASE

  function append(line: string) {
    const ts = new Date().toISOString().split('T')[1].replace('Z', '')
    logs.value.push(`[${ts}] ${line}`)
    if (logs.value.length > 5000) logs.value.shift()
  }

  function connect() {
    if (connected.value) return
    error.value = null

    if (useWs) {
      try {
        const wsUrl = API_BASE!.replace(/^http/i, 'ws') + `/ws/services/${encodeURIComponent(serviceId)}/console`
        ws = new WebSocket(wsUrl)
        ws.onopen = () => {
          connected.value = true
          append('Console verbunden.')
        }
        ws.onmessage = (ev) => append(String(ev.data))
        ws.onerror = () => {
          error.value = 'WebSocket-Fehler'
        }
        ws.onclose = () => {
          connected.value = false
          append('Console getrennt.')
        }
      } catch (e) {
        error.value = 'WebSocket konnte nicht initialisiert werden. Fallback auf Mock.'
        startMock()
      }
      return
    }
    startMock()
  }

  function startMock() {
    if (timer) return
    connected.value = true
    append('Mock-Konsole verbunden. (Setze VITE_API_BASE für echte Verbindung)')
    const samples = [
      'INFO Bootstrapping service...',
      'INFO Loading plugins...',
      'INFO Done! For help, type "help".',
      'WARN TPS dropped to 18.7',
      'INFO Player Steve joined the game',
      'INFO Saving world...',
    ]
    timer = window.setInterval(() => {
      append(samples[Math.floor(Math.random() * samples.length)])
    }, 900)
  }

  function disconnect() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.close()
    }
    connected.value = false
  }

  function send(cmd: string) {
    if (!cmd) return
    if (ws && connected.value && ws.readyState === WebSocket.OPEN) {
      ws.send(cmd)
    } else {
      append(`> ${cmd}`)
      if (cmd.toLowerCase() === 'help') {
        append('Available: stop, list, tps, say <msg>')
      }
    }
  }

  onBeforeUnmount(disconnect)

  return { logs, connected, error, connect, disconnect, send }
}
