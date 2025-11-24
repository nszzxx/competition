package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.config.GlobalConfig;

public interface ConfigMapper extends BaseMapper<GlobalConfig>  {
    /**
     * 根据configKey获取configValue
     *
     * @param configKey configKey
     * @return String
     */
    String selectValueByConfigKey(String configKey);

}
