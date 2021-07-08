package com.example.emos.wx.config.shiro;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.catch-expire}")
    private int catchExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String token = getRequestToken(req);
        if(StringUtils.isAllBlank(token)) {
            return null;
        }
        return new OAuth2Token(token);
    }
    /**
     * 拦截请求，判断请求是否需要被shiro处理
     * */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        // 在ajax提交application/json数据的时候，会先发option请求确认，
        // 这里方形option请求，不需要shrio进行处理
        if(req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        // 除了option请求以外，其他请求都需要shrio处理
        return false;
    }
    /**
     * 该方法用于处理所有应该被shrio处理的请求
     * */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        res.setHeader("Content-Type", "text/html;charset=UTF-8");
        //允许跨域请求
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        threadLocalToken.clearToken();

        // 获取请求中的token， 如果token不存在直接返回401
        String token = getRequestToken(req);
        if(StringUtils.isAllBlank(token)) {
            res.setStatus(HttpStatus.SC_UNAUTHORIZED);
            res.getWriter().print("无效的令牌");
        }
        try {
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {
            // 客户端令牌过期，查询redis中是否存在令牌，如果存在令牌就重新生成一个令牌给客户端
            if(redisTemplate.hasKey(token)) {
                redisTemplate.delete(token); // 删除redis中令牌
                int userId = jwtUtil.getUserId(token);
                // 重新生成一个令牌
                token = jwtUtil.createToken(userId);
                // 把新的令牌保存到redis中
                redisTemplate.opsForValue().set(token, userId + "", catchExpire, TimeUnit.DAYS);
                // 然后把令牌绑定到线程
                threadLocalToken.setToken(token);
            } else {
                // 如果redis中不存令牌，让用户重新登录
                res.setStatus(HttpStatus.SC_UNAUTHORIZED);
                res.getWriter().print("令牌已经过期");
                return false;
            }
        } catch (JWTDecodeException e) {
            res.setStatus(HttpStatus.SC_UNAUTHORIZED);
            res.getWriter().print("无效的令牌");
            return false;
        }
        Boolean bool = executeLogin(servletRequest, servletResponse);
        return bool;
    }

    /**
     * 获取请求头中的token
     * */
    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if(StringUtils.isAllBlank(token)) {
            token = request.getParameter("token");
        }
        return token;
    }
}
