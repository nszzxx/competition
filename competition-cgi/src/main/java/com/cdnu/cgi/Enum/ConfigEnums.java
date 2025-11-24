package com.cdnu.cgi.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConfigEnums {
    PARTICIPATE_CONFIG("PARTICIPATE_CONFIG", "参赛配置"),
    TEAM_CONFIG("TEAM_CONFIG", "团队配置"),
    SYSTEM_CONFIG("SYSTEM_CONFIG", "系统配置"),
    AI_SERVICE_CONFIG("AI_SERVICE_CONFIG", "AI服务配置"),
    USER_CONFIG("USER_CONFIG", "用户配置");

    private final String key;
    private final String description;
}
