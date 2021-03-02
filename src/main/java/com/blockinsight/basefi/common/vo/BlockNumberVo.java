package com.blockinsight.basefi.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BlockNumberVo implements Serializable {
    private String jsonrpc;
    private int id;
    private String result;
}
