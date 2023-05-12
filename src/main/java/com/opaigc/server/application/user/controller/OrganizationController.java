package com.opaigc.server.application.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.opaigc.server.application.user.domain.Organization;
import com.opaigc.server.application.user.service.OrganizationService;
import com.opaigc.server.infrastructure.auth.AccountSession;
import com.opaigc.server.infrastructure.http.ApiResponse;

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
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取APP列表
     **/
    @PostMapping("/list")
    public ApiResponse list(@NotNull(message = "请登录后再操作") AccountSession session) {
        return ApiResponse.success(organizationService.list());
    }

    /**
     * APP 创建
     **/
    @PostMapping("/create")
    public ApiResponse create(@RequestBody CreateParam req, @NotNull(message = "请登录后再操作") AccountSession session) {
		Organization organization = Organization.builder().name(req.getName()).build();
        return ApiResponse.success(organizationService.save(organization));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateParam {
		private String name;
    }
}
