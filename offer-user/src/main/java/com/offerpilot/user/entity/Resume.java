package com.offerpilot.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * 简历标题，同标题=同一份简历的不同版本
     */
    private String title;

    /**
     * 版本号，同标题下自增
     */
    private Integer version;

    /**
     * 简历内容（结构化 JSON）
     */
    private String content;

    /**
     * 上传的简历文件 URL（MinIO）
     */
    private String fileUrl;

    /**
     * 简历摘要（AI 生成，预留）
     */
    private String summary;

    /**
     * 是否当前使用版本
     */
    private Boolean isCurrent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
