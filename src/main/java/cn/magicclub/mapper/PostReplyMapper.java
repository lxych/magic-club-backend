package cn.magicclub.mapper;

import cn.magicclub.entity.PostReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子回复Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface PostReplyMapper extends BaseMapper<PostReply> {

    /**
     * 查询帖子的所有回复
     */
    @Select("SELECT * FROM mc_post_reply WHERE post_id = #{postId} AND deleted = 0 ORDER BY create_time ASC")
    List<PostReply> selectByPostId(@Param("postId") Long postId);

    /**
     * 查询楼中楼回复
     */
    @Select("SELECT * FROM mc_post_reply WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY create_time ASC")
    List<PostReply> selectByParentId(@Param("parentId") Long parentId);
}
