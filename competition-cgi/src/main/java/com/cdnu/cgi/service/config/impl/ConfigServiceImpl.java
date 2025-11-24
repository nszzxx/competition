package com.cdnu.cgi.service.config.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cdnu.cgi.Enum.ConfigEnums;
import com.cdnu.cgi.config.AIServiceConfig;
import com.cdnu.cgi.config.UserConfig;
import com.cdnu.cgi.mapper.ConfigMapper;
import com.cdnu.cgi.service.config.ConfigService;
import com.cdnu.cgi.config.ParticipateConfig;
import com.cdnu.cgi.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class ConfigServiceImpl implements ConfigService {
    private final ConfigMapper configMapper;

    @Override
    public String getConfigValueByConfigKey(String configKey) {
        String configValue = configMapper.selectValueByConfigKey(configKey);
        if (StringUtils.isBlank(configValue)) {
            throw new RuntimeException(String.format("配置[%s]不存在", configKey));
        }
        return configValue;
    }

    @Override
    public ParticipateConfig getParticipateConfig() {
        String configValue = getConfigValueByConfigKey(ConfigEnums.PARTICIPATE_CONFIG.getKey());
        try {
            return JsonUtils.create().fromJson(configValue, ParticipateConfig.class);
        } catch (Exception e) {
            log.error("解析参赛配置失败", e);
            throw e;
        }
    }

    @Override
    public AIServiceConfig getAIServiceConfig() {
        String configValue = getConfigValueByConfigKey(ConfigEnums.AI_SERVICE_CONFIG.getKey());
        try {
            return JsonUtils.create().fromJson(configValue, AIServiceConfig.class);
        } catch (Exception e) {
            log.error("解析AI服务配置失败", e);
            throw e;
        }
    }

    @Override
    public UserConfig getUserConfig() {
        String configValue = getConfigValueByConfigKey(ConfigEnums.USER_CONFIG.getKey());
        try {
            return JsonUtils.create().fromJson(configValue, UserConfig.class);
        } catch (Exception e) {
            log.error("解析用户配置失败", e);
            throw e;
        }
    }
}
