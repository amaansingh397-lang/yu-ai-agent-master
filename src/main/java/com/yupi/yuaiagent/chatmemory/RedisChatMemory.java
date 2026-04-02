package com.yupi.yuaiagent.chatmemory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 企业级分布式 AI 对话记忆存储 (基于 Redis)
 */
@Component
public class RedisChatMemory implements ChatMemory {

    // 缓存 Key 的统一前缀，防止和其他业务数据冲突
    private static final String MEMORY_KEY_PREFIX = "ai:chat:memory:";
    // 记忆过期时间：例如用户 7 天不聊天，就清空记忆释放内存
    private static final long EXPIRE_DAYS = 7;

    // 强烈建议在配置类中为这个 Template 配置 Jackson 序列化器 (GenericJackson2JsonRedisSerializer)
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisChatMemory(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 追加新的聊天记录
     */
    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String redisKey = Objects.requireNonNull(getRedisKey(conversationId));
        Object[] values = Objects.requireNonNull(messages.toArray(new Object[0]));

        // 核心优化：使用 Redis 的 List 数据结构进行右侧追加 (Right Push)
        // 这里的操作是原子性的，完美避开了 Kryo 的线程安全问题
        redisTemplate.opsForList().rightPushAll(redisKey, values);

        // 每次追加新消息时，顺手刷新一下过期时间（续期）
        redisTemplate.expire(redisKey, EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取历史聊天记录
     */
    @Override
    public void clear(@NonNull String conversationId) {
        String redisKey = Objects.requireNonNull(getRedisKey(conversationId));
        redisTemplate.delete(redisKey);
    }

    /**
     * 清理指定会话的记忆
     */
    @Override
    @NonNull
    public List<Message> get(@NonNull String conversationId) {
        String redisKey = Objects.requireNonNull(getRedisKey(conversationId));

        // range(key, 0, -1) 表示获取 List 中的所有元素
        List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (cachedMessages == null || cachedMessages.isEmpty()) {
            return new ArrayList<>();
        }

        // 将 Object 转换为 Spring AI 需要的 Message 接口实现
        List<Message> result = new ArrayList<>(cachedMessages.size());
        for (Object obj : cachedMessages) {
            if (obj instanceof Message) {
                result.add((Message) obj);
            }
        }
        return result;
    }

    /**
     * 统一生成 Redis Key
     */
    @NonNull
    private String getRedisKey(@NonNull String conversationId) {
        return MEMORY_KEY_PREFIX + conversationId;
    }
}