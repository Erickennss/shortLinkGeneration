<template>
  <div class="container">
    <!-- 头部标题栏 -->
    <header class="page-header">
      <h1 class="title">短链接管理平台</h1>
      <p class="desc">快速生成短链接，便于分享与传播</p>
    </header>

    <main class="main-content">
      <!-- 生成短链卡片 -->
      <div class="card">
        <h2 class="card-title">生成短链接</h2>
        <div class="form-row">
          <label class="form-label">原始链接</label>
          <input
              v-model="form.originalUrl"
              type="text"
              class="form-input"
              placeholder="请输入完整链接，例如 https://www.example.com"
              :disabled="loading"
          />
        </div>

        <div class="form-row">
          <label class="form-label">有效期（小时）</label>
          <input
              v-model.number="form.expireHours"
              type="number"
              min="1"
              max="720"
              class="form-input w-180"
              placeholder="单位：小时"
              :disabled="loading"
          />
          <span class="hint">最大支持720小时</span>
        </div>

        <div class="btn-row">
          <button class="btn-primary" @click="handleGenerate" :disabled="loading">
            <span v-if="loading">处理中…</span>
            <span v-else>生成短链接</span>
          </button>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="alert-error">
          {{ errorMsg }}
        </div>

        <!-- 生成结果区域 -->
        <div v-if="shortLink" class="result-wrap">
          <div class="result-title">生成成功</div>
          <div class="result-link">
            <code>{{ shortLink }}</code>
          </div>
          <div class="result-op">
            <button class="btn-secondary" @click="copyLink">复制链接</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const loading = ref(false)
const errorMsg = ref('')
const shortLink = ref('')

const form = ref({
  originalUrl: '',
  expireHours: 24
})

const handleGenerate = async () => {
  errorMsg.value = ''
  shortLink.value = ''

  if (!form.value.originalUrl) {
    errorMsg.value = '请输入原始链接'
    return
  }

  loading.value = true
  try {
    const resp = await axios.post('/api/generate', {
      originalUrl: form.value.originalUrl,
      expireHours: form.value.expireHours
    })
    const code = resp.data.data.shortCode
    shortLink.value = `http://localhost:8080/${code}`
  } catch (err) {
    console.error(err)
    errorMsg.value = '请求后端失败，请确认后端8080服务正常'
  } finally {
    loading.value = false
  }
}

// 复制到剪贴板
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shortLink.value)
    alert('已复制剪贴板')
  } catch {
    alert('复制失败，请手动复制')
  }
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 20px;
  font-family: "Microsoft YaHei", system-ui, sans-serif;
}
.page-header {
  max-width: 720px;
  margin: 0 auto 32px;
}
.title {
  font-size: 28px;
  color: #1f2937;
  margin: 0 0 8px;
  font-weight: 600;
}
.desc {
  color: #6b7280;
  font-size: 15px;
  margin: 0;
}
.main-content {
  max-width: 720px;
  margin: 0 auto;
}
.card {
  background-color: #ffffff;
  border-radius: 10px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
}
.card-title {
  font-size: 20px;
  color: #1f2937;
  margin-top: 0;
  margin-bottom: 24px;
  font-weight: 600;
}
.form-row {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
  font-weight: 500;
}
.form-input {
  width: 100%;
  height: 42px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 14px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}
.form-input:focus {
  border-color: #2563eb;
}
.form-input:disabled {
  background-color: #f9fafb;
  cursor: not-allowed;
}
.w-180 {
  width: 180px;
}
.hint {
  font-size:13px;
  color:#9ca3af;
  margin-left:12px;
}
.btn-row {
  margin-top:24px;
}
.btn-primary {
  background-color: #2563eb;
  color:#fff;
  border: none;
  border-radius:6px;
  height:44px;
  padding: 0 28px;
  font-size:15px;
  cursor:pointer;
  transition: background-color 0.2s;
}
.btn-primary:hover:not(:disabled) {
  background-color:#1d4ed8;
}
.btn-primary:disabled {
  background-color:#93c5fd;
  cursor:not-allowed;
}
.btn-secondary {
  background-color:#f3f4f6;
  color:#374151;
  border:1px solid #d1d5db;
  border-radius:6px;
  padding: 8px 16px;
  cursor:pointer;
}
.btn-secondary:hover {
  background-color:#e5e7eb;
}
.alert-error {
  margin-top:16px;
  padding:12px 14px;
  background-color:#fef2f2;
  color:#dc2626;
  border-radius:6px;
  font-size:14px;
}
.result-wrap {
  margin-top:24px;
  padding:20px;
  background-color:#f0f7ff;
  border-radius:8px;
}
.result-title {
  font-weight: 600;
  color:#0369a1;
  margin-bottom:12px;
}
.result-link {
  background-color:#ffffff;
  padding:12px;
  border-radius:6px;
  border:1px solid #bfdbfe;
}
.result-link code {
  font-size:15px;
  color:#1e40af;
}
.result-op {
  margin-top:14px;
}
</style>





