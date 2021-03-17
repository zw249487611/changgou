package com.itheima.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;

/**
 * 令牌的乘谷城和解析
 */
public class JwtTest {
    /**
     * 创建令牌
     */
    @Test
    public void testCreateToken() {
        //构建jwt令牌的对象
        JwtBuilder builder = Jwts.builder();
        builder.setIssuer("黑马训练营");//颁发者
        builder.setIssuedAt(new Date());//颁发时间
        builder.setExpiration(new Date(System.currentTimeMillis() + 30000));
        builder.setSubject("Jwt令牌测试");//主题信息
        builder.signWith(SignatureAlgorithm.HS256, "itcast");//1签名算法，2、密钥（盐）
        String token = builder.compact();
        System.out.println(token);
    }

    /**
     * 解析令牌
     */
    @Test
    public void parseToken() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiLpu5Hpqazorq3nu4PokKUiLCJpYXQiOjE2MTU0NzA1NTcsImV4cCI6MTYxNTQ3MDU4Nywic3ViIjoiSnd05Luk54mM5rWL6K-VIn0.yIuZI4aQMjCMpQs-Mc8OHue3qn91FFenNrtu2MQnWG4";
        Claims claims = Jwts.parser()
                .setSigningKey("itcast")
                .parseClaimsJws(token)
                .getBody();
        System.out.println(claims.toString());
    }
}
