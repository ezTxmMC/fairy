<script setup lang="ts">
import SidebarNav from '@/components/SidebarNav.vue'
import TopBar from '@/components/TopBar.vue'
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <SidebarNav />
    </aside>
    <main class="main">
      <TopBar />
      <section class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
}
.sidebar {
  background: var(--panel);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}
.main {
  display: flex;
  flex-direction: column;
}
.content {
  padding: 20px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    position: sticky;
    top: 0;
    z-index: 10;
  }
}
</style>
