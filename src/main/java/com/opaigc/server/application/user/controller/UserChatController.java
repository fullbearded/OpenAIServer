package com.opaigc.server.application.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.opaigc.server.application.user.domain.User;
import com.opaigc.server.application.user.service.UserChatService;
import com.opaigc.server.application.user.service.UserService;
import com.opaigc.server.infrastructure.auth.AccountSession;
import com.opaigc.server.infrastructure.http.ApiResponse;
import com.opaigc.server.infrastructure.utils.PageUtil;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/9
 */
@Slf4j
@RestController
@RequestMapping("/api/user-chat")
public class UserChatController {

    @Autowired
    private UserService userService;

    /**
     * 获取聊天列表
     **/
    @PostMapping("/list")
    public ApiResponse list(@RequestBody UserChatService.ListParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        Optional<User> user = userService.getByCode(session.getCode());
        if (user.isPresent() && user.get().isAdmin()) {
            return ApiResponse.success(userService.userChatList(req, user.get()));
        } else {
            return ApiResponse.success();
        }
    }

    /**
     * 获取聊天分页
     **/
    @PostMapping("/page")
    public ApiResponse page(@RequestBody UserChatService.PageParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        Optional<User> user = userService.getByCode(session.getCode());
        if (user.isPresent() && user.get().isAdmin()) {
            return ApiResponse.success(PageUtil.convert(userService.userChatPage(req, user.get())));
        } else {
            return ApiResponse.success();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateParam {
        private String name;
    }
}
