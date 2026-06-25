// 把 AI 生成的分析结果按 用户+维度 持久化到 localStorage,避免重复生成。
const PREFIX = 'aiInsight:'

function key(aspect) {
  const uid = localStorage.getItem('userId') || 'anon'
  return `${PREFIX}${uid}:${aspect}`
}

/** 读取缓存,返回 { data, savedAt } 或 null。 */
export function loadInsight(aspect) {
  try {
    const raw = localStorage.getItem(key(aspect))
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (!parsed || !parsed.data) return null
    return parsed
  } catch {
    return null
  }
}

/** 写入缓存。 */
export function saveInsight(aspect, data) {
  try {
    localStorage.setItem(key(aspect), JSON.stringify({ data, savedAt: Date.now() }))
  } catch {
    // 存储不可用时忽略,不影响功能
  }
}

/** 清除某维度缓存。 */
export function clearInsight(aspect) {
  try { localStorage.removeItem(key(aspect)) } catch {}
}

/** 友好的相对时间文案。 */
export function savedAgo(ts) {
  if (!ts) return ''
  const s = Math.floor((Date.now() - ts) / 1000)
  if (s < 60) return '刚刚'
  if (s < 3600) return `${Math.floor(s / 60)} 分钟前`
  if (s < 86400) return `${Math.floor(s / 3600)} 小时前`
  return `${Math.floor(s / 86400)} 天前`
}
