package com.blockinsight.basefi.common.resp;

public enum ErrorStatus {
    /**
     * 请求无效
     */
    INVALID_REQUEST(200,"Invalid request"),
    /**
     * 无效的编辑
     */
    INVALID_EDIT(200,"Invalid edit"),
    /**
     * 重复请求
     */
    HAVE_PLEDGE(200,"Repeated pledge"),
    /**
     * 服务器正忙
     */
    SYSTEM_BUSY(200,"Server is busy");
    private int code;
    private String msg;

    ErrorStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
