package com.opaigc.server.application.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.opaigc.server.application.user.domain.App;
import com.opaigc.server.application.user.domain.Organization;
import com.opaigc.server.application.user.service.AppService;
import com.opaigc.server.application.user.service.OrganizationService;
import com.opaigc.server.application.user.service.UserChatService;
import com.opaigc.server.infrastructure.auth.AccountSession;
import com.opaigc.server.infrastructure.http.ApiResponse;
import com.opaigc.server.infrastructure.utils.PageUtil;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述
 *
 * @author huhongda@fiture.com
 * @date 2023/4/9
 */
@Slf4j
@RestController
@RequestMapping("/api/user-chat")
public class UserChatController {

    @Autowired
    private UserChatService userChatService;

    /**
     * 获取聊天列表
     **/
    @PostMapping("/list")
    public ApiResponse list(@RequestBody UserChatService.ListParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        return ApiResponse.success(userChatService.list(req));
    }

    /**
     * 获取聊天分页
     **/
    @PostMapping("/page")
    public ApiResponse page(@RequestBody UserChatService.PageParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        return ApiResponse.success(PageUtil.convert(userChatService.page(req)));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateParam {
		private String name;
    }
}
