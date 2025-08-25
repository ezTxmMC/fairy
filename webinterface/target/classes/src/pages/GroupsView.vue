<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listGroups } from '@/api/client'
import type { Group } from '@/types'

const groups = ref<Group[]>([])

onMounted(async () => {
  groups.value = await listGroups()
})

function scaleUp(g: Group) {
  // Placeholder: hier könnte ein API-Call erfolgen
  alert(`Skaliere ${g.name} hoch (Mock)`)
}
function scaleDown(g: Group) {
  alert(`Skaliere ${g.name} herunter (Mock)`)
}
</script>

<template>
  <div class="panel">
    <div class="panel-header">Gruppen</div>
    <div class="panel-body">
      <table class="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Typ</th>
            <th>Min/Max</th>
            <th>Templates</th>
            <th>Autoscale</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="g in groups" :key="g.name">
            <td>{{ g.name }}</td>
            <td>{{ g.type }}</td>
            <td>{{ g.minServices }}/{{ g.maxServices }}</td>
            <td>{{ g.templates.join(', ') }}</td>
            <td>{{ g.autoScale ? 'Ja' : 'Nein' }}</td>
            <td>
              <button class="btn" @click="scaleDown(g)">-</button>
              <button class="btn" @click="scaleUp(g)">+</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
