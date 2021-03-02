package com.blockinsight.basefi.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class Ticker {
    private String status;
    private String ts;
    private List<DataDto> data;
}
