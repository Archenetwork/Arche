package com.blockinsight.basefi.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultVo implements Serializable {
    private String address;
    private String[] topics;
    private String data;
    private String blockNumber;
    private String timeStamp;
    private String gasPrice;
    private String gasUsed;
    private String logIndex;
    private String transactionHash;
    private String transactionIndex;
}
