<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listTemplates } from '@/api/client'
import type { Template } from '@/types'

const templates = ref<Template[]>([])

onMounted(async () => {
  templates.value = await listTemplates()
})
</script>

<template>
  <div class="panel">
    <div class="panel-header">Templates</div>
    <div class="panel-body">
      <table class="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Typ</th>
            <th>RAM</th>
            <th>Disk</th>
            <th>Java</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in templates" :key="t.id">
            <td>{{ t.name }}</td>
            <td>{{ t.type }}</td>
            <td>{{ t.ramMB }} MB</td>
            <td>{{ t.diskMB }} MB</td>
            <td>{{ t.javaVersion }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
