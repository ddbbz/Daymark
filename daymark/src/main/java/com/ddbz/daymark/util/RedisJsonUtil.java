package com.ddbz.daymark.util;

import org.apache.ibatis.type.NStringTypeHandler;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisJsonUtil {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private ObjectMapper objectMapper =new ObjectMapper();

    //String 存操作
    public void set(String key, Object obj){
        String json = objectMapper.writeValueAsString(obj);
        stringRedisTemplate.opsForValue().set(key,json);
    }

    //String 取操作
    public<T> T get(String key,Class<T> clazz){
        String json = stringRedisTemplate.opsForValue().get(key);
        if(json==null) {
            return null;
        }
        return objectMapper.readValue(json,clazz);
    }


    //String列表往尾存入
    public<T> void PushHead(String key, T value) {
        stringRedisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(value));
    }

    //String列表头存入
    public<T> void PushTail(String key, T value) {
        stringRedisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(value));
    }

    //批量向String列表尾存入
    public<T> void PushTailAll(String key, List<T> values){
        for(Object value:values){
            stringRedisTemplate.opsForList().rightPush(key,objectMapper.writeValueAsString(value));
        }
    }

    //String列表取全部操作
    public<T> List<T> lRangeAll(String key, Class<T> clazz) {
         List<String> jsons = stringRedisTemplate.opsForList().range(key, 0, -1);
         if(jsons==null){return null;}
         List<T> results = new ArrayList<>();
         for(String json : jsons){
             results.add(objectMapper.readValue(json,clazz));
         }
         return results;
    }

    //String列表取一段操作
    public<T> List<T> lRange(String key, Class<T> clazz, long start, long end) {
        List<String> jsons = stringRedisTemplate.opsForList().range(key, start, end);
        if(jsons == null){return null;}
        List<T> results = new ArrayList<>();
        for(String json : jsons){
            results.add(objectMapper.readValue(json,clazz));
        }
        return results;
    }

    //删操作
    public void delete(String key){
        stringRedisTemplate.delete(key);
    }
}
