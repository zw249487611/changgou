package com.changgou.filter;

/**
 * 不需要认证就可以访问的路径校验
 */
public class URLFilter {
    private static final String allurl = "/user/login,/api/user/add";

    /**
     * 校验当前访问路径是否需要验证权限，
     * 如果不需要就，false
     */
    public static boolean hasAuthorize(String url) {
        //不需要拦截的url
        String[] urls = allurl.split(",");
        for (String uri : urls) {
            if (url.equals(uri)) {
                return false;
            }
        }
        return true;
    }
}
