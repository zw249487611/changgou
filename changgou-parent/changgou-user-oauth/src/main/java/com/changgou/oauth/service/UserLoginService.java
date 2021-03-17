package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

import java.io.UnsupportedEncodingException;

public interface UserLoginService {
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws UnsupportedEncodingException, Exception;
}
