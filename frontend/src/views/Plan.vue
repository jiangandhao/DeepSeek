<template>
  <div class="page">
    <div class="page-heading">
      <div><p class="eyebrow">Personal plan</p><h1 class="page-title">方案与行动</h1><p class="page-subtitle">方案来自你的最新健康档案，并会随反馈持续调整。</p></div>
      <div class="heading-actions">
        <el-button :loading="loading" @click="generate"><MagicStick />重新生成</el-button>
        <el-button type="primary" :loading="saving" :disabled="!currentPlan || !dirty" @click="savePlan"><DocumentChecked />{{ dirty ? '保存当前方案' : '方案已保存' }}</el-button>
      </div>
    </div>

    <section class="progress surface">
      <div><span class="soft-tag">本周行动计划</span><h2>本周完成 {{ completedCount }} / {{ week.length }} 项</h2><p>{{ progressCopy }}</p></div>
      <div class="progress-ring" :style="{ '--progress': progressPercent }"><b>{{ progressPercent }}%</b></div>
    </section>

    <div class="status-row">
      <span v-if="savedAt" class="save-status"><CircleCheck />已保存于 {{ savedAt }}</span>
      <span>{{ savedPlans.length }} 个历史版本</span>
    </div>

    <div class="tabs"><button v-for="tab in tabs" :key="tab.key" :class="{active:activeTab===tab.key}" type="button" @click="activeTab=tab.key"><component :is="tab.icon" />{{tab.label}}</button></div>

    <el-skeleton v-if="loading && !currentPlan" :rows="8" animated />
    <section v-else-if="activeTab==='diet'" class="plan-grid">
      <article class="surface meals">
        <div class="card-head"><div><p class="eyebrow">Nutrition</p><h2 class="section-title">今日饮食处方</h2></div><span class="soft-tag">{{ totalCalories.toLocaleString() }} kcal</span></div>
        <div v-for="meal in meals" :key="meal.mealType" class="meal">
          <span class="meal-time">{{meal.time}}</span><div class="meal-photo" :class="meal.color"><Food /></div>
          <div><b>{{meal.name}}</b><p>{{meal.detail}}</p><small>{{meal.calories}} kcal · 碳水 {{meal.carbsG}}g · GL {{meal.gl}}</small></div>
          <el-dropdown trigger="click" @command="choice => replaceMeal(meal, choice)">
            <button class="replace-button" type="button"><Refresh />替换<ArrowDown /></button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item v-for="choice in meal.options.filter(item => item.name !== meal.name)" :key="choice.name" :command="choice"><div class="food-option"><b>{{choice.name}}</b><small>{{choice.calories}} kcal · GL {{choice.gl}}</small></div></el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </article>
      <article class="surface nutrition"><p class="eyebrow">Daily balance</p><h2 class="section-title">营养目标</h2><div ref="pieRef" class="pie"/><div class="nutrition-list"><span><i class="carb"/>碳水 <b>{{targets.carbohydrate_percent || 45}}%</b></span><span><i class="protein"/>蛋白质 <b>{{targets.protein_percent || 25}}%</b></span><span><i class="fat"/>脂肪 <b>{{targets.fat_percent || 30}}%</b></span></div><div class="goal"><b>膳食纤维</b><span>{{targets.fiber_g || 25}}g / 天</span><el-progress :percentage="84" :show-text="false" color="#35a889" /></div><div class="target-list"><span>钠摄入上限 <b>{{targets.sodium_mg_max || 2000}}mg</b></span><span>饮水建议 <b>{{targets.water_ml || 1950}}ml</b></span></div></article>
    </section>

    <section v-else-if="activeTab==='exercise'" class="surface exercise-card">
      <div class="card-head"><div><p class="eyebrow">AI Coach</p><h2 class="section-title">本周运动计划</h2><p class="section-copy">点击当天任务即可确认完成，记录会同步到运动档案。</p></div><span class="soft-tag">目标 {{weeklyMinutes}} 分钟</span></div>
      <div class="week"><button v-for="day in week" :key="day.dateIso" :class="{today:day.today,done:day.done,future:day.future}" :disabled="day.future || day.saving" type="button" @click="toggleExercise(day)"><small>{{day.label}}</small><b>{{day.date}}</b><span><Loading v-if="day.saving"/><Check v-else-if="day.done"/><template v-else>{{day.task}}</template></span><em>{{day.duration}} 分钟</em></button></div>
      <div v-if="todayTask" class="today-workout"><span><Bicycle /></span><div><p>今日任务</p><h3>{{todayTask.type}} · {{todayTask.duration}} 分钟</h3><small>建议心率 {{todayTask.heartRate}} 次/分。{{todayTask.precaution}}</small></div><el-button :type="todayTask.done?'success':'primary'" :loading="todayTask.saving" @click="toggleExercise(todayTask)"><Check />{{todayTask.done?'已完成，点击撤销':'确认完成'}}</el-button></div>
    </section>

    <section v-else class="surface ai-plan"><div class="card-head"><div><p class="eyebrow">Comprehensive plan</p><h2 class="section-title">综合健康方案</h2></div><el-button type="primary" plain :loading="saving" :disabled="!dirty" @click="savePlan">保存此版本</el-button></div><div v-if="planContent" class="markdown" v-html="renderMd(planContent)"/><el-empty v-else description="正在生成基于健康档案的综合方案"/></section>

    <section class="adjust surface" :class="{expanded:aiReply}"><span class="assistant-avatar"><ChatDotRound /></span><div><b>DeepSeek 方案调整助手</b><p>结合你的健康档案、最近记录和当前方案给出调整建议。</p></div><div class="adjust-input"><el-input v-model="question" :disabled="adjusting" placeholder="例如：今晚有聚餐，饮食怎么调整？" @keyup.enter="ask"/><el-button type="primary" :loading="adjusting" :disabled="!question.trim()" @click="ask">发送</el-button></div><div v-if="aiReply" class="ai-reply"><div class="reply-head"><b>AI 调整建议</b><button type="button" @click="aiReply=''">关闭</button></div><div class="markdown" v-html="renderMd(aiReply)"/></div></section>
    <p class="ai-note">本方案由 AI 生成，仅供参考，不替代专业医疗意见。涉及药物剂量、治疗调整等决策，请咨询执业医师。</p>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { MagicStick, Food, Bicycle, ChatDotRound, Refresh, Document, DocumentChecked, CircleCheck, ArrowDown, Check, Loading } from '@element-plus/icons-vue'
import { addExercise, deleteExercise, healthPlan, listExercise, listSavedPlans, saveStructuredPlan, structuredHealthPlan } from '../api'

const activeTab=ref('diet'),loading=ref(false),saving=ref(false),adjusting=ref(false),dirty=ref(false)
const planContent=ref(''),question=ref(''),aiReply=ref(''),pieRef=ref(),currentPlan=ref(null),savedAt=ref(''),savedPlans=ref([]),meals=ref([]),week=ref([])
let pie
const tabs=[{key:'diet',label:'饮食方案',icon:Food},{key:'exercise',label:'运动计划',icon:Bicycle},{key:'ai',label:'综合方案',icon:Document}]
const targets=computed(()=>currentPlan.value?.daily_targets||{})
const totalCalories=computed(()=>meals.value.reduce((sum,meal)=>sum+Number(meal.calories||0),0))
const completedCount=computed(()=>week.value.filter(day=>day.done).length)
const progressPercent=computed(()=>week.value.length?Math.round(completedCount.value/week.value.length*100):0)
const weeklyMinutes=computed(()=>week.value.reduce((sum,day)=>sum+day.duration,0))
const todayTask=computed(()=>week.value.find(day=>day.today)||week.value[0])
const progressCopy=computed(()=>completedCount.value===week.value.length?'本周计划已全部完成，做得很稳。':completedCount.value?'保持节奏，完成一次就记录一次。':'从今天最容易完成的一项开始。')

function normalizeDish(dish){return {name:dish.name,foods:dish.foods||[],calories:Number(dish.calories||0),carbsG:Number(dish.carbs_g||0),gl:Number(dish.gl||0)}}
function localDate(date){const pad=value=>String(value).padStart(2,'0');return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}`}
function mapMeal(meal,index){const options=[normalizeDish(meal),...(meal.alternatives||[]).map(normalizeDish)];const current=options[0];return {mealType:meal.meal_type,time:meal.time,color:['mint','orange','blue','mint'][index],options,...current,detail:foodText(current.foods)}}
function foodText(foods){return foods.map(food=>`${food.name} ${food.amount}`).join('、')}
function weekDates(items){const today=new Date(),day=today.getDay()||7,monday=new Date(today);monday.setHours(0,0,0,0);monday.setDate(today.getDate()-day+1);return items.map((item,index)=>{const date=new Date(monday);date.setDate(monday.getDate()+index);const iso=localDate(date);return {label:['周一','周二','周三','周四','周五','周六','周日'][index],date:String(date.getDate()),dateIso:iso,today:iso===localDate(today),future:date>today,task:item.type.slice(0,1),type:item.type,duration:item.duration_min,intensity:item.intensity,heartRate:item.heart_rate,precaution:item.precaution,done:false,saving:false,recordId:null}})}
function syncPlan(){if(!currentPlan.value)return;currentPlan.value.meals=meals.value.map(meal=>({meal_type:meal.mealType,time:meal.time,name:meal.name,foods:meal.foods,calories:meal.calories,carbs_g:meal.carbsG,gl:meal.gl,alternatives:meal.options.filter(item=>item.name!==meal.name).map(item=>({name:item.name,foods:item.foods,calories:item.calories,carbs_g:item.carbsG,gl:item.gl}))}))}
function buildMarkdown(plan){return `## 今日营养目标\n- 总能量：${plan.daily_targets.calories} kcal\n- 膳食纤维：${plan.daily_targets.fiber_g} g\n- 钠摄入上限：${plan.daily_targets.sodium_mg_max} mg\n\n## 今日食谱\n${plan.meals.map(meal=>`- **${meal.name}**：${foodText(meal.foods)}（${meal.calories} kcal，GL ${meal.gl}）`).join('\n')}\n\n## 本周运动\n${plan.exercise_week.map((item,index)=>`- 周${'一二三四五六日'[index]}：${item.type} ${item.duration_min} 分钟，心率 ${item.heart_rate}`).join('\n')}\n\n## 监测建议\n${plan.monitoring.map(item=>`- ${item}`).join('\n')}\n\n> ${plan.disclaimer}`}
async function loadCheckins(){if(!week.value.length)return;try{const records=await listExercise({from:`${week.value[0].dateIso}T00:00:00`});for(const day of week.value){const found=records.find(item=>item.doneAt?.slice(0,10)===day.dateIso&&item.type===day.type);if(found){day.done=true;day.recordId=found.id}}}catch{/* 页面仍可使用，提交时会再次校验 */}}
async function generate(){loading.value=true;try{const plan=await structuredHealthPlan();currentPlan.value=plan;meals.value=plan.meals.map(mapMeal);week.value=weekDates(plan.exercise_week);planContent.value=buildMarkdown(plan);dirty.value=true;savedAt.value='';await loadCheckins();await nextTick();renderPie();ElMessage.success('方案已根据最新健康数据更新')}catch{ElMessage.error('方案生成失败，请确认后端和 AI 服务正常运行')}finally{loading.value=false}}
function replaceMeal(meal,choice){Object.assign(meal,choice,{detail:foodText(choice.foods)});syncPlan();planContent.value=buildMarkdown(currentPlan.value);dirty.value=true;savedAt.value='';ElMessage.success(`已替换为“${choice.name}”，营养数据已同步更新`)}
async function toggleExercise(day){if(day.future||day.saving)return;day.saving=true;try{if(day.done){if(day.recordId)await deleteExercise(day.recordId);day.done=false;day.recordId=null;ElMessage.success('已撤销本次运动打卡')}else{const record=await addExercise({type:day.type,durationMin:day.duration,intensity:day.intensity,calories:Math.round(day.duration*(day.intensity==='LOW'?3:day.intensity==='HIGH'?8:5)),doneAt:`${day.dateIso}T18:30:00`});day.done=true;day.recordId=record.id;ElMessage.success('运动已完成并同步到档案')}}finally{day.saving=false}}
async function savePlan(){if(!currentPlan.value)return;saving.value=true;try{syncPlan();await saveStructuredPlan(currentPlan.value);dirty.value=false;savedAt.value=new Intl.DateTimeFormat('zh-CN',{hour:'2-digit',minute:'2-digit'}).format(new Date());savedPlans.value=await listSavedPlans();ElMessage.success('综合方案已安全保存，可在历史版本中回看')}finally{saving.value=false}}
async function ask(){const input=question.value.trim();if(!input||adjusting.value)return;adjusting.value=true;aiReply.value='';try{const summary=meals.value.map(meal=>`${meal.mealType}:${meal.name}`).join('；');const result=await healthPlan({question:`当前饮食方案为：${summary}。用户希望调整：${input}。请结合健康档案和近期数据，给出明确可执行的替换、分量和运动建议，并说明调整原因。`});aiReply.value=result.content;question.value='';const fallback=result.context?.includes('local-rag-fallback');ElMessage.success(fallback?'DeepSeek 暂不可用，已切换本地健康知识库':'DeepSeek 已完成方案调整建议')}catch{ElMessage.error('调整助手暂时不可用，请稍后重试')}finally{adjusting.value=false}}
function renderMd(text){return marked.parse((text||'').replace(/<[^>]*>/g,''))}
function renderPie(){if(!pieRef.value)return;pie ||= echarts.init(pieRef.value);pie.setOption({series:[{type:'pie',radius:['58%','78%'],label:{show:false},data:[{value:targets.value.carbohydrate_percent||45,itemStyle:{color:'#35a889'}},{value:targets.value.protein_percent||25,itemStyle:{color:'#7bb6d9'}},{value:targets.value.fat_percent||30,itemStyle:{color:'#eebd74'}}]}],graphic:[{type:'text',left:'center',top:'42%',style:{text:'营养\n均衡',textAlign:'center',fill:'#18352f',font:'700 16px sans-serif',lineHeight:22}}]})}
onMounted(async()=>{try{savedPlans.value=await listSavedPlans()}catch{savedPlans.value=[]}await generate()})
onBeforeUnmount(()=>pie?.dispose())
</script>

<style scoped>
.heading-actions{display:flex;gap:8px}.heading-actions svg{width:16px}.progress{display:flex;align-items:center;justify-content:space-between;padding:25px 28px;background:linear-gradient(120deg,#fff,#f0f8f5)}.progress h2{margin:15px 0 5px}.progress p{margin:0;color:var(--muted)}.progress-ring{display:grid;width:85px;height:85px;place-items:center;background:radial-gradient(circle,#fff 58%,transparent 59%),conic-gradient(var(--mint-500) calc(var(--progress)*1%),#dfece7 0);border-radius:50%}.status-row{display:flex;justify-content:flex-end;gap:16px;margin-top:10px;color:var(--muted);font-size:11px}.save-status{display:flex;align-items:center;gap:5px;color:var(--mint-700)}.save-status svg{width:14px}.tabs{display:flex;gap:8px;margin:18px 0}.tabs button{display:flex;align-items:center;gap:7px;padding:10px 16px;color:var(--muted);background:white;border:1px solid var(--line);border-radius:12px;cursor:pointer}.tabs svg{width:16px}.tabs .active{color:#fff;background:var(--mint-700);border-color:var(--mint-700)}.plan-grid{display:grid;grid-template-columns:1.35fr .65fr;gap:18px}.meals,.nutrition,.exercise-card,.ai-plan{padding:24px}.card-head{display:flex;align-items:center;justify-content:space-between}.meal{display:grid;grid-template-columns:45px 62px 1fr auto;align-items:center;gap:13px;padding:17px 0;border-bottom:1px solid var(--line)}.meal:last-child{border:0}.meal-time{color:var(--muted);font-size:12px}.meal-photo{display:grid;width:58px;height:58px;place-items:center;border-radius:15px}.meal-photo svg{width:25px}.meal-photo.mint{color:#217561;background:#daf2e9}.meal-photo.orange{color:#946118;background:#f9e9cb}.meal-photo.blue{color:#397495;background:#dcecf5}.meal b,.meal p,.meal small{display:block}.meal p{margin:5px 0;color:var(--muted);font-size:12px}.meal small{color:#81958e}.replace-button{display:flex;align-items:center;gap:4px;padding:7px 9px;color:var(--mint-700);background:#f1f8f5;border:1px solid var(--line);border-radius:9px;cursor:pointer}.replace-button svg{width:13px}.food-option{min-width:210px;padding:3px}.food-option b,.food-option small{display:block}.food-option small{margin-top:4px;color:var(--muted)}.pie{height:210px}.nutrition-list{display:grid;gap:9px}.nutrition-list span{display:flex;align-items:center;color:var(--muted);font-size:12px}.nutrition-list i{width:8px;height:8px;margin-right:7px;border-radius:50%}.nutrition-list b{margin-left:auto;color:var(--ink)}.carb{background:#35a889}.protein{background:#7bb6d9}.fat{background:#eebd74}.goal{margin-top:20px;padding-top:16px;border-top:1px solid var(--line)}.goal>span{float:right;color:var(--muted);font-size:11px}.goal .el-progress{margin-top:10px}.target-list{display:grid;gap:8px;margin-top:16px;padding-top:14px;border-top:1px solid var(--line)}.target-list span{display:flex;justify-content:space-between;color:var(--muted);font-size:11px}.target-list b{color:var(--ink)}.week{display:grid;grid-template-columns:repeat(7,1fr);gap:9px;margin:22px 0}.week button{display:grid;gap:7px;padding:13px 8px;color:var(--muted);background:#f8faf9;border:1px solid var(--line);border-radius:14px;cursor:pointer;transition:.2s}.week button:hover:not(:disabled){transform:translateY(-2px);border-color:var(--mint-500)}.week button span{display:grid;width:30px;height:30px;margin:auto;place-items:center;background:#e8f2ee;border-radius:50%}.week button span svg{width:15px}.week button.done span{color:white;background:var(--mint-500)}.week button.today{color:var(--mint-700);background:#edf8f4;border-color:var(--mint-500);box-shadow:0 0 0 2px rgba(53,168,137,.1)}.week button.future{cursor:not-allowed;opacity:.55}.week button em{font-size:9px;font-style:normal}.today-workout{display:grid;grid-template-columns:54px 1fr auto;align-items:center;gap:14px;padding:20px;background:#f2f8f5;border-radius:17px}.today-workout>span{display:grid;width:52px;height:52px;place-items:center;color:var(--mint-700);background:white;border-radius:14px}.today-workout svg{width:22px}.today-workout p,.today-workout h3{margin:0}.today-workout p,.today-workout small{color:var(--muted)}.today-workout h3{margin:4px 0}.markdown{overflow:auto;line-height:1.8}.ai-plan .markdown{max-height:560px;margin-top:18px}.adjust{display:grid;grid-template-columns:46px 1fr minmax(300px,.9fr);align-items:center;gap:14px;margin:18px 0;padding:18px 21px}.adjust.expanded{align-items:start}.assistant-avatar{display:grid;width:44px;height:44px;place-items:center;color:#fff;background:var(--mint-700);border-radius:14px}.assistant-avatar svg{width:21px}.adjust p{margin:4px 0 0;color:var(--muted);font-size:11px}.adjust-input{display:flex;gap:8px}.ai-reply{grid-column:1/-1;padding:18px;margin-top:4px;background:#f4f9f7;border:1px solid var(--line);border-radius:15px}.reply-head{display:flex;justify-content:space-between;padding-bottom:10px;border-bottom:1px solid var(--line)}.reply-head button{color:var(--muted);background:none;border:0;cursor:pointer}.ai-reply .markdown{margin-top:10px;max-height:360px}
@media(max-width:900px){.plan-grid{grid-template-columns:1fr}.adjust{grid-template-columns:45px 1fr}.adjust-input{grid-column:1/-1}.week{overflow-x:auto}.week button{min-width:76px}}@media(max-width:600px){.heading-actions{width:100%}.heading-actions .el-button{flex:1}.progress-ring{display:none}.meal{grid-template-columns:40px 52px 1fr}.meal .el-dropdown{grid-column:3}.today-workout{grid-template-columns:45px 1fr}.today-workout .el-button{grid-column:1/-1}.adjust-input{flex-direction:column}.status-row{justify-content:flex-start}}
</style>
