<template>
  <div class="love-master-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI恋爱大师</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            ai-type="love"
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
import {chatWithLoveApp} from '../api'

useHead({
  title: 'AI恋爱大师 - ccxAI超级智能体应用平台',
  meta: [
    {name: 'description', content: 'AI恋爱大师是ccxAI超级智能体应用平台的专业情感顾问'},
    {name: 'keywords', content: 'AI恋爱大师,情感顾问,恋爱咨询'}
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

const generateChatId = () => 'love_' + Math.random().toString(36).substring(2, 10)

const addMessage = (content, isUser) => {
  messages.value.push({content, isUser, time: Date.now()})
}

const sendMessage = (message) => {
  addMessage(message, true)

  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  const aiMessageIndex = messages.value.length
  addMessage('', false)
  connectionStatus.value = 'connecting'

  // 修复：传入 onMessage / onError 回调，与新 api 签名一致
  eventSource = chatWithLoveApp(
      message,
      chatId.value,
      (data) => {
        if (data === '[DONE]') {
          connectionStatus.value = 'disconnected'
          return
        }
        if (aiMessageIndex < messages.value.length) {
          messages.value[aiMessageIndex].content += data
        }
      },
      (error) => {
        console.error('SSE Error:', error)
        connectionStatus.value = 'error'
      }
  )
}

const goBack = () => router.push('/')

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('欢迎来到AI恋爱大师，请告诉我你的恋爱问题，我会尽力给予帮助和建议。', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<style scoped>
.love-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #fff9f9;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #ff6b8b;
  color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}
.back-button { font-size: 16px; cursor: pointer; transition: opacity 0.2s; }
.back-button:hover { opacity: 0.8; }
.back-button:before { content: '←'; margin-right: 8px; }
.title { font-size: 20px; font-weight: bold; margin: 0; }
.chat-id { font-size: 14px; opacity: 0.8; }
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
  .chat-id { display: none; }
  .chat-area { padding: 8px; min-height: calc(100vh - 42px - 150px); margin-bottom: 8px; }
}
</style>
