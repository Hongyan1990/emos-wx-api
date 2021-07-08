package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalToken {
    private ThreadLocal<String> local = new ThreadLocal<String>();

    public void setToken(String token) {
        local.set(token);
    }

    public String getToken() {
        return local.get();
    }

    public void clearToken() {
        local.remove();
    }
}
