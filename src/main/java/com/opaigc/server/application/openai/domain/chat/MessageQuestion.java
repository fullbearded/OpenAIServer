package com.opaigc.server.application.openai.domain.chat;

import com.opaigc.server.application.openai.service.OpenAiService;
import com.opaigc.server.application.user.domain.UserChat;
import com.opaigc.server.infrastructure.common.Constants;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述
 *
 * @author runner.dada@gmail.com
 * @date 2023/3/28
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageQuestion {
	private MessageType messageType;
	private String message;
	private Date date;
	private String remoteIp;
	private List<OpenAiService.Message> messages;
	private List<OpenAiService.Message> originMessages;
	private UserChat.ChatCategoryEnum chatType;
	private Long appId;
	private Double temperature;

	public MessageQuestion(MessageType messageType, List<OpenAiService.Message> messages, List<OpenAiService.Message> originMessages,
												 String remoteIp, UserChat.ChatCategoryEnum chatType,
												 Long appId, Double temperature) {
		this.appId = Optional.ofNullable(appId).orElse(Constants.DEFAULT_APP_ID);
		this.messageType = messageType;
		this.messages = messages;
		this.originMessages = originMessages;
		this.remoteIp = remoteIp;
		this.chatType = chatType;
		this.temperature = Optional.ofNullable(temperature).orElse(null);
		this.date = new Date();
	}
}
