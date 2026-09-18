<!--<template>-->
<!--  <div class="container">-->
<!--    <header class="page-header">-->
<!--      <h1 class="title">短链接管理平台</h1>-->
<!--      <p class="desc">快速生成短链接，支持访问数据统计</p>-->
<!--    </header>-->

<!--    <main class="main-content">-->
<!--      &lt;!&ndash; 生成短链卡片 &ndash;&gt;-->
<!--      <div class="card">-->
<!--        <h2 class="card-title">生成短链接</h2>-->
<!--        <div class="form-row">-->
<!--          <label class="form-label">原始链接</label>-->
<!--          <input-->
<!--              v-model="form.originalUrl"-->
<!--              type="text"-->
<!--              class="form-input"-->
<!--              placeholder="请输入完整链接，例如 https://www.example.com"-->
<!--              :disabled="loading"-->
<!--          />-->
<!--        </div>-->

<!--        <div class="form-row">-->
<!--          <label class="form-label">有效期（小时）</label>-->
<!--          <input-->
<!--              v-model.number="form.expireHours"-->
<!--              type="number"-->
<!--              min="1"-->
<!--              max="720"-->
<!--              class="form-input w-180"-->
<!--              placeholder="单位：小时"-->
<!--              :disabled="loading"-->
<!--          />-->
<!--          <span class="hint">最大支持720小时</span>-->
<!--        </div>-->

<!--        <div class="btn-row">-->
<!--          <button class="btn-primary" @click="handleGenerate" :disabled="loading">-->
<!--            <span v-if="loading">处理中…</span>-->
<!--            <span v-else>生成短链接</span>-->
<!--          </button>-->
<!--        </div>-->

<!--        <div v-if="errorMsg" class="alert-error">{{ errorMsg }}</div>-->

<!--        &lt;!&ndash; 生成结果 &ndash;&gt;-->
<!--        <div v-if="shortLink" class="result-wrap">-->
<!--          <div class="result-title">生成成功</div>-->
<!--          <div class="result-link"><code>{{ shortLink }}</code></div>-->
<!--          <div class="result-op">-->
<!--            <button class="btn-secondary" @click="copyLink">复制链接</button>-->
<!--            <button class="btn-secondary" @click="showStatistics = true">查看统计</button>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 统计弹窗 &ndash;&gt;-->
<!--      <div v-if="showStatistics" class="modal-mask" @click.self="showStatistics = false">-->
<!--        <div class="modal-content">-->
<!--          <div class="modal-header">-->
<!--            <h3>访问数据统计</h3>-->
<!--            <button class="close-btn" @click="showStatistics = false">×</button>-->
<!--          </div>-->

<!--          <div class="modal-body">-->
<!--            &lt;!&ndash; 指标卡片 &ndash;&gt;-->
<!--            <div class="stat-cards">-->
<!--              <div class="stat-card">-->
<!--                <div class="stat-num">{{ statistics.totalPv || 0 }}</div>-->
<!--                <div class="stat-label">总访问量(PV)</div>-->
<!--              </div>-->
<!--              <div class="stat-card">-->
<!--                <div class="stat-num">{{ statistics.totalUv || 0 }}</div>-->
<!--                <div class="stat-label">独立访客(UV)</div>-->
<!--              </div>-->
<!--            </div>-->

<!--            &lt;!&ndash; 时间分布折线图 &ndash;&gt;-->
<!--            <div class="chart-block">-->
<!--              <h4>访问时间分布（近7天）</h4>-->
<!--              <div ref="timeChartRef" class="chart-box"></div>-->
<!--            </div>-->

<!--            &lt;!&ndash; 饼图区域 &ndash;&gt;-->
<!--            <div class="chart-row">-->
<!--              <div class="chart-block half">-->
<!--                <h4>设备分布</h4>-->
<!--                <div ref="deviceChartRef" class="chart-box"></div>-->
<!--              </div>-->
<!--              <div class="chart-block half">-->
<!--                <h4>来源分布</h4>-->
<!--                <div ref="sourceChartRef" class="chart-box"></div>-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </main>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, onMounted, nextTick, watch } from 'vue'-->
<!--import axios from 'axios'-->
<!--import * as echarts from 'echarts'-->

<!--const loading = ref(false)-->
<!--const errorMsg = ref('')-->
<!--const shortLink = ref('')-->
<!--const currentShortCode = ref('')-->
<!--const showStatistics = ref(false)-->
<!--const statistics = ref({})-->

<!--const form = ref({-->
<!--  originalUrl: '',-->
<!--  expireHours: 24-->
<!--})-->

<!--// 图表DOM引用-->
<!--const timeChartRef = ref(null)-->
<!--const deviceChartRef = ref(null)-->
<!--const sourceChartRef = ref(null)-->
<!--let timeChart, deviceChart, sourceChart-->

<!--// 生成短链-->
<!--const handleGenerate = async () => {-->
<!--  errorMsg.value = ''-->
<!--  shortLink.value = ''-->

<!--  if (!form.value.originalUrl) {-->
<!--    errorMsg.value = '请输入原始链接'-->
<!--    return-->
<!--  }-->

<!--  loading.value = true-->
<!--  try {-->
<!--    const resp = await axios.post('/api/generate', {-->
<!--      originalUrl: form.value.originalUrl,-->
<!--      expireHours: form.value.expireHours-->
<!--    })-->
<!--    const code = resp.data.data.shortCode-->
<!--    currentShortCode.value = code-->
<!--    shortLink.value = `http://localhost:8080/${code}`-->
<!--  } catch (err) {-->
<!--    errorMsg.value = '请求后端失败，请确认后端8080服务正常'-->
<!--  } finally {-->
<!--    loading.value = false-->
<!--  }-->
<!--}-->

<!--// 复制链接-->
<!--const copyLink = async () => {-->
<!--  try {-->
<!--    await navigator.clipboard.writeText(shortLink.value)-->
<!--    alert('已复制到剪贴板')-->
<!--  } catch {-->
<!--    alert('复制失败，请手动复制')-->
<!--  }-->
<!--}-->

<!--// 监听弹窗打开，加载统计数据并渲染图表-->
<!--watch(showStatistics, async (val) => {-->
<!--  if (val && currentShortCode.value) {-->
<!--    await loadStatistics()-->
<!--    await nextTick()-->
<!--    initCharts()-->
<!--  }-->
<!--})-->

<!--// 加载统计数据-->
<!--const loadStatistics = async () => {-->
<!--  try {-->
<!--    const res = await axios.get(`/api/statistics/${currentShortCode.value}`)-->
<!--    statistics.value = res.data.data-->
<!--  } catch (e) {-->
<!--    console.error('加载统计失败', e)-->
<!--  }-->
<!--}-->

<!--// 初始化所有图表-->
<!--const initCharts = () => {-->
<!--  // 时间分布折线图-->
<!--  timeChart = echarts.init(timeChartRef.value)-->
<!--  const timeData = statistics.value.timeDistribution || []-->
<!--  timeChart.setOption({-->
<!--    tooltip: { trigger: 'axis' },-->
<!--    xAxis: {-->
<!--      type: 'category',-->
<!--      data: timeData.map(item => item.date)-->
<!--    },-->
<!--    yAxis: { type: 'value' },-->
<!--    series: [{-->
<!--      data: timeData.map(item => item.count),-->
<!--      type: 'line',-->
<!--      smooth: true,-->
<!--      itemStyle: { color: '#2563eb' }-->
<!--    }]-->
<!--  })-->

<!--  // 设备分布饼图-->
<!--  deviceChart = echarts.init(deviceChartRef.value)-->
<!--  const deviceData = statistics.value.deviceDistribution || []-->
<!--  deviceChart.setOption({-->
<!--    tooltip: { trigger: 'item' },-->
<!--    series: [{-->
<!--      type: 'pie',-->
<!--      radius: '60%',-->
<!--      data: deviceData.map(item => ({ name: item.device, value: item.count }))-->
<!--    }]-->
<!--  })-->

<!--  // 来源分布饼图-->
<!--  sourceChart = echarts.init(sourceChartRef.value)-->
<!--  const sourceData = statistics.value.sourceDistribution || []-->
<!--  sourceChart.setOption({-->
<!--    tooltip: { trigger: 'item' },-->
<!--    series: [{-->
<!--      type: 'pie',-->
<!--      radius: '60%',-->
<!--      data: sourceData.map(item => ({ name: item.source, value: item.count }))-->
<!--    }]-->
<!--  })-->

<!--  // 窗口大小变化自适应-->
<!--  window.onresize = () => {-->
<!--    timeChart?.resize()-->
<!--    deviceChart?.resize()-->
<!--    sourceChart?.resize()-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--* { box-sizing: border-box; }-->
<!--.container {-->
<!--  min-height: 100vh;-->
<!--  background-color: #f5f7fa;-->
<!--  padding: 40px 20px;-->
<!--  font-family: "Microsoft YaHei", system-ui, sans-serif;-->
<!--}-->
<!--.page-header {-->
<!--  max-width: 900px;-->
<!--  margin: 0 auto 32px;-->
<!--}-->
<!--.title {-->
<!--  font-size: 28px;-->
<!--  color: #1f2937;-->
<!--  margin: 0 0 8px;-->
<!--  font-weight: 600;-->
<!--}-->
<!--.desc { color: #6b7280; font-size: 15px; margin: 0; }-->
<!--.main-content { max-width: 900px; margin: 0 auto; }-->
<!--.card {-->
<!--  background-color: #fff;-->
<!--  border-radius: 10px;-->
<!--  padding: 32px;-->
<!--  box-shadow: 0 2px 12px rgba(0,0,0,0.07);-->
<!--}-->
<!--.card-title {-->
<!--  font-size: 20px;-->
<!--  color: #1f2937;-->
<!--  margin-top: 0;-->
<!--  margin-bottom: 24px;-->
<!--  font-weight: 600;-->
<!--}-->
<!--.form-row { margin-bottom: 20px; }-->
<!--.form-label {-->
<!--  display: block;-->
<!--  font-size: 14px;-->
<!--  color: #374151;-->
<!--  margin-bottom: 8px;-->
<!--  font-weight: 500;-->
<!--}-->
<!--.form-input {-->
<!--  width: 100%;-->
<!--  height: 42px;-->
<!--  border: 1px solid #d1d5db;-->
<!--  border-radius: 6px;-->
<!--  padding: 0 14px;-->
<!--  font-size: 15px;-->
<!--  outline: none;-->
<!--  transition: border-color 0.2s;-->
<!--}-->
<!--.form-input:focus { border-color: #2563eb; }-->
<!--.form-input:disabled { background-color: #f9fafb; cursor: not-allowed; }-->
<!--.w-180 { width: 180px; }-->
<!--.hint { font-size: 13px; color: #9ca3af; margin-left: 12px; }-->
<!--.btn-row { margin-top: 24px; }-->
<!--.btn-primary {-->
<!--  background-color: #2563eb;-->
<!--  color: #fff;-->
<!--  border: none;-->
<!--  border-radius: 6px;-->
<!--  height: 44px;-->
<!--  padding: 0 28px;-->
<!--  font-size: 15px;-->
<!--  cursor: pointer;-->
<!--  transition: background-color 0.2s;-->
<!--}-->
<!--.btn-primary:hover:not(:disabled) { background-color: #1d4ed8; }-->
<!--.btn-primary:disabled { background-color: #93c5fd; cursor: not-allowed; }-->
<!--.btn-secondary {-->
<!--  background-color: #f3f4f6;-->
<!--  color: #374151;-->
<!--  border: 1px solid #d1d5db;-->
<!--  border-radius: 6px;-->
<!--  padding: 8px 16px;-->
<!--  cursor: pointer;-->
<!--  margin-right: 10px;-->
<!--}-->
<!--.btn-secondary:hover { background-color: #e5e7eb; }-->
<!--.alert-error {-->
<!--  margin-top: 16px;-->
<!--  padding: 12px 14px;-->
<!--  background-color: #fef2f2;-->
<!--  color: #dc2626;-->
<!--  border-radius: 6px;-->
<!--  font-size: 14px;-->
<!--}-->
<!--.result-wrap {-->
<!--  margin-top: 24px;-->
<!--  padding: 20px;-->
<!--  background-color: #f0f7ff;-->
<!--  border-radius: 8px;-->
<!--}-->
<!--.result-title { font-weight: 600; color: #0369a1; margin-bottom: 12px; }-->
<!--.result-link {-->
<!--  background-color: #fff;-->
<!--  padding: 12px;-->
<!--  border-radius: 6px;-->
<!--  border: 1px solid #bfdbfe;-->
<!--}-->
<!--.result-link code { font-size: 15px; color: #1e40af; }-->
<!--.result-op { margin-top: 14px; }-->

<!--/* 弹窗样式 */-->
<!--.modal-mask {-->
<!--  position: fixed;-->
<!--  top: 0; left: 0; right: 0; bottom: 0;-->
<!--  background: rgba(0,0,0,0.5);-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  z-index: 1000;-->
<!--}-->
<!--.modal-content {-->
<!--  background: #fff;-->
<!--  border-radius: 10px;-->
<!--  width: 800px;-->
<!--  max-height: 90vh;-->
<!--  overflow-y: auto;-->
<!--}-->
<!--.modal-header {-->
<!--  padding: 16px 24px;-->
<!--  border-bottom: 1px solid #e5e7eb;-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--}-->
<!--.modal-header h3 { margin: 0; font-size: 18px; }-->
<!--.close-btn {-->
<!--  border: none;-->
<!--  background: none;-->
<!--  font-size: 24px;-->
<!--  cursor: pointer;-->
<!--  color: #6b7280;-->
<!--}-->
<!--.modal-body { padding: 24px; }-->
<!--.stat-cards {-->
<!--  display: flex;-->
<!--  gap: 20px;-->
<!--  margin-bottom: 24px;-->
<!--}-->
<!--.stat-card {-->
<!--  flex: 1;-->
<!--  padding: 20px;-->
<!--  background: #f8fafc;-->
<!--  border-radius: 8px;-->
<!--  text-align: center;-->
<!--}-->
<!--.stat-num { font-size: 28px; font-weight: 600; color: #2563eb; }-->
<!--.stat-label { font-size: 14px; color: #6b7280; margin-top: 4px; }-->
<!--.chart-block { margin-bottom: 24px; }-->
<!--.chart-block h4 { margin: 0 0 12px; font-size: 15px; color: #374151; }-->
<!--.chart-box { height: 280px; width: 100%; }-->
<!--.chart-row { display: flex; gap: 20px; }-->
<!--.chart-row .half { flex: 1; }-->
<!--</style>-->



<template>
  <div class="home-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>短链接管理平台</h1>
      <p>快速生成短链接，支持访问数据统计</p>
    </div>

    <!-- 生成短链接卡片 -->
    <div class="card generate-card">
      <h2>生成短链接</h2>

      <div class="form-item">
        <label>原始链接</label>
        <el-input v-model="originalUrl" placeholder="请输入原始链接" />
      </div>

      <div class="form-item">
        <label>有效期（小时）</label>
        <el-input v-model="expireHours" type="number" placeholder="1" />
        <span class="tip">最大支持720小时</span>
      </div>

      <el-button type="primary" class="generate-btn" :loading="generating" @click="generateShortUrl">
        生成短链接
      </el-button>

      <!-- 生成成功结果：展示+复制 -->
      <div class="result-box" v-if="shortUrlResult">
        <div class="result-title">生成成功</div>

        <!-- 短链展示：只读 + 可点击跳转 -->
        <el-input
            v-model="shortUrlResult"
            readonly
            class="short-url-input"
            @click="openShortUrl"
            placeholder="生成的短链接"
        />

        <div class="result-btns">
          <el-button size="small" type="primary" plain @click="copyUrl">
            复制链接
          </el-button>
          <el-button size="small" @click="viewStats">查看统计</el-button>
        </div>
      </div>
    </div>

    <!-- 访问统计查询卡片 -->
    <div class="card stats-card" id="statsSection">
      <h2>访问统计查询</h2>
      <div class="query-bar">
        <el-input
            v-model="queryShortCode"
            placeholder="请输入短链编码（如 uK4hK2）"
            clearable
            style="width: 320px"
            @keyup.enter="queryStats"
        />
        <el-button type="primary" :loading="statsLoading" style="margin-left: 12px" @click="queryStats">
          查询统计
        </el-button>
        <el-button :loading="cleaning" type="danger" plain style="margin-left: 12px" @click="cleanExpired">
          清理过期链接
        </el-button>
      </div>

      <!-- 统计总览 -->
      <div class="overview-row" v-if="statsData">
        <div class="overview-item">
          <span class="label">总点击量 (PV)</span>
          <span class="value">{{ statsData.totalPv || 0 }}</span>
        </div>
        <div class="overview-item">
          <span class="label">总访客数 (UV)</span>
          <span class="value">{{ statsData.totalUv || 0 }}</span>
        </div>
      </div>

      <!-- 时间趋势图 -->
      <div class="chart-card" v-if="statsData">
        <h3>访问量日趋势</h3>
        <div ref="trendChartRef" class="chart-box"></div>
        <el-empty v-if="!statsData.timeDistribution || statsData.timeDistribution.length === 0" description="暂无时间数据" />
      </div>

      <!-- 来源+设备分布 -->
      <div class="chart-row" v-if="statsData">
        <div class="chart-card half">
          <h3>访问来源分布</h3>
          <div ref="sourceChartRef" class="chart-box"></div>
          <el-empty v-if="!statsData.sourceDistribution || statsData.sourceDistribution.length === 0" description="暂无来源数据" />
        </div>
        <div class="chart-card half">
          <h3>访问设备分布</h3>
          <div ref="deviceChartRef" class="chart-box"></div>
          <el-empty v-if="!statsData.deviceDistribution || statsData.deviceDistribution.length === 0" description="暂无设备数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

// ========== 生成短链相关 ==========
const originalUrl = ref('请输入链接')
const expireHours = ref(1)
const generating = ref(false)
const shortUrlResult = ref('')

// 生成短链接
const generateShortUrl = async () => {
  if (!originalUrl.value.trim()) {
    return ElMessage.warning('请输入原始链接')
  }
  generating.value = true
  try {
    const res = await axios.post('/api/shortUrl/generate', {
      originalUrl: originalUrl.value,
      expireHours: expireHours.value
    })
    const data = res.data?.data
    const code = data?.shortCode
    if (!code) {
      throw new Error('未获取到短码')
    }
    // 后端返回的是 ShortUrl 实体，需拼接短链地址
    shortUrlResult.value = `http://localhost:8080/${code}`
    ElMessage.success('生成成功')
  } catch (err) {
    console.error('生成短链失败', err)
    ElMessage.error(err.response?.data?.msg || err.message || '生成失败，请重试')
  } finally {
    generating.value = false
  }
}

// 一键复制短链接
const copyUrl = async () => {
  try {
    // 优先使用现代剪贴板 API
    await navigator.clipboard.writeText(shortUrlResult.value)
    ElMessage.success('短链接已复制到剪贴板')
  } catch (err) {
    // 降级兼容方案
    const inputDom = document.querySelector('.short-url-input input')
    inputDom.select()
    document.execCommand('copy')
    ElMessage.success('短链接已复制')
  }
}

// 点击短链新窗口打开测试
const openShortUrl = () => {
  window.open(shortUrlResult.value, '_blank')
}

// 从短链接中提取短码
const extractShortCode = (url) => {
  return url.split('/').pop()
}

// 点击查看统计
const viewStats = () => {
  const code = extractShortCode(shortUrlResult.value)
  queryShortCode.value = code
  queryStats()
  document.getElementById('statsSection').scrollIntoView({ behavior: 'smooth' })
}

// ========== 统计查询相关 ==========
const queryShortCode = ref('')
const statsLoading = ref(false)
const cleaning = ref(false)
const statsData = ref(null)

const trendChartRef = ref(null)
const sourceChartRef = ref(null)
const deviceChartRef = ref(null)

let trendChart = null
let sourceChart = null
let deviceChart = null

// 手动清理过期短链
const cleanExpired = async () => {
  try {
    cleaning.value = true
    const res = await axios.delete('/api/expired')
    const count = res.data?.data ?? 0
    ElMessage.success(`已清理 ${count} 条过期短链`)
  } catch (err) {
    console.error('清理过期短链失败', err)
    ElMessage.error(err.response?.data?.msg || '清理失败，请重试')
  } finally {
    cleaning.value = false
  }
}

const queryStats = async () => {
  if (!queryShortCode.value.trim()) {
    return ElMessage.warning('请输入短链编码')
  }
  statsLoading.value = true
  try {
    const res = await axios.get(`/api/shortUrl/stats/${queryShortCode.value.trim()}`)
    statsData.value = res.data.data
    await nextTick()
    renderAllCharts()
    ElMessage.success('查询成功')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '查询失败，请检查短链编码')
    statsData.value = null
  } finally {
    statsLoading.value = false
  }
}

const renderAllCharts = () => {
  renderTrendChart()
  renderSourceChart()
  renderDeviceChart()
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const dates = statsData.value.timeDistribution.map(item => item.date)
  const pvs = statsData.value.timeDistribution.map(item => item.pv)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [{
      data: pvs,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const renderSourceChart = () => {
  if (!sourceChartRef.value) return
  if (!sourceChart) sourceChart = echarts.init(sourceChartRef.value)

  const data = statsData.value.sourceDistribution.map(item => ({
    name: item.referer,
    value: item.pv
  }))

  sourceChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data
    }]
  })
}

const renderDeviceChart = () => {
  if (!deviceChartRef.value) return
  if (!deviceChart) deviceChart = echarts.init(deviceChartRef.value)

  const data = statsData.value.deviceDistribution.map(item => ({
    name: item.device,
    value: item.pv
  }))

  deviceChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data
    }]
  })
}

const resizeCharts = () => {
  trendChart?.resize()
  sourceChart?.resize()
  deviceChart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  sourceChart?.dispose()
  deviceChart?.dispose()
})
</script>

<style scoped>
.home-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #1f2329;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #86909c;
  margin: 0;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card h2 {
  font-size: 18px;
  color: #1f2329;
  margin: 0 0 20px 0;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  font-size: 14px;
  color: #4e5969;
  margin-bottom: 8px;
}

.tip {
  font-size: 12px;
  color: #86909c;
  margin-left: 8px;
}

.generate-btn {
  width: 100%;
  margin-top: 8px;
}

.result-box {
  margin-top: 20px;
  padding: 16px;
  background: #f2f7ff;
  border-radius: 6px;
}

.result-title {
  color: #007d9c;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.short-url-input {
  margin-bottom: 12px;
  cursor: pointer;
}

.short-url-input :deep(.el-input__inner) {
  color: #1677ff;
}

.result-btns {
  display: flex;
  gap: 12px;
}

/* 统计区域样式 */
.query-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.overview-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.overview-item {
  flex: 1;
  padding: 20px;
  background: #f7f8fa;
  border-radius: 6px;
  text-align: center;
}

.overview-item .label {
  display: block;
  font-size: 14px;
  color: #86909c;
  margin-bottom: 8px;
}

.overview-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2329;
}

.chart-card {
  background: #f7f8fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.chart-card h3 {
  font-size: 15px;
  color: #1f2329;
  margin: 0 0 12px 0;
}

.chart-box {
  width: 100%;
  height: 300px;
}

.chart-row {
  display: flex;
  gap: 20px;
}

.chart-card.half {
  flex: 1;
  margin-bottom: 0;
}
</style>
