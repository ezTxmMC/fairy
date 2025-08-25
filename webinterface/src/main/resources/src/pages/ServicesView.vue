<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listServices } from '@/api/client'
import type { Service } from '@/types'
import ServiceStatusChip from '@/components/ServiceStatusChip.vue'

const services = ref<Service[]>([])

onMounted(async () => {
  services.value = await listServices()
})
</script>

<template>
  <div class="panel">
    <div class="panel-header">Services</div>
    <div class="panel-body">
      <table class="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Gruppe</th>
            <th>Template</th>
            <th>Status</th>
            <th>Spieler</th>
            <th>Host</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in services" :key="s.id">
            <td>
              <router-link class="btn" :to="`/services/${s.id}`">{{ s.name }}</router-link>
            </td>
            <td>{{ s.groupName }}</td>
            <td>{{ s.templateName }}</td>
            <td><ServiceStatusChip :status="s.state" /></td>
            <td>{{ s.players }}/{{ s.maxPlayers }}</td>
            <td>{{ s.host }}:{{ s.port }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
