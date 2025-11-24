package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.AIChatLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI聊天日志数据访问接口
 */
@Mapper
public interface AIChatLogMapper extends BaseMapper<AIChatLog> {

    /**
     * 根据用户ID和组ID查找聊天记录
     */
    List<AIChatLog> selectByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") String groupId);

    /**
     * 根据用户ID和组ID删除聊天记录
     */
    int deleteByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") String groupId);

    /**
     * 获取用户的聊天历史分组信息
     */
    List<Map<String, Object>> selectChatHistoryGroupsByUserId(Long userId);

    /**
     * 批量查询用户多个对话组的聊天记录
     */
    List<AIChatLog> selectBatchByUserIdAndGroupIds(@Param("userId") Long userId, @Param("groupIds") List<String> groupIds);

    /**
     * 删除用户的所有聊天记录
     */
    int deleteByUserId(Long userId);

    /**
     * 保存聊天记录
     */
    int insert(AIChatLog chatLog);
}
