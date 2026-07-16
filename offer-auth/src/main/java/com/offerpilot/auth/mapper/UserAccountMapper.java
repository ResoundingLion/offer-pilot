package com.offerpilot.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offerpilot.auth.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
