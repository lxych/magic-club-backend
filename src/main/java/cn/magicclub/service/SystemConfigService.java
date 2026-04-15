package cn.magicclub.service;

import cn.magicclub.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 获取配置值
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * 设置配置值
     */
    void setConfigValue(String key, String value);

    /**
     * 获取全员禁言状态
     */
    boolean isGlobalMute();

    /**
     * 设置全员禁言状态
     */
    void setGlobalMute(boolean mute);
}
