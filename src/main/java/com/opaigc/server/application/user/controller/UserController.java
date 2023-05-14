package com.opaigc.server.application.user.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.opaigc.server.application.user.domain.Member;
import com.opaigc.server.application.user.domain.User;
import com.opaigc.server.application.user.service.MemberService;
import com.opaigc.server.application.user.service.UserService;
import com.opaigc.server.config.AppConfig;
import com.opaigc.server.infrastructure.auth.AccountSession;
import com.opaigc.server.infrastructure.exception.AppException;
import com.opaigc.server.infrastructure.http.ApiResponse;
import com.opaigc.server.infrastructure.utils.CodeUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AppConfig appConfig;

    /**
     * 获取用户列表
     **/
    @PostMapping("/list")
    public ApiResponse list(@RequestBody UserService.ListParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        req.setOrganizationId(session.getOrganizationId());
        List<UserService.UserMemberDTO> list = userService.list(req);
        return ApiResponse.success(list);
    }

    /**
     * 获取用户信息
     **/
    @PostMapping("/info")
    public ApiResponse info(@NotNull(message = "请登录后再操作") AccountSession session) {
        UserService.UserMemberDTO userMemberDTO = userService.getUserInfo(session.getCode());
        UserService.UserInfoDTO userInfoDTO = new UserService.UserInfoDTO();
        BeanUtils.copyProperties(userMemberDTO, userInfoDTO);
        return ApiResponse.success(userInfoDTO);
    }

    /**
     * 修改用户密码
     **/
    @PostMapping("/password/change")
    public ApiResponse passwordChange(@RequestBody @Valid UserPasswordChangeParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        userService.passwordChange(req);
        return ApiResponse.success();
    }

    /**
     * 修改用户
     **/
    @PostMapping("/update")
    public ApiResponse update(@RequestBody @Valid UserService.UserUpdateParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
        userService.update(req);
        return ApiResponse.success();
    }

    @PostMapping("/create")
    public ApiResponse create(@NotNull(message = "请登录后再操作") AccountSession session,
                              @RequestBody UserCreateParam req) {

        User admin = userService.getByCode(session.getCode()).get();
        if (!admin.getUserType().equals(User.UserType.SUPER_ADMIN)) {
            throw new AppException("001","非超级管理员不允许创建用户");
        }

        String password = Optional.ofNullable(req.getPassword()).orElse("V123456@a");
        String username = Optional.ofNullable(req.getUsername()).orElse(CodeUtil.generateNewCode("CCST", 10));

        User user = User.builder().mobile(Optional.ofNullable(req.getMobile()).orElse(CodeUtil.generateNewCode("0", 11)))
                .username(username)
                .password(passwordEncoder.encode(password))
                .code(CodeUtil.generateRandomUserCode())
                .remark(req.getRemark())
                .registerIp(Optional.ofNullable(req.getRegisterIp()).orElse("127.0.0.1"))
                .userType(User.UserType.USER)
                .organizationId(req.getOrganizationId())
                .createdBy(session.getUsername())
                .updatedBy(session.getUsername())
                .build();

        userService.getByUsername(username).ifPresent(u -> {
            throw new AppException("001","用户名已存在");
        });

        if (userService.save(user)) {
            Member member = memberService.findOrCreateByUserId(user.getId());

            Member.Equities equities = Member.Equities.builder()
                    .gpt4(Optional.ofNullable(req.getGpt4()).orElse(false))
                    .build();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            Member updated = Member.builder().id(member.getId())
                    .dailyLimit(Optional.of(req.getDailyLimit()).orElse(appConfig.getDailyLimit()))
                    .totalQuota(req.getTotalQuota())
                    .expireDate(Optional.ofNullable(req.getExpireDate()).map(f -> LocalDateTime.parse(req.getExpireDate(), formatter))
                            .orElse(LocalDateTime.now()))
                    .equities(JSONObject.parseObject(JSONObject.toJSONString(equities)))
                    .build();
            memberService.updateById(updated);
        }
        return ApiResponse.success(user);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPasswordChangeParam {
        @NotNull(message = "不能为空")
        private String code;
        @Size(min = 6, message = "长度不能小于6位")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserCreateParam {
        private String username;

        private String mobile;

        private String password;

        private String registerIp;

        private String remark;

        private Long organizationId;
        /**
         * 会员到期日
         **/
        private String expireDate;
        /**
         * 每日限额
         **/
        private long dailyLimit;
        /**
         * 总查询额度
         **/
        private long totalQuota;
        /**
         * 是否开通GPT4
         **/
        @Builder.Default
        private Boolean gpt4 = false;
    }


}
