package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OSS STS Token 控制器
 * <p>
 * 为前端提供临时访问凭证，实现前端直传 OSS（绕过后端中转），
 * 解决大文件（如 3GB 视频）上传超时问题。
 *
 * @author demo
 * @since 2025-08-05
 */
@Slf4j
@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;
    private final JwtUtil jwtUtil;

    /**
     * 获取 STS 临时凭证（需要登录）
     * <p>
     * 返回的凭证有效期 1 小时，前端可使用该凭证直接上传文件到 OSS。
     */
    @GetMapping("/sts-token")
    public Result<Map<String, String>> getStsToken(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        log.info("用户 {} 请求 STS Token", userId);
        Map<String, String> stsToken = ossService.generateStsToken();
        return Result.ok(stsToken);
    }

    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}
