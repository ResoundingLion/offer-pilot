package com.offerpilot.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息
 * <p>
 * role: USER / ASSISTANT / TOOL_USE / TOOL_RESULT
 * tool_name / tool_args / tool_result 只在 TOOL_USE 和 TOOL_RESULT 时有值
 */
@Data
@TableName("conversation_message")
public class ConversationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    /** USER / ASSISTANT / TOOL_USE / TOOL_RESULT */
    private String role;

    /** 文本内容 */
    private String content;

    /** LLM 调用的工具名 */
    private String toolName;

    /** 工具参数（JSON） */
    private String toolArgs;

    /** 工具执行结果（JSON） */
    private String toolResult;

    /** 消息序号 */
    private Integer msgIndex;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
