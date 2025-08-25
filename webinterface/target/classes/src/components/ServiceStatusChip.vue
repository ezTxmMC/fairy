<script setup lang="ts">
const props = defineProps<{
  status: 'STARTING' | 'RUNNING' | 'STOPPED' | 'CRASHED'
}>()

const color = {
  STARTING: 'var(--warn)',
  RUNNING: 'var(--success)',
  STOPPED: 'var(--muted)',
  CRASHED: 'var(--danger)',
}[props.status]
</script>

<template>
  <span class="chip" :style="{ borderColor: color, color }">
    <span class="dot" :style="{ background: color }"></span>
    {{ status }}
  </span>
</template>

<style scoped>
.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}
</style>
