package top.zhangpy.mychat.entity.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MessageQueue {

    private static final Log log = LogFactory.getLog(MessageQueue.class);
    @Getter
    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<ServerMessage>> messageQueue = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // userId == message.receiverId
    public boolean addMessage(Integer userId, ServerMessage message) {
        if (userId == null || message == null) {
            return false;
        }
        if (!userId.equals(message.getReceiverId())) {
            return false;
        }
        if (messageQueue.containsKey(userId)) {
            messageQueue.get(userId).offer(message);
        } else {
            ConcurrentLinkedQueue<ServerMessage> queue = new ConcurrentLinkedQueue<>();
            queue.offer(message);
            messageQueue.put(userId, queue);
        }
        return true;
    }

    private void saveToRedis(Integer userId, ConcurrentLinkedQueue<ServerMessage> queue){
        try {
            String key = "messageQueue:" + userId;
            String value = objectMapper.writeValueAsString(queue);
            redisTemplate.opsForValue().set(key, value);
        } catch (JsonProcessingException e) {
            log.error("save message queue to redis error: " + e);
        }
    }

//    @PreDestroy
    private void saveAllToRedis(){
        for (Integer userId : messageQueue.keySet()) {
            ConcurrentLinkedQueue<ServerMessage> queue = new ConcurrentLinkedQueue<>(messageQueue.get(userId));
            saveToRedis(userId, queue);
        }
    }

    @PostConstruct
    public void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM Shutdown Hook triggered. Saving data to Redis...");
            saveAllToRedis();
        }));
    }


    @PostConstruct
    private void loadFromRedis(){
        messageQueue.clear();
        for (String key : Objects.requireNonNull(redisTemplate.keys("messageQueue:*"))) {
            Integer userId = Integer.parseInt(key.substring(13));
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.error("load message queue from redis error: value is null ,key = " + key);
                continue;
            }
            try {
                ConcurrentLinkedQueue<ServerMessage> queue = objectMapper.readValue(value,
                        new TypeReference<ConcurrentLinkedQueue<ServerMessage>>() {});
                messageQueue.put(userId, queue);
            } catch (JsonProcessingException e) {
                log.error("load message queue from redis error: " + e);
            }
        }
    }
}
