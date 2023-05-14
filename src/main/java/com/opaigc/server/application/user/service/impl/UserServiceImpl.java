package com.opaigc.server.application.user.service.impl;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opaigc.server.application.user.controller.UserController;
import com.opaigc.server.application.user.domain.Member;
import com.opaigc.server.application.user.domain.Organization;
import com.opaigc.server.application.user.domain.User;
import com.opaigc.server.application.user.domain.UserChat;
import com.opaigc.server.application.user.mapper.UserChatMapper;
import com.opaigc.server.application.user.mapper.UserMapper;
import com.opaigc.server.application.user.service.MemberService;
import com.opaigc.server.application.user.service.OrganizationService;
import com.opaigc.server.application.user.service.UserChatService;
import com.opaigc.server.application.user.service.UserService;
import com.opaigc.server.config.AppConfig;
import com.opaigc.server.infrastructure.enums.EntityStatusEnum;
import com.opaigc.server.infrastructure.exception.AppException;
import com.opaigc.server.infrastructure.http.CommonResponseCode;
import com.opaigc.server.infrastructure.utils.CodeUtil;
import com.opaigc.server.infrastructure.utils.PageUtil;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Runner.dada
 * @date: 2020/12/6
 * @description: the system user domain service
 **/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserChatService userChatService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserChatMapper userChatMapper;


    @Override
    public User create(UserRegistrationParam req) {
        User user = User.builder().mobile(req.getMobile())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .code(CodeUtil.generateRandomUserCode())
                .createdBy(req.getUsername())
                .updatedBy(req.getUsername())
                .registerIp(req.getRegisterIp())
                .build();
        user.setUsername(req.getUsername());
        if (save(user)) {
            memberService.findOrCreateByUserId(user.getId());
        }
        return user;
    }

    @Override
    public void update(UserUpdateParam req) {
        User user = getByCodeOrElseThrow(req.getCode());

        getByUsername(req.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(user.getId())) {
                throw new AppException(CommonResponseCode.USER_NAME_EXIST);
            }
        });

        lambdaUpdate()
                .set(Objects.nonNull(req.getUsername()), User::getUsername, req.getUsername())
                .set(Objects.nonNull(req.getAvatar()), User::getAvatar, req.getAvatar())
                .set(Objects.nonNull(req.getRemark()), User::getRemark, req.getRemark())
                .set(Objects.nonNull(req.getStatus()), User::getStatus, req.getStatus())
                .eq(User::getId, user.getId())
                .update();
    }

    @Override
    public void passwordChange(UserController.UserPasswordChangeParam req) {
        User user = getByCodeOrElseThrow(req.getCode());
        User updated = User.builder().id(user.getId()).password(passwordEncoder.encode(req.getPassword())).build();
        updateById(updated);
    }

    @Override
    public User findById(Long id) {
        return lambdaQuery().eq(User::getId, id).one();
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).eq(User::getStatus, EntityStatusEnum.ENABLED).oneOpt();
    }

    @Override
    public Boolean existsByUsernameOrMobile(String username, String mobile) {
        return lambdaQuery().eq(User::getUsername, username).or().eq(User::getMobile, mobile).count() > 0;
    }

    @Override
    public User getByCodeOrElseThrow(String code) {
        return getByCode(code).orElseThrow(() -> new AppException(CommonResponseCode.USER_NOT_FOUND));
    }

    @Override
    public Optional<User> getByCode(String code) {
        return lambdaQuery().eq(User::getCode, code).eq(User::getStatus, EntityStatusEnum.ENABLED).last("LIMIT 1").oneOpt();
    }

    @Override
    public Boolean delete(Long id) {
        return lambdaUpdate().eq(User::getId, id).set(User::getDeletedAt, LocalTime.now())
                .update();
    }

    @Override
    public UserMemberDTO getUserInfo(String code) {
        User user = getByCode(code).get();
        Member member = memberService.findOrCreateByUserId(user.getId());

        UserMemberDTO userMemberDTO = buildUserMemberDTO(user, member);
        if (Objects.nonNull(user.getOrganizationId())) {
            organizationService.lambdaQuery().eq(Organization::getId, user.getOrganizationId()).oneOpt().ifPresent(organization -> {
                userMemberDTO.setOrganizationName(organization.getName());
                userMemberDTO.setOrganizationId(organization.getId());
            });
        }
        return userMemberDTO;
    }

    private UserMemberDTO buildUserMemberDTO(User user, Member member) {
        UserMemberDTO userMemberDTO = new UserMemberDTO();
        BeanUtils.copyProperties(user, userMemberDTO);
        userMemberDTO.setUserType(user.getUserType());
        userMemberDTO.setExpireDate(member.getExpireDate());
        userMemberDTO.setDailyLimit(member.getDailyLimit());
        userMemberDTO.setUsedQuota(member.getUsedQuota());
        userMemberDTO.setTotalQuota(member.getTotalQuota());
        userMemberDTO.setTodayUsedQuota(userChatService.todayUsedQuota(member.getUserId()));

        if (member.isFreeUser()) {
            userMemberDTO.setDailyLimit(appConfig.getDailyLimit());
        }
        return userMemberDTO;
    }

    @Override
    public List<UserMemberDTO> list(ListParam req) {
        List<User> users = lambdaQuery().eq(Objects.nonNull(req.getOrganizationId()), User::getOrganizationId, req.getOrganizationId())
                .eq(Objects.nonNull(req.getUserType()), User::getUserType, req.getUserType())
                .eq(Objects.nonNull(req.getStatus()), User::getStatus, req.getStatus())
                .list();
        Map<Long, Member> memberMap =
                memberService.lambdaQuery().in(Member::getUserId, users.stream().map(User::getId).collect(Collectors.toList()))
                        .list().stream().collect(Collectors.toMap(Member::getUserId, member -> member));
        return users.stream().map(user -> {
            Member member = memberMap.get(user.getId());
            return buildUserMemberDTO(user, member);
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserChatService.UserChatDTO> userChatList(UserChatService.ListParam req, User manager) {
        List<Long> colleagueIds = lambdaQuery().eq(User::getOrganizationId, manager.getOrganizationId()).list()
                .stream().map(User::getId).toList();

        Long userId = getUserIdWithUsername(req.getUsername());

        List<UserChat> userChats = userChatService.lambdaQuery().orderByDesc(UserChat::getCreatedAt)
                .eq(Objects.nonNull(req.getCategory()), UserChat::getCategory, req.getCategory())
                .eq(Objects.nonNull(req.getAppId()), UserChat::getAppId, req.getAppId())
                .eq(Objects.nonNull(userId), UserChat::getUserId, userId)
                .in(!CollectionUtils.isEmpty(colleagueIds),UserChat::getUserId, colleagueIds)
                .list();

        if (CollectionUtils.isEmpty(userChats)) {
            return List.of();
        }
        List<Long> ids = userChats.stream().map(UserChat::getUserId).toList();
        Map<Long, User> userMap = CollectionUtils.isEmpty(ids) ? Map.of() : lambdaQuery()
                .in(User::getId, ids).list().stream().collect(Collectors.toMap(User::getId, user -> user));
        return covertToUserChatDTO(userChats, userMap);
    }



    @Override
    public Page<UserChatService.UserChatDTO> userChatPage(UserChatService.PageParam req, User manager) {
        List<Long> colleagueIds = lambdaQuery().eq(User::getOrganizationId, manager.getOrganizationId()).list()
                .stream().map(User::getId).toList();

        Long userId = getUserIdWithUsername(req.getUsername());

        PageUtil pageUtil = new PageUtil(req.getPage(), req.getPerPage());
        Page<UserChat> page = new Page<>(pageUtil.getPage(), pageUtil.getPerPage());
        QueryWrapper<UserChat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(userId), "user_id", userId);
        queryWrapper.eq(Objects.nonNull(req.getAppId()), "app_id", req.getAppId());
        queryWrapper.eq(Objects.nonNull(req.getCategory()), "category", req.getCategory());
        queryWrapper.in(!CollectionUtils.isEmpty(colleagueIds),"user_id", colleagueIds);
        queryWrapper.orderByDesc("created_at");
        Page<UserChat> appPage = userChatMapper.selectPage(page, queryWrapper);
        List<Long> ids = appPage.getRecords().stream().map(UserChat::getUserId).toList();
        Map<Long, User> userMap = CollectionUtils.isEmpty(ids) ? Map.of() : lambdaQuery()
                .in(User::getId, ids).list().stream().collect(Collectors.toMap(User::getId, user -> user));

        List<UserChatService.UserChatDTO> dtos = covertToUserChatDTO(appPage.getRecords(), userMap);
        Page<UserChatService.UserChatDTO> userChatDTOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        userChatDTOPage.setRecords(dtos);
        return userChatDTOPage;
    }

    private Long getUserIdWithUsername(String username) {
        if (Objects.nonNull(username)) {
            User user = getByUsername(username).get();
            return user.getId();
        }
        return null;
    }

    private List<UserChatService.UserChatDTO> covertToUserChatDTO(List<UserChat> userChats, Map<Long, User> userMap) {
        return userChats.stream().map(u -> {
            User user = userMap.get(u.getUserId());
            UserChatService.UserChatDTO dto = new UserChatService.UserChatDTO();
            BeanUtils.copyProperties(u, dto);
            if (Objects.nonNull(user)) {
                dto.setUserCode(user.getCode());
                dto.setUsername(user.getUsername());
            }
            return dto;
        }).collect(Collectors.toList());
    }

}
