import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import axios from 'axios'

const app = createApp(App)
app.use(ElementPlus)

// axios实例
const api = axios.create({
    baseURL: 'http://127.0.0.1:8080'
})
app.config.globalProperties.$api = api

app.mount('#app')
