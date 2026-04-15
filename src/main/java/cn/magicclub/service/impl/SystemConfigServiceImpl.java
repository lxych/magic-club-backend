package cn.magicclub.service.impl;

import cn.magicclub.entity.SystemConfig;
import cn.magicclub.mapper.SystemConfigMapper;
import cn.magicclub.service.SystemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getConfigValue(String key, String defaultValue) {
        SystemConfig config = baseMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public void setConfigValue(String key, String value) {
        SystemConfig config = baseMapper.selectByKey(key);
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            save(config);
        } else {
            config.setConfigValue(value);
            updateById(config);
        }
    }

    @Override
    public boolean isGlobalMute() {
        String value = getConfigValue("global_mute", "false");
        return "true".equals(value);
    }

    @Override
    public void setGlobalMute(boolean mute) {
        setConfigValue("global_mute", String.valueOf(mute));
    }
}
