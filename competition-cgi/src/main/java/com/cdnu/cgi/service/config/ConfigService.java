package com.cdnu.cgi.service.config;

import com.cdnu.cgi.config.AIServiceConfig;
import com.cdnu.cgi.config.ParticipateConfig;
import com.cdnu.cgi.config.UserConfig;
import org.springframework.stereotype.Service;

@Service
public interface ConfigService {
    /**
     * 根据key获取value
     *
     * @param configKey configKey
     * @return String
     */
    String getConfigValueByConfigKey(String configKey);


    /**
     * 获取参赛配置
     *
     * @return ParticipateConfig
     */
    ParticipateConfig getParticipateConfig();


    /**
     * 获取参赛配置
     *
     * @return ParticipateConfig
     */
    AIServiceConfig getAIServiceConfig();

    /**
     *  获取用户配置
     *
     * @return UserConfig
     */
    UserConfig getUserConfig();
}
