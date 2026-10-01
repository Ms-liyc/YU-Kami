package com.yuchen.kami.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuchen.kami.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
