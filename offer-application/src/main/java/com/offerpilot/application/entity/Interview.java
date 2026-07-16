package com.offerpilot.application.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.offerpilot.application.enums.InterviewResult;
import com.offerpilot.application.enums.InterviewRound;
import com.offerpilot.application.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试记录 —— 关联投递，一次投递可有多轮面试
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联投递 ID
     */
    private Long applicationId;

    /**
     * 面试轮次（一面/二面/三面/四面/HR面）
     */
    private InterviewRound round;

    /**
     * 面试时间
     */
    private LocalDateTime scheduledAt;

    /**
     * 面试形式（线上/线下/电话）
     */
    private InterviewType interviewType;

    /**
     * 面试地点或会议链接
     */
    private String location;

    /**
     * 面试官
     */
    private String interviewer;

    /**
     * 面试结果（待定/通过/未通过）
     */
    private InterviewResult result;

    /**
     * 面试反馈/面经
     */
    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
