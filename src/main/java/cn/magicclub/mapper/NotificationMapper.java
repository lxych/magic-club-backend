package cn.magicclub.mapper;

import cn.magicclub.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 通知消息Mapper接口
 *
 * @author demo
 * @since 2025-08-05
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 将用户所有未读通知标记为已读
     */
    @Update("UPDATE mc_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int readAll(@Param("userId") Long userId);

    /**
     * 删除用户所有已读通知
     */
    @Delete("DELETE FROM mc_notification WHERE user_id = #{userId} AND is_read = 1")
    int deleteReadByUserId(@Param("userId") Long userId);
}
