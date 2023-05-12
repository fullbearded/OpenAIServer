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

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
     * 获取用户信息
     **/
    @PostMapping("/info")
    public ApiResponse info(@NotNull(message = "请登录后再操作") AccountSession session) {
        UserService.UserMemberDTO userMemberDTO = userService.getUserInfo(session.getCode());
        UserService.UserInfoDTO userInfoDTO = new UserService.UserInfoDTO();
        BeanUtils.copyProperties(userMemberDTO, userInfoDTO);
        return ApiResponse.success(userInfoDTO);
    }


    @PostMapping("/create")
    public ApiResponse create(@NotNull(message = "请登录后再操作") AccountSession session,
                              @RequestBody UserCreateParam req) {

        User admin = userService.getByCode(session.getCode()).get();
        if (!admin.getUserType().equals(User.UserType.ADMIN)) {
            throw new AppException("001","非管理员不允许创建用户");
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
    public static class UserCreateParam {
        private String username;

        private String mobile;

        private String password;

        private String registerIp;

        private String remark;

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
