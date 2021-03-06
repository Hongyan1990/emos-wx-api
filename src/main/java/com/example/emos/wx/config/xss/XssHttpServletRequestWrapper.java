package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if(!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if(values != null) {
            for(int i=0; i<values.length; i++) {
                String value = values[i];
                if(!StrUtil.hasEmpty(value)) {
                    values[i] = HtmlUtil.filter(value);
                }
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameter = super.getParameterMap();
        LinkedHashMap<String, String[]> result = new LinkedHashMap<String, String[]>();
        if(parameter != null) {
            for(String key : parameter.keySet()) {
                String[] values = parameter.get(key);
                if(values != null) {
                    for(int i=0; i<values.length; i++) {
                        String value = values[i];
                        if(!StrUtil.hasEmpty(value)) {
                            values[i] = HtmlUtil.filter(value);
                        }
                    }
                }
                result.put(key, values);
            }
        }
        return result;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if(!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();
        while(line != null) {
            body.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();
        in.close();
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        Map<String, Object> result = new HashMap<>();
        for(String key : map.keySet()) {
            Object val = map.get(key);
            if(val instanceof String) {
                result.put(key, HtmlUtil.filter(val.toString()));
            } else {
                result.put(key, val);
            }
        }
        String jsonStr = JSONUtil.toJsonStr(result);
        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonStr.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }
}
