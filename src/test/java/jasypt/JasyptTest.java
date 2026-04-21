package jasypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;

/**
 * 密码加密
 */
public class JasyptTest {

    public static void main(String[] args) {
        // 1. 创建加密器
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        // 2. 配置加密规则（和命令行一致）
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword("mySecretKey"); // 加密密钥（重要！需保密）
        config.setAlgorithm("PBEWithMD5AndDES"); // 加密算法
        encryptor.setConfig(config);
        // 3. 加密明文 123456
        String cipherText = encryptor.encrypt("123456");
        System.out.println("加密后的密文：" + cipherText);
        // 示例输出（每次加密结果不同，但解密后都是 123456）：
        // +rG7R9+8Z9X8a7b6C5d4e3f2g1h0i9j8k7l6=
    }

}
