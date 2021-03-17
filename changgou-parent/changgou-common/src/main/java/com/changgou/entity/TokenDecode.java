package com.changgou.entity;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenDecode {
    //公钥
    private static final String PUBLIC_KEY = "public.key";

    private static String publickey = "";

    /**
     * 获取用户信息
     */
    public static Map<String, String> getUserInfo() {
        //获取用户授权信息
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        //令牌节码
        return decode(details.getTokenValue());
    }

    /**
     * 读取令牌数据
     * @param
     * @return
     */
    private static Map<String, String> decode(String token) {
        //校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));

        //获取jwt原始内容
        String claims = jwt.getClaims();
        return JSON.parseObject(claims, Map.class);
    }

    /**
     * 获取非对称加密公钥，key
     * @return
     */
    private static String getPubKey() {
        if (!StringUtils.isEmpty(publickey)) {
            return publickey;
        }
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader bf = new BufferedReader(inputStreamReader);
            publickey = bf.lines().collect(Collectors.joining("\n"));
            return publickey;
        } catch (IOException e) {
            return null;
        }
    }
}
