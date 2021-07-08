package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.shiro.crypto.hash.Hash;

import java.util.HashMap;

@Mapper
public interface TbUserDao {
    public boolean haveRootUser();

    public int insert(HashMap param);

    public Integer searchIdByOpenId(String openId);
}