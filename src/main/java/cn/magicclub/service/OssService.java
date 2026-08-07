package cn.magicclub.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 阿里云 OSS 服务
 * <p>
 * 负责大文件（超过阈值）上传到 OSS，以及生成签名预览/下载链接。
 * AccessKey 通过 application.yml 注入，不在代码中硬编码。
 *
 * @author demo
 * @since 2025-08-05
 */
@Slf4j
@Service
public class OssService {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.file-size-threshold:52428800}")
    private long fileSizeThreshold;

    @Value("${oss.role-arn:}")
    private String roleArn;

    @Value("${oss.role-session-name:oss-upload-session}")
    private String roleSessionName;

    @Value("${oss.sts-endpoint:sts.cn-beijing.aliyuncs.com}")
    private String stsEndpoint;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("OSS 客户端初始化完成: endpoint={}, bucket={}", endpoint, bucket);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已关闭");
        }
    }

    /**
     * 获取文件大小分流阈值
     */
    public long getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    /**
     * 上传文件到 OSS
     *
     * @param file 上传文件
     * @return OSS 文件 URL（https://bucket.endpoint/objectKey）
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String objectKey = generateObjectKey(file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucket, objectKey, inputStream, metadata);
        }

        String fileUrl = "https://" + bucket + "." + endpoint + "/" + objectKey;
        log.info("OSS 上传成功: {} -> {}", file.getOriginalFilename(), fileUrl);
        return fileUrl;
    }

    /**
     * 生成签名预览链接（有效期 1 小时）
     *
     * @param ossUrl OSS 文件 URL
     * @return 签名 URL
     */
    public String getPreviewUrl(String ossUrl) {
        String objectKey = extractObjectKey(ossUrl);
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        URL url = ossClient.generatePresignedUrl(bucket, objectKey, expiration);
        return url.toString();
    }

    /**
     * 生成签名下载链接（带 Content-Disposition: attachment，有效期 1 小时）
     *
     * @param ossUrl   OSS 文件 URL
     * @param fileName 下载时显示的文件名
     * @return 签名 URL
     */
    public String getDownloadUrl(String ossUrl, String fileName) {
        String objectKey = extractObjectKey(ossUrl);
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey);
        request.setExpiration(expiration);
        request.addHeader("response-content-disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\"");

        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 删除 OSS 对象
     *
     * @param ossUrl OSS 文件 URL
     */
    public void deleteFile(String ossUrl) {
        String objectKey = extractObjectKey(ossUrl);
        ossClient.deleteObject(bucket, objectKey);
        log.info("OSS 对象已删除: {}", ossUrl);
    }

    /**
     * 从 OSS URL 中提取对象 Key
     * <p>
     * URL 格式: https://bucket.endpoint/objectKey
     */
    private String extractObjectKey(String ossUrl) {
        try {
            URL url = new URL(ossUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            throw new RuntimeException("无效的 OSS URL: " + ossUrl, e);
        }
    }

    /**
     * 生成 STS Token（供前端直传 OSS 使用）
     * <p>
     * 如果配置了 RAM 角色 ARN，则通过 AssumeRole 生成临时凭证；
     * 否则回退到基于主账号 AK 签发 Session Token。
     * <p>
     * ⚠️ 建议在阿里云 RAM 中创建专用角色（如 oss-upload-role），
     * 授予 OSS 写入权限，并配置 trust policy 允许当前账号代入。
     *
     * @return STS 凭证 Map（accessKeyId, accessKeySecret, securityToken, expiration, region, bucket, endpoint）
     */
    public Map<String, String> generateStsToken() {
        Map<String, String> result = new LinkedHashMap<>();

        try {
            if (roleArn != null && !roleArn.isEmpty() && !roleArn.startsWith("acs:ram::your-account-id")) {
                // 使用 RAM 角色获取 STS Token
                IClientProfile profile = DefaultProfile.getProfile(
                        "cn-beijing", accessKeyId, accessKeySecret);
                DefaultAcsClient client = new DefaultAcsClient(profile);

                AssumeRoleRequest request = new AssumeRoleRequest();
                request.setRoleArn(roleArn);
                request.setRoleSessionName(roleSessionName);
                request.setDurationSeconds(3600L); // 1 小时

                AssumeRoleResponse response = client.getAcsResponse(request);
                AssumeRoleResponse.Credentials credentials = response.getCredentials();

                result.put("accessKeyId", credentials.getAccessKeyId());
                result.put("accessKeySecret", credentials.getAccessKeySecret());
                result.put("securityToken", credentials.getSecurityToken());
                result.put("expiration", credentials.getExpiration());

                log.info("STS Token 已生成（RAM角色）: sessionName={}, expiration={}",
                        roleSessionName, credentials.getExpiration());
            } else {
                // 回退方案：用主账号创建 STS Session Token
                // 这需要调用 GetCallerIdentity 等方式，但 aliyun-java-sdk-sts 的
                // AssumeRole 必须指定 RoleArn。这里使用 OSS SDK 的方式创建临时凭证。
                //
                // 通过 STS 的 AssumeRole 支持「扮演自身」：
                // 需要在 RAM 中创建一个角色，信任当前账号本身。
                // 如果 role-arn 未配置，返回明显提示。
                log.warn("⚠️ oss.role-arn 未配置，无法生成 STS 临时凭证。");
                log.warn("   请在阿里云 RAM 控制台创建角色（如 oss-upload-role），");
                log.warn("   授予 AliyunOSSFullAccess 或自定义 OSS 写入策略，");
                log.warn("   然后在 application.yml 中配置 oss.role-arn。");
                log.warn("   当前将返回主账号 AccessKey（仅用于开发环境）。");

                result.put("accessKeyId", accessKeyId);
                result.put("accessKeySecret", accessKeySecret);
                result.put("securityToken", "");
                result.put("expiration", "");
            }

            result.put("region", "oss-cn-beijing");
            result.put("bucket", bucket);
            result.put("endpoint", endpoint);

        } catch (Exception e) {
            log.error("生成 STS Token 失败", e);
            throw new RuntimeException("无法生成 OSS 上传凭证: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成 OSS 对象 Key（含日期路径，public 方法供 OssController 使用）
     */
    public String generateObjectKey(String originalFilename) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return datePath + "/" + uuid + extension;
    }
}
