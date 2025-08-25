<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <header class="topbar panel">
    <div class="panel-body bar">
      <div class="left">
        <div class="title">
          {{ (route.meta?.title as string) || 'Webinterface' }}
        </div>
        <div class="hint">Verwalte deine Cloud: Gruppen, Templates, Services</div>
      </div>
      <div class="right">
        <router-link to="/services" class="btn primary">Alle Services</router-link>
        <router-link to="/groups" class="btn">Gruppen</router-link>
      </div>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 5;
  border-radius: 0;
  border-left: none;
  border-right: none;
}
.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 800;
  letter-spacing: 0.3px;
  font-size: 18px;
}
.hint {
  color: var(--muted);
  font-size: 12px;
}
.right {
  display: flex;
  gap: 10px;
}
</style>
