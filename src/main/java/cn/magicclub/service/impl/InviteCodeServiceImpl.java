package cn.magicclub.service.impl;

import cn.magicclub.entity.InviteCode;
import cn.magicclub.mapper.InviteCodeMapper;
import cn.magicclub.service.InviteCodeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 邀请码服务实现
 */
@Service
@RequiredArgsConstructor
public class InviteCodeServiceImpl extends ServiceImpl<InviteCodeMapper, InviteCode> implements InviteCodeService {

    @Override
    public String getValidInviteCode() {
        InviteCode code = baseMapper.selectValidCode();
        return code != null ? code.getCode() : null;
    }

    @Override
    public boolean validateInviteCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        String validCode = getValidInviteCode();
        return code.equals(validCode);
    }

    @Override
    @Transactional
    public void setNewInviteCode(String code, Long adminId) {
        // 作废旧邀请码
        lambdaUpdate()
            .set(InviteCode::getStatus, 0)
            .set(InviteCode::getInvalidTime, LocalDateTime.now())
            .set(InviteCode::getInvalidReason, "管理员更换邀请码")
            .eq(InviteCode::getStatus, 1)
            .update();
        
        // 创建新邀请码
        InviteCode newCode = new InviteCode();
        newCode.setCode(code);
        newCode.setStatus(1);
        newCode.setCreateBy(adminId);
        newCode.setCreateTime(LocalDateTime.now());
        save(newCode);
    }

    @Override
    @Transactional
    public void invalidateCurrentCode(String reason, Long adminId) {
        lambdaUpdate()
            .set(InviteCode::getStatus, 0)
            .set(InviteCode::getInvalidTime, LocalDateTime.now())
            .set(InviteCode::getInvalidReason, reason)
            .eq(InviteCode::getStatus, 1)
            .update();
    }
}
