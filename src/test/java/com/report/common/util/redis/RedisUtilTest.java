package com.report.common.util.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisUtilTest {

    @Autowired
    private RedisUtil redisUtil;

    @AfterEach
    void cleanup() {
        try {
            redisUtil.del("test:key");
        } catch (Exception ignored) {
        }
    }

    // 测试：set和get - 正常存取
    @Test
    void testSetGetSuccess() {
        redisUtil.set("test:key", "hello");
        String result = redisUtil.get("test:key");
        assertEquals("hello", result);
    }

    // 测试：get - key不存在返回null
    @Test
    void testGetKeyNotExist() {
        String result = redisUtil.get("test:key");
        assertNull(result);
    }

    // 测试：del - 删除已存在的key
    @Test
    void testDelExistingKey() {
        redisUtil.set("test:key", "value");
        redisUtil.del("test:key");
        assertNull(redisUtil.get("test:key"));
    }

    // 测试：del - 删除不存在的key不报错
    @Test
    void testDelNonExistingKey() {
        assertDoesNotThrow(() -> redisUtil.del("test:key"));
    }

    // 测试：set - 覆盖已有值
    @Test
    void testSetOverwrite() {
        redisUtil.set("test:key", "old");
        redisUtil.set("test:key", "new");
        assertEquals("new", redisUtil.get("test:key"));
    }
}
