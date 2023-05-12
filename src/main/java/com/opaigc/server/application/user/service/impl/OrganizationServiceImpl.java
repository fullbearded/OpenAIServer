package com.opaigc.server.application.user.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opaigc.server.application.user.domain.Organization;
import com.opaigc.server.application.user.mapper.OrganizationMapper;
import com.opaigc.server.application.user.service.OrganizationService;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/9
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

}
