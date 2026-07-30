package com.offerpilot.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.offerpilot.ai.entity.Conversation;
import com.offerpilot.ai.entity.ConversationMessage;
import com.offerpilot.ai.mapper.ConversationMapper;
import com.offerpilot.ai.mapper.ConversationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话管理服务 —— 持久化对话历史和构建 LLM 消息列表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper messageMapper;

    // ==================== 对话 CRUD ====================

    /**
     * 创建新对话
     */
    @Transactional
    public Conversation createConversation(Long userId, String title) {
        Conversation conv = new Conversation();
        conv.setUserId(userId);
        conv.setTitle(title != null && title.length() > 30 ? title.substring(0, 30) : title);
        conversationMapper.insert(conv);
        return conv;
    }

    /**
     * 获取用户的对话列表（按更新时间倒序）
     */
    public List<Conversation> getUserConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getUpdatedAt)
        );
    }

    /**
     * 根据 ID 获取对话
     */
    public Conversation getConversation(Long id) {
        return conversationMapper.selectById(id);
    }

    /**
     * 更新对话标题
     */
    @Transactional
    public void updateConversationTitle(Long id, String title) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setTitle(title);
        conversationMapper.updateById(conv);
    }

    /**
     * 删除对话及其所有消息
     */
    @Transactional
    public void deleteConversation(Long id) {
        messageMapper.delete(new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getConversationId, id));
        conversationMapper.deleteById(id);
    }

    // ==================== 消息管理 ====================

    /**
     * 添加一条消息
     */
    @Transactional
    public ConversationMessage addMessage(Long conversationId, String role, String content,
                                          String toolName, String toolArgs, String toolResult) {
        // 查询当前最大序号（取最后一条消息的序号 + 1）
        ConversationMessage lastMsg = messageMapper.selectOne(
                new LambdaQueryWrapper<ConversationMessage>()
                        .eq(ConversationMessage::getConversationId, conversationId)
                        .orderByDesc(ConversationMessage::getMsgIndex)
                        .last("LIMIT 1")
        );
        int nextIndex = (lastMsg != null ? lastMsg.getMsgIndex() : 0) + 1;

        ConversationMessage msg = new ConversationMessage();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setToolName(toolName);
        msg.setToolArgs(toolArgs);
        msg.setToolResult(toolResult);
        msg.setMsgIndex(nextIndex);
        messageMapper.insert(msg);

        // 更新对话时间
        conversationMapper.update(null, new LambdaUpdateWrapper<Conversation>()
                .eq(Conversation::getId, conversationId)
                .set(Conversation::getUpdatedAt, LocalDateTime.now()));

        return msg;
    }

    /**
     * 获取对话的完整消息历史（按序号排序）
     */
    public List<ConversationMessage> getHistory(Long conversationId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ConversationMessage>()
                        .eq(ConversationMessage::getConversationId, conversationId)
                        .orderByAsc(ConversationMessage::getMsgIndex)
        );
    }
}
