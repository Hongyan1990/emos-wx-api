package com.example.emos.wx.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.appSecret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if(openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        if(registerCode.equals("000000")) {
            boolean bool = userDao.haveRootUser();
            if(!bool) {
                String openId = getOpenId(code);
                HashMap map = new HashMap<>();
                map.put("openId", openId);
                map.put("nickname", nickname);
                map.put("photo", photo);
                map.put("role", "[0]");
                map.put("status", "1");
                map.put("createTime", new Date());
                map.put("root", true);
                userDao.insert(map);
                int id = userDao.searchIdByOpenId(openId);
                return id;
            } else {
                throw new EmosException("无法绑定超级管理员账户");
            }
        }
        // TODO 此处还有其他判断
        else {
            return 0;
        }
        return 0;
    }
}
