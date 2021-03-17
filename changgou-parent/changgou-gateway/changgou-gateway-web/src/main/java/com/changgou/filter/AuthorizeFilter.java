package com.changgou.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器，
 * 实现用户权限的鉴别（校验
 *      因为在用户微服务中，已经将用户的token等信息加入并传给了response,并添加到了cookie中
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌的名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    /**
     * 全局拦截
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //用户如果是登录或者一些不需要做权限认证的请求，直接放行
        String uri = request.getURI().toString();
        if (URLFilter.hasAuthorize(uri)) {
            return chain.filter(exchange);
        }
        //获取用户令牌信息
            //1）先从头中获取看看
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //Boolean,true : 令牌在头文件中，，false:令牌不在头文件中-》将令牌封装到头文件中，再传递给其他微服务
        boolean hasToken = true;
            //2)如果头中没有，则看看参数里有没有令牌
        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
            //3)如果参数里还没有，则取cookie中看看有没有，如果还没有的话，说明真的没有了
        if (StringUtils.isEmpty(token)) {
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (httpCookie != null) {
                token = httpCookie.getValue();
            }
        }
        //如果没有令牌，则拦截
        if (StringUtils.isEmpty(token)) {
            //设置没有权限的状态码，401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();
        }
        //如果有令牌，则校验是u否有效
        /*try {
//            JwtUtil.parseJWT(token);
            //放行
//            return chain.filter(exchange);
        } catch (Exception e) {
            //无效则拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();
        }*/
        //令牌判断是否为空，如果不为空，将令牌放到头文件中，放行

        if (StringUtils.isEmpty(token)) {
            //无效则拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();
        } else {
            if (!hasToken) {
                //判断当前令牌是否有bearer 前缀，如果没有，则添加前缀bearer
                if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")) {
                    token = "bearer " + token;
                }
                //将令牌封装到头文件中
                request.mutate().header(AUTHORIZE_TOKEN, token);
            }
        }
        //无效，则拦截

            //有效，则放行
        return chain.filter(exchange);
    }

    /**
     * 排序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
