package com.opaigc.server.application.user.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opaigc.server.application.user.domain.App;
import com.opaigc.server.application.user.domain.UserChat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/4/9
 */
public interface UserChatService extends IService<UserChat> {

    Long todayUsedQuota(Long userId);

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	class UserChatDTO {
		/**s
		 * 使用的用户id
		 */
		private Long id;

		/**s
		 * 使用的用户id
		 */
		private String username;

		/**
		 * 使用的用户id
		 */
		private String userCode;

		/**
		 * token 大小
		 */
		private Integer token;

		private JSONObject questions;

		private JSONObject answers;
		/**
		 * 创建时间
		 **/
		private LocalDateTime createdAt;
	}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ListParam {

		/**
		 * 使用的用户id
		 */
		private String username;

		/**
		 * 组织ID
		 **/
		private Long organizationId;

		/**
		 * 使用的APP id
		 */
		private Long appId;

		/**
		 * 聊天类型，FREE 免费， PAID 付费
		 */
		private UserChat.ChatCategoryEnum category;

		/**
		 * token 大小
		 */
		private Integer token;

		/**
		 * 创建时间
		 **/
		private LocalDateTime createdAt;

		/**
		 * 创建人
		 **/
		private String createdBy;

	}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class PageParam extends ListParam {
        /**
         * 页码
         */
        private Integer page;
        /**
         * 每页数量
         */
        private Integer perPage;
    }
}
