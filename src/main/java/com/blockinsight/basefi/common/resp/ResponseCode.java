package com.blockinsight.basefi.common.resp;

/**
 * 返回码
 *
 */
public enum ResponseCode
{
    /**
     * 请求成功
     */
    SUCCESS(0, "成功"),
    /**
     * 未知错误
     */
    UNKNOWN_EXCEPTION(1400, "系统错误，请联系管理员");



    private int code;
    private String messageKey;

    ResponseCode(int code, String message)
    {
        this.code = code;
        this.messageKey = message;
    }

    public int getCode()
    {
        return code;
    }

    public String getMessageKey()
    {
        return messageKey;
    }

    public String toJson(String message)
    {
        return "{ \"code\":" + this.getCode() + ",\"message\":\"" + message + "\"}";
    }
}
