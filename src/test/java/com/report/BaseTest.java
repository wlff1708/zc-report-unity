package com.report;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

    public String readFileContent(String filePath) {
        try {
            return FileUtil.readString(this.getClass().getClassLoader().getResource(filePath).getPath(), StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            throw new RuntimeException(String.format("文件不存在: %s", filePath));
        }
    }
}
