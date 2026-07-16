package com.offerpilot.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private String title;

    private Integer salaryMin;

    private Integer salaryMax;

    private String city;

    private String education;

    private String experience;

    private String employmentType;

    private String description;

    private Integer status = 1;

    private LocalDate deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
