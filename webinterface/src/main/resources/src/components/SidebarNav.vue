<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
const links = [
  { to: '/', label: 'Dashboard', icon: '🏠' },
  { to: '/groups', label: 'Gruppen', icon: '👥' },
  { to: '/templates', label: 'Templates', icon: '🧩' },
  { to: '/services', label: 'Services', icon: '🖥️' },
]
</script>

<template>
  <div class="nav">
    <div class="brand">
      <div class="logo">☁️</div>
      <div class="brand-text">
        <div class="title">Fairy Cloud</div>
        <div class="subtitle">Webinterface</div>
      </div>
    </div>

    <nav class="menu">
      <router-link
        v-for="l in links"
        :key="l.to"
        :to="l.to"
        class="item"
        :class="{ active: route.path === l.to }"
      >
        <span class="icon">{{ l.icon }}</span>
        <span class="label">{{ l.label }}</span>
      </router-link>
    </nav>

    <div class="footer">
      <a href="https://vuejs.org" target="_blank" rel="noreferrer" class="muted">Made with Vue 3</a>
    </div>
  </div>
</template>

<style scoped>
.nav {
  padding: 18px;
  display: flex;
  gap: 18px;
  flex-direction: column;
  height: 100%;
}
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02));
  border: 1px solid var(--line);
}
.logo {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  border-radius: 10px;
  color: #020617;
  box-shadow: var(--shadow);
}
.title {
  font-weight: 800;
  letter-spacing: 0.2px;
}
.subtitle {
  color: var(--muted);
  font-size: 12px;
}
.menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--text);
  border: 1px solid transparent;
  transition: 0.15s ease;
}
.item:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--line);
}
.item.active {
  background: linear-gradient(180deg, #0e1626, #0b1120);
  border-color: rgba(255, 255, 255, 0.12);
}
.icon {
  width: 22px;
  text-align: center;
}
.footer {
  margin-top: auto;
  padding: 8px 10px;
  color: var(--muted);
  font-size: 12px;
}
.muted {
  color: var(--muted);
}
</style>
