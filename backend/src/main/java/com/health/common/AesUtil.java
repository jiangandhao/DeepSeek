package com.health.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 敏感字段 AES 加解密工具(数据治理 - 隐私脱敏)。
 *
 * 采用 AES/GCM/NoPadding(认证加密 + 每次随机 IV),抵御模式分析并提供完整性校验。
 * 密文格式:Base64(IV[12B] || 密文+GCM标签)。每次加密结果不同(非确定性),
 * 因此**不支持按密文等值检索**——本项目加密字段(user.phone)仅存储与脱敏展示,无等值查询需求。
 *
 * 密钥由配置 secret 经 SHA-256 取前 16 字节派生(AES-128);
 * 密钥在应用启动时由 {@code CryptoConfig} 注入静态字段。
 *
 * 兼容性:解密无法识别的内容(历史明文 / 旧 ECB 密文 / 非法 Base64)时原样返回,不抛异常。
 */
public final class AesUtil {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LEN = 12;     // GCM 推荐 12 字节 IV
    private static final int GCM_TAG_BITS = 128;  // 认证标签长度(bit)
    private static final SecureRandom RANDOM = new SecureRandom();

    private static SecretKeySpec keySpec;

    private AesUtil() {}

    public static void init(String secret) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            keySpec = new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
        } catch (Exception e) {
            throw new IllegalStateException("初始化加密密钥失败", e);
        }
    }

    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            // 拼接 IV || 密文,便于解密时取回 IV
            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            byte[] data = Base64.getDecoder().decode(cipherText);
            if (data.length <= GCM_IV_LEN) {
                // 长度不足以包含 IV + 标签,非本工具产生的密文
                return cipherText;
            }
            byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LEN);
            byte[] ciphertext = Arrays.copyOfRange(data, GCM_IV_LEN, data.length);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 兼容历史明文 / 旧 ECB 密文 / 非法 Base64:解密失败则原样返回
            return cipherText;
        }
    }

    /** 手机号脱敏:保留前3后4,中间用 * 代替。 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
