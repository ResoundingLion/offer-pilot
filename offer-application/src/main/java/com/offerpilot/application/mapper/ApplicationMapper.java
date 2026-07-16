package com.offerpilot.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offerpilot.application.entity.Application;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

}
