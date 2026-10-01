package com.yuchen.kami.service;

import com.yuchen.kami.entity.AuditLog;
import com.yuchen.kami.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    @Async
    public void log(Long userId, String username, String action, String target, String detail, String ip) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setTarget(target);
        log.setDetail(detail);
        log.setIp(ip);
        auditLogMapper.insert(log);
    }
}
