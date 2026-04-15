package cn.magicclub.service;

import cn.magicclub.entity.InviteCode;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 邀请码服务接口
 */
public interface InviteCodeService extends IService<InviteCode> {

    /**
     * 获取当前有效邀请码
     */
    String getValidInviteCode();

    /**
     * 验证邀请码是否有效
     */
    boolean validateInviteCode(String code);

    /**
     * 设置新邀请码（自动作废旧邀请码）
     */
    void setNewInviteCode(String code, Long adminId);

    /**
     * 作废当前邀请码
     */
    void invalidateCurrentCode(String reason, Long adminId);
}
