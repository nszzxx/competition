// 性能检测工具函数

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间(毫秒)
 * @param {boolean} immediate 是否立即执行
 */
export function debounce(func, wait, immediate = false) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func.apply(this, args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func.apply(this, args)
  }
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} limit 时间间隔(毫秒)
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 检测设备性能信息
 */
export function detectPerformance() {
  const performance = {
    // CPU 核心数
    cpuCores: navigator.hardwareConcurrency || 4,
    
    // 设备内存 (GB)
    deviceMemory: navigator.deviceMemory || 4,
    
    // 网络连接类型
    connectionType: getConnectionType(),
    
    // 设备类型
    deviceType: getDeviceType(),
    
    // 浏览器性能等级
    performanceLevel: getPerformanceLevel()
  }
  
  return performance
}

/**
 * 获取网络连接类型
 */
function getConnectionType() {
  if (!navigator.connection) {
    return 'unknown'
  }
  
  const connection = navigator.connection
  const effectiveType = connection.effectiveType
  
  switch (effectiveType) {
    case 'slow-2g':
      return '2G慢'
    case '2g':
      return '2G'
    case '3g':
      return '3G'
    case '4g':
      return '4G'
    default:
      return connection.type || 'unknown'
  }
}

/**
 * 获取设备类型
 */
function getDeviceType() {
  const userAgent = navigator.userAgent.toLowerCase()
  
  if (/mobile|android|iphone|ipad|phone/i.test(userAgent)) {
    return 'mobile'
  }
  
  if (/tablet|ipad/i.test(userAgent)) {
    return 'tablet'
  }
  
  return 'desktop'
}

/**
 * 评估设备性能等级
 */
function getPerformanceLevel() {
  const cores = navigator.hardwareConcurrency || 4
  const memory = navigator.deviceMemory || 4
  
  // 基于 CPU 核心数和内存计算性能分数
  let score = 0
  
  // CPU 分数 (0-40分)
  if (cores >= 8) score += 40
  else if (cores >= 4) score += 30
  else if (cores >= 2) score += 20
  else score += 10
  
  // 内存分数 (0-40分)
  if (memory >= 8) score += 40
  else if (memory >= 4) score += 30
  else if (memory >= 2) score += 20
  else score += 10
  
  // 网络分数 (0-20分)
  const connection = navigator.connection
  if (connection) {
    const effectiveType = connection.effectiveType
    if (effectiveType === '4g') score += 20
    else if (effectiveType === '3g') score += 15
    else if (effectiveType === '2g') score += 10
    else score += 5
  } else {
    score += 15 // 默认分数
  }
  
  // 根据分数确定性能等级
  if (score >= 80) return 'high'
  else if (score >= 60) return 'medium'
  else return 'low'
}

/**
 * 监控页面性能指标
 */
export function monitorPagePerformance() {
  const metrics = {}
  
  // 页面加载时间
  if (performance.timing) {
    const timing = performance.timing
    metrics.loadTime = timing.loadEventEnd - timing.navigationStart
    metrics.domReady = timing.domContentLoadedEventEnd - timing.navigationStart
    metrics.firstPaint = timing.responseStart - timing.navigationStart
  }
  
  // 获取 FCP (First Contentful Paint)
  if ('PerformanceObserver' in window) {
    try {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        entries.forEach(entry => {
          if (entry.name === 'first-contentful-paint') {
            metrics.fcp = entry.startTime
          }
        })
      })
      observer.observe({ entryTypes: ['paint'] })
    } catch (e) {
      console.warn('PerformanceObserver not supported:', e)
    }
  }
  
  // 内存使用情况
  if (performance.memory) {
    metrics.memoryUsage = {
      used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024),
      total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024),
      limit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024)
    }
  }
  
  return metrics
}

/**
 * 检测是否为低性能设备
 */
export function isLowPerformanceDevice() {
  const level = getPerformanceLevel()
  return level === 'low'
}

/**
 * 获取优化建议
 */
export function getOptimizationSuggestions() {
  const performance = detectPerformance()
  const suggestions = []
  
  if (performance.performanceLevel === 'low') {
    suggestions.push('建议关闭动画效果以提升性能')
    suggestions.push('建议减少同时加载的内容')
  }
  
  if (performance.connectionType.includes('2G') || performance.connectionType.includes('3G')) {
    suggestions.push('检测到较慢的网络连接，建议启用数据节省模式')
    suggestions.push('建议延迟加载非关键资源')
  }
  
  if (performance.deviceType === 'mobile') {
    suggestions.push('移动设备检测：建议使用触摸友好的界面')
    suggestions.push('建议启用移动端优化')
  }
  
  if (performance.cpuCores <= 2) {
    suggestions.push('CPU核心数较少，建议减少并发处理')
  }
  
  if (performance.deviceMemory <= 2) {
    suggestions.push('设备内存较少，建议启用内存优化模式')
  }
  
  return suggestions
}

/**
 * 性能监控类
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {}
    this.observers = []
    this.isMonitoring = false
  }
  
  start() {
    if (this.isMonitoring) return
    
    this.isMonitoring = true
    this.startFPSMonitoring()
    this.startMemoryMonitoring()
    this.startNetworkMonitoring()
  }
  
  stop() {
    this.isMonitoring = false
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
  }
  
  startFPSMonitoring() {
    let frameCount = 0
    let lastTime = performance.now()
    
    const measureFPS = () => {
      if (!this.isMonitoring) return
      
      frameCount++
      const currentTime = performance.now()
      
      if (currentTime >= lastTime + 1000) {
        this.metrics.fps = Math.round((frameCount * 1000) / (currentTime - lastTime))
        frameCount = 0
        lastTime = currentTime
      }
      
      requestAnimationFrame(measureFPS)
    }
    
    measureFPS()
  }
  
  startMemoryMonitoring() {
    const updateMemory = () => {
      if (!this.isMonitoring) return
      
      if (performance.memory) {
        this.metrics.memory = {
          used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024),
          total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024)
        }
      }
      
      setTimeout(updateMemory, 1000)
    }
    
    updateMemory()
  }
  
  startNetworkMonitoring() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          entries.forEach(entry => {
            if (entry.entryType === 'resource') {
              this.metrics.networkRequests = this.metrics.networkRequests || []
              this.metrics.networkRequests.push({
                name: entry.name,
                duration: entry.duration,
                size: entry.transferSize || 0
              })
            }
          })
        })
        
        observer.observe({ entryTypes: ['resource'] })
        this.observers.push(observer)
      } catch (e) {
        console.warn('Network monitoring not supported:', e)
      }
    }
  }
  
  getMetrics() {
    return { ...this.metrics }
  }
}

/**
 * 全局性能监控实例
 */
export const globalPerformanceMonitor = new PerformanceMonitor()

/**
 * 性能优化工具
 */
export const PerformanceOptimizer = {
  // 图片懒加载
  enableLazyLoading() {
    if ('IntersectionObserver' in window) {
      const images = document.querySelectorAll('img[data-src]')
      const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target
            img.src = img.dataset.src
            img.removeAttribute('data-src')
            imageObserver.unobserve(img)
          }
        })
      })
      
      images.forEach(img => imageObserver.observe(img))
    }
  },
  
  // 预加载关键资源
  preloadCriticalResources(urls) {
    urls.forEach(url => {
      const link = document.createElement('link')
      link.rel = 'preload'
      link.href = url
      link.as = url.endsWith('.css') ? 'style' : 'script'
      document.head.appendChild(link)
    })
  },
  
  // 启用 Service Worker 缓存
  enableServiceWorkerCache() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js')
        .then(registration => {
          console.log('Service Worker registered:', registration)
        })
        .catch(error => {
          console.log('Service Worker registration failed:', error)
        })
    }
  },
  
  // 优化动画性能
  optimizeAnimations() {
    const style = document.createElement('style')
    style.textContent = `
      @media (prefers-reduced-motion: reduce) {
        *, *::before, *::after {
          animation-duration: 0.01ms !important;
          animation-iteration-count: 1 !important;
          transition-duration: 0.01ms !important;
        }
      }
    `
    document.head.appendChild(style)
  }
}