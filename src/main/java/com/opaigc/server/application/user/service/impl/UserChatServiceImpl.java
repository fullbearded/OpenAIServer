package com.opaigc.server.application.user.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opaigc.server.application.user.domain.UserChat;
import com.opaigc.server.application.user.mapper.UserChatMapper;
import com.opaigc.server.application.user.service.UserChatService;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/9
 */
@Service
public class UserChatServiceImpl extends ServiceImpl<UserChatMapper, UserChat> implements UserChatService {

    @Override
    public Long todayUsedQuota(Long userId) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);

        return lambdaQuery().between(UserChat::getCreatedAt, startOfDay, endOfDay).eq(UserChat::getUserId, userId).count();
    }
}
