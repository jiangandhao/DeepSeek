package com.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.health.common.AesUtil;

class AesUtilTest {

    @BeforeAll
    static void init() {
        AesUtil.init("unit-test-secret-key-1234567890");
    }

    @Test
    void encryptDecryptRoundTrip() {
        String plain = "13812345678";
        String cipher = AesUtil.encrypt(plain);
        assertNotEquals(plain, cipher, "密文不应等于明文");
        assertEquals(plain, AesUtil.decrypt(cipher), "解密应还原明文");
    }

    @Test
    void encryptIsNonDeterministic() {
        // GCM 随机 IV:相同明文 -> 每次密文不同,但都能解回原文
        String c1 = AesUtil.encrypt("hello");
        String c2 = AesUtil.encrypt("hello");
        assertNotEquals(c1, c2, "随机 IV 应使两次密文不同");
        assertEquals("hello", AesUtil.decrypt(c1));
        assertEquals("hello", AesUtil.decrypt(c2));
    }

    @Test
    void handlesNullAndEmpty() {
        assertNull(AesUtil.encrypt(null));
        assertEquals("", AesUtil.encrypt(""));
        assertNull(AesUtil.decrypt(null));
    }

    @Test
    void decryptNonCipherReturnsOriginal() {
        // 兼容历史明文:无法解密时原样返回
        assertEquals("plain-legacy", AesUtil.decrypt("plain-legacy"));
    }

    @Test
    void maskPhone() {
        assertEquals("138****5678", AesUtil.maskPhone("13812345678"));
        assertEquals("123", AesUtil.maskPhone("123")); // 过短不脱敏
    }
}
