import axios from 'axios'

const API_BASE_URL = process.env.NODE_ENV === 'production'
    ? '/api'
    : 'http://localhost:8123/api'

const request = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000
})

/**
 * 封装 SSE 连接
 * @param {string} url - 相对路径
 * @param {object} params - 查询参数
 * @param {function} onMessage - 收到消息回调
 * @param {function} onError - 出错回调
 * @returns {EventSource}
 */
export const connectSSE = (url, params, onMessage, onError) => {
    const queryString = Object.keys(params)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&')

    const fullUrl = `${API_BASE_URL}${url}?${queryString}`
    const eventSource = new EventSource(fullUrl)

    eventSource.onmessage = event => {
        const data = event.data
        if (onMessage) onMessage(data)
        // [DONE] 时关闭连接
        if (data === '[DONE]') {
            eventSource.close()
        }
    }

    eventSource.onerror = error => {
        if (onError) onError(error)
        eventSource.close()
    }

    return eventSource
}

/**
 * AI 恋爱大师聊天
 * 修复：补全 onMessage / onError 参数，与 connectSSE 签名一致
 */
export const chatWithLoveApp = (message, chatId, onMessage, onError) => {
    return connectSSE('/ai/love_app/chat/sse', { message, chatId }, onMessage, onError)
}

/**
 * AI 超级智能体聊天
 * 修复：补全 onMessage / onError 参数，与 connectSSE 签名一致
 */
export const chatWithManus = (message, onMessage, onError) => {
    return connectSSE('/ai/manus/chat', { message }, onMessage, onError)
}

export default {
    chatWithLoveApp,
    chatWithManus
}
