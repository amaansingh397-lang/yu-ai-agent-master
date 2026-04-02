<template>
  <div class="super-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI超级智能体</h1>
      <div class="placeholder"></div>
    </div>
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            ai-type="super"
            @send-message="sendMessage"
        />
      </div>
    </div>
    <div class="footer-container">
      <AppFooter/>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onBeforeUnmount} from 'vue'
import {useRouter} from 'vue-router'
import {useHead} from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import {chatWithManus} from '../api'

useHead({
  title: 'AI超级智能体 - ccxAI超级智能体应用平台',
  meta: [
    {name: 'description', content: 'AI超级智能体是ccxAI超级智能体应用平台的全能助手'},
    {name: 'keywords', content: 'AI超级智能体,智能助手,专业问答'}
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

const addMessage = (content, isUser, type = '') => {
  messages.value.push({content, isUser, type, time: Date.now()})
}

const sendMessage = (message) => {
  addMessage(message, true, 'user-question')

  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  connectionStatus.value = 'connecting'

  /**
   * 修复：不再用标点符号判断句子结束（原逻辑脆弱，会丢数据）。
   * 改为：每条 SSE 事件即为一个独立步骤结果（后端 BaseAgent 每步 send 一次），
   * 直接将每条消息作为一个气泡追加，简单可靠。
   */
  eventSource = chatWithManus(
      message,
      (data) => {
        if (data === '[DONE]') {
          connectionStatus.value = 'disconnected'
          return
        }
        if (data && data.trim()) {
          addMessage(data, false, 'ai-answer')
        }
      },
      (error) => {
        console.error('SSE Error:', error)
        connectionStatus.value = 'error'
        addMessage('连接出错，请重试。', false, 'ai-error')
      }
  )
}

const goBack = () => router.push('/')

onMounted(() => {
  addMessage('你好，我是AI超级智能体。我可以解答各类问题，提供专业建议，请问有什么可以帮助你的吗？', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<style scoped>
.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f9fbff;
}
.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 16px 24px;
  background-color: #3f51b5;
  color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}
.back-button { font-size: 16px; cursor: pointer; display: flex; align-items: center; transition: opacity 0.2s; justify-self: start; }
.back-button:hover { opacity: 0.8; }
.back-button:before { content: '←'; margin-right: 8px; }
.title { font-size: 20px; font-weight: bold; margin: 0; text-align: center; justify-self: center; }
.placeholder { width: 1px; justify-self: end; }
.content-wrapper { display: flex; flex-direction: column; flex: 1; }
.chat-area {
  flex: 1;
  padding: 16px;
  min-height: calc(100vh - 56px - 180px);
  margin-bottom: 16px;
}
.footer-container { margin-top: auto; }
@media (max-width: 768px) {
  .header { padding: 12px 16px; }
  .title { font-size: 18px; }
  .chat-area { padding: 12px; min-height: calc(100vh - 48px - 160px); margin-bottom: 12px; }
}
@media (max-width: 480px) {
  .header { padding: 10px 12px; }
  .back-button { font-size: 14px; }
  .title { font-size: 16px; }
  .chat-area { padding: 8px; min-height: calc(100vh - 42px - 150px); margin-bottom: 8px; }
}
</style>
