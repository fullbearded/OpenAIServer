package com.opaigc.server.application.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opaigc.server.application.user.domain.App;
import com.opaigc.server.application.user.domain.UserChat;
import com.opaigc.server.application.user.mapper.UserChatMapper;
import com.opaigc.server.application.user.service.AppService;
import com.opaigc.server.application.user.service.UserChatService;
import com.opaigc.server.infrastructure.utils.PageUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/9
 */
@Service
public class UserChatServiceImpl extends ServiceImpl<UserChatMapper, UserChat> implements UserChatService {

	@Autowired
	private UserChatMapper userChatMapper;

	@Override
	public Long todayUsedQuota(Long userId) {
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);

		return lambdaQuery().between(UserChat::getCreatedAt, startOfDay, endOfDay).eq(UserChat::getUserId, userId).count();
	}

	@Override
	public List<UserChat> list(ListParam req) {
		return lambdaQuery().orderByDesc(UserChat::getCreatedAt)
				.eq(Objects.nonNull(req.getCategory()), UserChat::getCategory, req.getCategory())
				.eq(Objects.nonNull(req.getAppId()), UserChat::getAppId, req.getAppId())
				.eq(Objects.nonNull(req.getUserId()), UserChat::getUserId, req.getUserId())
				.list();
	}

	@Override
	public Page<UserChat> page(PageParam req) {
		PageUtil pageUtil = new PageUtil(req.getPage(), req.getPerPage());
		Page<UserChat> page = new Page<>(pageUtil.getPage(), pageUtil.getPerPage());
		QueryWrapper<UserChat> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(Objects.nonNull(req.getUserId()), "user_id", req.getUserId());
		queryWrapper.eq(Objects.nonNull(req.getAppId()), "app_id", req.getAppId());
		queryWrapper.eq(Objects.nonNull(req.getCategory()), "category", req.getCategory());
		queryWrapper.orderByDesc("created_at");
		Page<UserChat> appPage = userChatMapper.selectPage(page, queryWrapper);
		return appPage;
	}
}
