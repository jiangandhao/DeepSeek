<template>
  <div class="landing">
    <header class="topnav">
      <div class="topnav-inner">
        <button class="brand" type="button" @click="goTop">
          <span class="brand-mark"><FirstAidKit /></span>
          <span class="brand-text"><strong>知愈健康</strong><small>DeepSeek Care</small></span>
        </button>
        <nav class="links">
          <a href="#features">功能</a>
          <a href="#how">使用流程</a>
          <a href="#privacy">隐私</a>
        </nav>
        <div class="actions">
          <el-button text @click="go('/login')">登录</el-button>
          <el-button type="primary" round @click="go('/register')">免费注册</el-button>
        </div>
      </div>
    </header>

    <main>
      <!-- Hero -->
      <section class="hero">
        <div class="hero-copy">
          <span class="soft-tag"><Pulse /> AI 驱动的全周期健康管理</span>
          <h1>把每天的健康数据，<br />变成可执行的行动方案</h1>
          <p>
            知愈健康连接你的智能设备与体检报告，自动汇总血糖、饮食、运动等数据，
            结合 DeepSeek AI 给出个性化监测预警与每日健康计划。
          </p>
          <div class="hero-cta">
            <el-button type="primary" size="large" round @click="go('/register')">
              立即开始 <ArrowRight />
            </el-button>
            <el-button size="large" round @click="go('/login')">我已有账号</el-button>
          </div>
          <ul class="hero-points">
            <li><Check /> 数据加密存储，默认不共享</li>
            <li><Check /> 多端同步，随时查看</li>
            <li><Check /> 无需信用卡即可体验</li>
          </ul>
        </div>
        <div class="hero-art">
          <div class="art-card art-main">
            <div class="art-row"><span>今日血糖</span><b>5.6<i>mmol/L</i></b></div>
            <div class="art-bars"><i v-for="n in 7" :key="n" :style="{ height: barHeights[n - 1] + '%' }" /></div>
            <p class="art-note">趋势平稳，继续保持 👍</p>
          </div>
          <div class="art-card art-float art-ai">
            <span class="art-ai-icon"><ChatDotRound /></span>
            <div><b>AI 健康助手</b><small>建议今晚散步 20 分钟</small></div>
          </div>
          <div class="art-card art-float art-plan">
            <span class="art-plan-icon"><Calendar /></span>
            <div><b>今日计划</b><small>3 / 5 已完成</small></div>
          </div>
        </div>
      </section>

      <!-- Features -->
      <section id="features" class="section">
        <div class="section-head">
          <p class="eyebrow">核心功能</p>
          <h2>一个平台，覆盖健康管理的每一步</h2>
        </div>
        <div class="feature-grid">
          <article v-for="f in features" :key="f.title" class="feature-card">
            <span class="feature-icon"><component :is="f.icon" /></span>
            <h3>{{ f.title }}</h3>
            <p>{{ f.desc }}</p>
          </article>
        </div>
      </section>

      <!-- How it works -->
      <section id="how" class="section how">
        <div class="section-head">
          <p class="eyebrow">使用流程</p>
          <h2>三步开启你的健康管理</h2>
        </div>
        <div class="steps">
          <div v-for="(s, i) in steps" :key="s.title" class="step">
            <span class="step-num">{{ i + 1 }}</span>
            <h3>{{ s.title }}</h3>
            <p>{{ s.desc }}</p>
          </div>
        </div>
      </section>

      <!-- Privacy -->
      <section id="privacy" class="section">
        <div class="privacy-banner">
          <span class="privacy-icon"><Lock /></span>
          <div>
            <h2>数据由你掌控</h2>
            <p>所有健康数据均加密存储，默认不对外共享。你可以随时导出或删除自己的数据。</p>
          </div>
        </div>
      </section>

      <!-- CTA -->
      <section class="cta">
        <h2>准备好更轻松地管理健康了吗？</h2>
        <p>现在注册，立即体验 AI 健康助手与个性化监测。</p>
        <el-button type="primary" size="large" round @click="go('/register')">免费注册</el-button>
      </section>
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <div class="brand small">
          <span class="brand-mark"><FirstAidKit /></span>
          <span class="brand-text"><strong>知愈健康</strong><small>DeepSeek Care</small></span>
        </div>
        <span class="copy">© {{ year }} 知愈健康 · 仅用于健康管理演示</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  FirstAidKit, ArrowRight, Check, ChatDotRound, Calendar, Lock,
  TrendCharts, Watch, Document, User
} from '@element-plus/icons-vue'

const Pulse = FirstAidKit
const router = useRouter()
const year = new Date().getFullYear()
const barHeights = [42, 60, 48, 72, 55, 68, 50]

const features = [
  { title: '健康监测', desc: '汇总血糖、血压、心率等指标，自动识别异常趋势并及时预警。', icon: TrendCharts },
  { title: '每日方案', desc: '根据你的数据生成饮食、运动与作息建议，把目标拆成今日可执行的行动。', icon: Calendar },
  { title: 'AI 健康助手', desc: '随时提问健康疑惑，DeepSeek AI 结合你的档案给出专属解读与建议。', icon: ChatDotRound },
  { title: '设备接入', desc: '绑定智能手环、血糖仪等设备，数据自动上报，免去手动记录。', icon: Watch },
  { title: '体检与影像', desc: '在线预约体检、管理报告与影像资料，历史记录一目了然。', icon: Document },
  { title: '个人健康档案', desc: '集中管理你的基础信息与长期健康记录，多端随时查看。', icon: User }
]

const steps = [
  { title: '注册账号', desc: '填写基础健康信息，几秒钟即可创建你的专属档案。' },
  { title: '连接数据', desc: '绑定设备或手动录入血糖、饮食、运动等记录。' },
  { title: '获取方案', desc: '查看 AI 生成的监测预警与每日健康行动计划。' }
]

function go(path) { router.push(path) }
function goTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
</script>

<style scoped>
.landing {
  min-height: 100vh;
  color: var(--ink);
  background:
    radial-gradient(circle at 12% 0%, rgba(53, 168, 137, .12), transparent 34%),
    linear-gradient(180deg, #f1f9f5 0%, #f7fbfa 40%, #ffffff 100%);
}

/* Top nav */
.topnav { position: sticky; top: 0; z-index: 20; background: rgba(247, 251, 250, .82); border-bottom: 1px solid rgba(222, 235, 230, .7); backdrop-filter: blur(14px); }
.topnav-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; max-width: 1160px; margin: 0 auto; padding: 14px 24px; }
.brand { display: flex; align-items: center; gap: 11px; padding: 0; color: inherit; background: none; border: 0; cursor: pointer; }
.brand-mark { display: grid; width: 40px; height: 40px; place-items: center; color: #123d35; background: #d8f2e9; border-radius: 13px; }
.brand-mark svg { width: 22px; }
.brand-text strong { display: block; font-size: 17px; letter-spacing: .04em; }
.brand-text small { display: block; margin-top: 2px; color: var(--muted); font-size: 11px; }
.links { display: flex; gap: 26px; }
.links a { color: var(--mint-900); font-size: 14px; font-weight: 600; text-decoration: none; opacity: .8; }
.links a:hover { opacity: 1; color: var(--mint-700); }
.actions { display: flex; align-items: center; gap: 8px; }

/* Hero */
.hero { display: grid; grid-template-columns: 1.05fr .95fr; align-items: center; gap: 48px; max-width: 1160px; margin: 0 auto; padding: 64px 24px 40px; }
.hero-copy h1 { margin: 18px 0 16px; font-size: clamp(30px, 4.4vw, 50px); line-height: 1.12; letter-spacing: -.03em; font-weight: 800; }
.hero-copy > p { margin: 0; max-width: 520px; color: var(--muted); font-size: 16px; line-height: 1.75; }
.soft-tag svg { width: 14px; }
.hero-cta { display: flex; flex-wrap: wrap; gap: 12px; margin: 28px 0 22px; }
.hero-cta :deep(.el-button) svg { width: 16px; margin-left: 4px; }
.hero-points { display: grid; gap: 10px; margin: 0; padding: 0; list-style: none; }
.hero-points li { display: flex; align-items: center; gap: 9px; color: var(--mint-900); font-size: 14px; }
.hero-points svg { width: 17px; padding: 3px; color: #fff; background: var(--mint-500); border-radius: 50%; flex: none; }

/* Hero art */
.hero-art { position: relative; min-height: 340px; }
.art-card { background: var(--surface); border: 1px solid var(--line); border-radius: 22px; box-shadow: var(--shadow); }
.art-main { padding: 26px; }
.art-row { display: flex; align-items: baseline; justify-content: space-between; }
.art-row span { color: var(--muted); font-size: 14px; font-weight: 600; }
.art-row b { font-size: 38px; font-weight: 800; color: var(--mint-700); }
.art-row i { margin-left: 6px; font-size: 14px; font-weight: 600; color: var(--muted); font-style: normal; }
.art-bars { display: flex; align-items: flex-end; gap: 10px; height: 120px; margin: 22px 0 14px; }
.art-bars i { flex: 1; background: linear-gradient(180deg, var(--mint-500), var(--mint-100)); border-radius: 8px 8px 4px 4px; }
.art-note { margin: 0; color: var(--muted); font-size: 13px; }
.art-float { position: absolute; display: flex; align-items: center; gap: 11px; padding: 13px 16px; }
.art-float b { display: block; font-size: 14px; }
.art-float small { display: block; margin-top: 2px; color: var(--muted); font-size: 12px; }
.art-ai { right: -8px; top: 6px; }
.art-plan { left: -16px; bottom: 4px; }
.art-ai-icon, .art-plan-icon { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 11px; flex: none; }
.art-ai-icon { color: #123d35; background: #bfe8d9; }
.art-ai-icon svg, .art-plan-icon svg { width: 19px; }
.art-plan-icon { color: var(--mint-700); background: var(--mint-100); }

/* Sections */
.section { max-width: 1160px; margin: 0 auto; padding: 56px 24px; }
.section-head { max-width: 640px; margin: 0 auto 40px; text-align: center; }
.section-head h2 { margin: 6px 0 0; font-size: clamp(24px, 3vw, 34px); letter-spacing: -.03em; }

/* Features */
.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.feature-card { padding: 26px 24px; background: var(--surface); border: 1px solid var(--line); border-radius: 20px; box-shadow: var(--shadow); transition: transform .2s ease, box-shadow .2s ease; }
.feature-card:hover { transform: translateY(-4px); box-shadow: 0 24px 56px rgba(25, 68, 57, .12); }
.feature-icon { display: grid; width: 46px; height: 46px; place-items: center; margin-bottom: 16px; color: var(--mint-700); background: var(--mint-100); border-radius: 14px; }
.feature-icon svg { width: 23px; }
.feature-card h3 { margin: 0 0 8px; font-size: 18px; }
.feature-card p { margin: 0; color: var(--muted); font-size: 14px; line-height: 1.7; }

/* Steps */
.how { background: linear-gradient(180deg, rgba(220, 243, 235, .35), transparent); border-radius: 28px; }
.steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.step { padding: 8px 4px; }
.step-num { display: grid; width: 42px; height: 42px; place-items: center; margin-bottom: 14px; color: #fff; background: var(--mint-700); border-radius: 13px; font-weight: 800; font-size: 18px; }
.step h3 { margin: 0 0 8px; font-size: 18px; }
.step p { margin: 0; color: var(--muted); font-size: 14px; line-height: 1.7; }

/* Privacy */
.privacy-banner { display: flex; align-items: center; gap: 22px; padding: 32px; color: #eaf7f2; background: linear-gradient(165deg, #123d35 0%, #0b2f29 100%); border-radius: 24px; box-shadow: var(--shadow); }
.privacy-icon { display: grid; width: 56px; height: 56px; place-items: center; color: #123d35; background: #bfe8d9; border-radius: 16px; flex: none; }
.privacy-icon svg { width: 26px; }
.privacy-banner h2 { margin: 0 0 6px; font-size: 22px; }
.privacy-banner p { margin: 0; color: #b9d6cd; font-size: 14px; line-height: 1.7; }

/* CTA */
.cta { max-width: 760px; margin: 0 auto; padding: 64px 24px 72px; text-align: center; }
.cta h2 { margin: 0 0 10px; font-size: clamp(24px, 3vw, 32px); letter-spacing: -.03em; }
.cta p { margin: 0 0 26px; color: var(--muted); font-size: 16px; }

/* Footer */
.footer { border-top: 1px solid var(--line); background: rgba(247, 251, 250, .7); }
.footer-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; max-width: 1160px; margin: 0 auto; padding: 24px; }
.footer .copy { color: var(--muted); font-size: 13px; }

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; gap: 36px; padding-top: 44px; }
  .hero-art { order: -1; min-height: 280px; }
  .feature-grid, .steps { grid-template-columns: 1fr 1fr; }
  .links { display: none; }
}
@media (max-width: 600px) {
  .feature-grid, .steps { grid-template-columns: 1fr; }
  .privacy-banner { flex-direction: column; text-align: center; }
  .footer-inner { flex-direction: column; }
  .hero-cta :deep(.el-button) { flex: 1; }
}
</style>
