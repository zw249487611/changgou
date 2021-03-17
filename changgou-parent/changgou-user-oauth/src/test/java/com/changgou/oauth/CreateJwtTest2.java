package com.changgou.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌的创建和解析
 */
public class CreateJwtTest2 {
    /**
     * 创建令牌
     */
    @Test
    public void testCreateToken() {
        //加载证书
        ClassPathResource resource = new ClassPathResource("changgou.jks");
        //获取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "changgou".toCharArray());
        //获取证书中的一堆密钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou", "changgou".toCharArray());

        //取出私钥->RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //创建令牌，需要私钥加盐（RSA算法）
        Map<String, Object> payload = new HashMap<String,Object>();
        payload.put("nikename", "tomcat");
        payload.put("address", "sz");
        payload.put("role", "admin,user");
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    /**
     * 解析令牌
     */
    @Test
    public void testParseToken() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJyb2xlIjoiYWRtaW4sdXNlciIsIm5pa2VuYW1lIjoidG9tY2F0In0.U7jzMshY0HEJP9z3ZPjzIHJ3dEgrVYSp59t4nDJ5a3HHtYAP4KtzdCtIB6eKlT8MyDTUUgaqXuk4IQZ7AGbQ_mWsE46z0YHHLrZYR8JgJrN9PuL4VgZud10fFewnifSzX_FKrJbSkJj2ELrOiFNe3w6wFLjLzeypzF0X_cAZysUo0628LA-6M9l6XVWiJuF23Jzh7yeJo8mli11xVRpkwrXVpfpQa-oJGbuX8BbsmJwJhTrijCMiHLAL1FHdRndMVDYH5ftoT_vkgEgQ0BZnQwlRZ0qGhpsZjEhNITpdkBXmv1e1UQMbnALhfeeB1IAZrwY8vl2HSnrA8t5rXnc_WQ";
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkExKudC8EUlLyQHx+kWIv8DMg9l+BIIg2Ng7sNiPqB/ZpvTjSv8aaAJYHcmL6FhAbJ59GQ3JCFZ6uahXOX59zTaVN/Kz4STeGissLd8Jan574+Sp2jXNmLRxL7BsF/16ZZjRRz1OfFBerjqQdhETY5knmKNdQvS2FPP7ZfwkSXiFvk2NUw9dQpgrQ3gnmD/ITLhDyBAQxfyXXzcA62+59ToX0agyeYznjLHnJcd67YlZ8QqogdK/OFw/M2zZ0FixuIvy577d9juu0pjYC1H8ICSG9GW2U4KItSjRDaH8CrqXZtHHOpZUoIyaQGiuuUFX4YRRouNNi4X81GXnX7uTywIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        String claims = jwt.getClaims();
        System.out.println(claims);

    }
}
