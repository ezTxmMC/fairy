<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { useServiceConsole } from '@/composables/useServiceConsole'

const props = defineProps<{ serviceId: string }>()
const { logs, connected, error, connect, disconnect, send } = useServiceConsole(props.serviceId)
const input = ref('')
const box = ref<HTMLDivElement | null>(null)

function submit() {
  const cmd = input.value.trim()
  if (cmd.length) {
    send(cmd)
    input.value = ''
  }
}

watch(logs, async () => {
  await nextTick()
  if (box.value) {
    box.value.scrollTop = box.value.scrollHeight
  }
})

onMounted(() => {
  connect()
})
</script>

<template>
  <div class="panel">
    <div class="panel-header">Konsole</div>
    <div class="panel-body">
      <div class="console" ref="box">
        <pre class="mono">
<span v-for="(l, i) in logs" :key="i">{{ l }}<br/></span>
        </pre>
      </div>
      <div class="controls">
        <input
          v-model="input"
          class="input mono"
          placeholder="Befehl eingeben und Enter drücken..."
          @keyup.enter="submit"
        />
        <button class="btn" :class="{ danger: connected }" @click="connected ? disconnect() : connect()">
          {{ connected ? 'Trennen' : 'Verbinden' }}
        </button>
      </div>
      <p v-if="error" style="color: var(--danger); margin-top: 8px;">{{ error }}</p>
    </div>
  </div>
</template>

<style scoped>
.console {
  height: 320px;
  overflow: auto;
  background: #0a111b;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px;
}
pre {
  margin: 0;
  line-height: 1.4;
  font-size: 13px;
  white-space: pre-wrap;
}
.controls {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}
.controls .input {
  flex: 1 1 auto;
}
</style>
