package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 登录实现
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception {
        //调用的请求地址
//        String url = "http://localhost:9001/oauth/token";

        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        String url =  serviceInstance.getUri() + "/oauth/token";

        MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String,String>();
        multiValueMap.add("username", username);
        multiValueMap.add("password", password);
        multiValueMap.add("grant_type", grant_type);

        String Authorization = "Basic " + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
        MultiValueMap headerMap = new LinkedMultiValueMap();
        headerMap.add("Authorization", Authorization);

        HttpEntity httpEntity = new HttpEntity(multiValueMap, headerMap);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        System.out.println(response.getBody());


        Map<String,String> map = response.getBody();
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token"));
        authToken.setRefreshToken(map.get("refresh_token"));
        authToken.setJti(map.get("jti"));
        return authToken;
    }
}
