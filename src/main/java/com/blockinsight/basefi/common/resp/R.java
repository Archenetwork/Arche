/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.zrbx.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.blockinsight.basefi.common.resp;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 */
public class R extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    public R()
    {
        put("code", 200);
        put("msg", "success");
    }

    public static R error()
    {
        return error(ResponseCode.UNKNOWN_EXCEPTION);
    }

    public static R error(String msg)
    {
        return error(600, msg);
    }

    public static R error(int code, String msg)
    {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R error(ResponseCode responseCode)
    {
        R r = new R();
        r.put("code", responseCode.getCode());
        r.put("msg", responseCode.getMessageKey());
        return r;
    }

    public static R ok(String msg)
    {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map)
    {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok()
    {
        return new R();
    }

    public static R ok(ResponseCode responseCode)
    {
        R r = new R();
        r.put("code", responseCode.getCode());
        r.put("msg", responseCode.getMessageKey());
        return r;
    }

    @Override
    public R put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
