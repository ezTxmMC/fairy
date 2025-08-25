<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getService } from '@/api/client'
import type { Service } from '@/types'
import ServiceStatusChip from '@/components/ServiceStatusChip.vue'
import ServiceConsole from '@/components/ServiceConsole.vue'

const route = useRoute()
const id = ref(String(route.params.id || ''))
const service = ref<Service | null>(null)

async function load() {
  if (!id.value) return
  service.value = await getService(id.value)
}

watch(() => route.params.id, (v) => {
  id.value = String(v || '')
  load()
})

onMounted(load)
</script>

<template>
  <div class="grid cols-2" v-if="service">
    <div class="panel">
      <div class="panel-header">Informationen</div>
      <div class="panel-body">
        <div class="grid cols-2">
          <div>
            <div class="muted">Name</div>
            <div>{{ service.name }}</div>
          </div>
          <div>
            <div class="muted">Status</div>
            <ServiceStatusChip :status="service.state" />
          </div>
          <div>
            <div class="muted">Gruppe</div>
            <div>{{ service.groupName }}</div>
          </div>
          <div>
            <div class="muted">Template</div>
            <div>{{ service.templateName }}</div>
          </div>
          <div>
            <div class="muted">Host</div>
            <div>{{ service.host }}:{{ service.port }}</div>
          </div>
          <div>
            <div class="muted">Spieler</div>
            <div>{{ service.players }}/{{ service.maxPlayers }}</div>
          </div>
          <div>
            <div class="muted">Gestartet</div>
            <div>{{ new Date(service.startedAt).toLocaleString() }}</div>
          </div>
        </div>
      </div>
    </div>

    <ServiceConsole :service-id="id" />
  </div>
  <div v-else class="panel">
    <div class="panel-body">Lade Service-Daten...</div>
  </div>
</template>

<style scoped>
.muted { color: var(--muted); font-size: 12px; }
</style>
