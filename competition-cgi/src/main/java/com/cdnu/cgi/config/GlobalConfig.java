package com.cdnu.cgi.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class GlobalConfig {
    private String id;
    private String configKey;
    private String configValue;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
