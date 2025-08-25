import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

app.use(router)

router.afterEach((to) => {
  const base = 'Fairy Cloud'
  document.title = to.meta?.title ? `${to.meta.title as string} | ${base}` : `${base} | Webinterface`
})

app.mount('#app')
