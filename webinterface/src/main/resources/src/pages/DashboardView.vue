<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getStats, listServices } from '@/api/client'
import ServiceStatusChip from '@/components/ServiceStatusChip.vue'
import type { Service, Stats } from '@/types'

const stats = ref<Stats | null>(null)
const recent = ref<Service[]>([])

onMounted(async () => {
  stats.value = await getStats()
  const services = await listServices()
  recent.value = services.slice(0, 8)
})
</script>

<template>
  <div class="grid cols-4">
    <div class="panel" v-for="card in 4" :key="card">
      <div class="panel-body">
        <template v-if="stats">
          <div v-if="card === 1">
            <div class="muted">Gruppen</div>
            <div class="big">{{ stats.totalGroups }}</div>
          </div>
          <div v-else-if="card === 2">
            <div class="muted">Templates</div>
            <div class="big">{{ stats.totalTemplates }}</div>
          </div>
          <div v-else-if="card === 3">
            <div class="muted">Services</div>
            <div class="big">{{ stats.totalServices }}</div>
          </div>
          <div v-else>
            <div class="muted">Online</div>
            <div class="big">
              {{ stats.onlineServices }}
              <span class="players">Players: {{ stats.playersOnline }}</span>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="skeleton" />
        </template>
      </div>
    </div>
  </div>

  <div class="panel" style="margin-top: 16px;">
    <div class="panel-header">Letzte Services</div>
    <div class="panel-body">
      <table class="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Gruppe</th>
            <th>Template</th>
            <th>Status</th>
            <th>Spieler</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in recent" :key="s.id">
            <td>
              <router-link class="btn" :to="`/services/${s.id}`">{{ s.name }}</router-link>
            </td>
            <td>{{ s.groupName }}</td>
            <td>{{ s.templateName }}</td>
            <td><ServiceStatusChip :status="s.state" /></td>
            <td>{{ s.players }}/{{ s.maxPlayers }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.muted { color: var(--muted); }
.big { font-size: 28px; font-weight: 800; }
.players { font-size: 12px; color: var(--muted); margin-left: 10px; }
.skeleton {
  height: 44px;
  background: linear-gradient(90deg, rgba(255,255,255,0.04), rgba(255,255,255,0.08), rgba(255,255,255,0.04));
  border-radius: 8px;
}
</style>
