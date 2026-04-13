package cn.magicclub.mapper;

import cn.magicclub.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 增加浏览数
     */
    @Update("UPDATE mc_post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    @Update("UPDATE mc_post SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 增加回复数
     */
    @Update("UPDATE mc_post SET reply_count = reply_count + 1 WHERE id = #{id}")
    void incrementReplyCount(@Param("id") Long id);
}
