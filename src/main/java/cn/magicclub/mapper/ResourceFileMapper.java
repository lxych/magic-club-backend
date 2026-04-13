package cn.magicclub.mapper;

import cn.magicclub.entity.ResourceFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 资源文件Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface ResourceFileMapper extends BaseMapper<ResourceFile> {

    /**
     * 增加下载次数
     */
    @Update("UPDATE mc_resource SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);
}
