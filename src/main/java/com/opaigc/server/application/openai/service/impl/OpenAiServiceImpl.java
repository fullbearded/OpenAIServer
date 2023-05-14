package com.opaigc.server.application.openai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opaigc.server.application.openai.client.OpenAiClient;
import com.opaigc.server.application.openai.domain.chat.MessageQuestion;
import com.opaigc.server.application.openai.domain.chat.MessageType;
import com.opaigc.server.application.openai.listener.OpenAISubscriber;
import com.opaigc.server.application.openai.service.OpenAiService;
import com.opaigc.server.application.user.domain.App;
import com.opaigc.server.application.user.event.ChatStreamCompletedEvent;
import com.opaigc.server.application.user.service.AppService;
import com.opaigc.server.config.AppConfig;
import com.opaigc.server.infrastructure.common.Constants;
import com.opaigc.server.infrastructure.exception.AppException;
import com.opaigc.server.infrastructure.http.CommonResponseCode;
import com.opaigc.server.infrastructure.utils.TokenCounter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 描述
 *
 * @author Runner.dada
 * @date 2023/3/23
 */
@Service
@Slf4j
public class OpenAiServiceImpl implements OpenAiService {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AppService appService;

    /**
     * @param parameters ChatParameters
     * @return Flux<String>
     **/
    @Override
    public Flux<String> chatSend(ChatParameters parameters) {
        OpenAiClient openAiClient = buildClient();

        App app = appService.getByCode(parameters.getAppCode());



        List<Message> cloneMessages = JSONArray.parseArray(JSONArray.toJSONString(parameters.getMessages()), Message.class);
        TokenCounter tokenCounter = new TokenCounter();
        int cloneToken = tokenCounter.countMessages(cloneMessages);

        List<Message> countMessages = finallyRequestMessages(cloneMessages);
        int finallyToken = tokenCounter.countMessages(countMessages);

        log.info("@Calulate origin message token: {}, finally message token: {}", cloneToken, finallyToken);



        if (CollectionUtils.isEmpty(countMessages)) {
            throw new AppException(CommonResponseCode.CHAT_OVER_MAX_TOKEN);
        }

        // 保存原始消息时，如果clone的消息与原始消息数量不一致，则只保存clone的消息，意味着计算出来的消息是超过了最大的限制了
        MessageQuestion userMessage = new MessageQuestion(MessageType.TEXT,
                countMessages,
                parameters.getMessages().size() == countMessages.size() ? List.of() : parameters.getMessages(),
                parameters.getRemoteIp(),
                parameters.getChatType(),
                Optional.ofNullable(app).map(App::getId).orElse(null),
                parameters.getTemperature()
        );
        return sendToOpenAi(parameters.getSessionId(), openAiClient, userMessage);
    }

    private List<Message> finallyRequestMessages(List<Message> messages) {
        TokenCounter tokenCounter = new TokenCounter();
        int tokenCount = tokenCounter.countMessages(messages);

        if (tokenCount <= appConfig.getMaxToken()) {
            // 如果当前消息列表的token数量小于等于最大限制，则直接返回该列表
            return messages;
        } else {
            // 如果当前消息列表的token数量超过最大限制，则递归删除第一条消息
            messages.remove(0);
            return finallyRequestMessages(messages); // 递归调用自己，直到token数量小于等于最大限制
        }
    }


    private Flux<String> sendToOpenAi(String sessionId, OpenAiClient openAiClient, MessageQuestion userMessage) {
        return Flux.create(emitter -> {
            OpenAISubscriber subscriber = new OpenAISubscriber(emitter, sessionId, this, userMessage);
            Flux<String> openAiResponse =
                    openAiClient.getChatResponse(appConfig.getApiToken(), sessionId, userMessage.getMessages(),
                            null, userMessage.getTemperature(), null);
            openAiResponse.subscribe(subscriber);
            emitter.onDispose(subscriber);
        });
    }

    @Override
    public CreditGrantsResponse creditGrants(String key) {
        OpenAiClient client = buildClient();
        return client.getCredit(Objects.isNull(key) ? appConfig.getApiToken() : key).block();
    }

    @Override
    public ModerationData moderation(String prompt) {
        OpenAiClient client = buildClient();
        Mono<ModerationData> toMono = client.getModeration(appConfig.getApiToken(), prompt);
        return toMono.block();
    }

    @Override
    public Mono<Boolean> checkContent(String prompt) {
        OpenAiClient client = buildClient();
        return client.checkContent(appConfig.getApiToken(), prompt);
    }

    private OpenAiClient buildClient() {
        return new OpenAiClient(appConfig);
    }

    @Override
    public void completed(MessageQuestion questions, String sessionId, String response) {
        ChatStreamCompletedEvent event = ChatStreamCompletedEvent.builder()
                .sessionId(sessionId)
                .questions(questions)
                .response(response)
                .build();
        redisTemplate.convertAndSend(Constants.CHAT_STREAM_COMPLETED_TOPIC, JSONObject.toJSONString(event));
        log.info("Chat Completed: {}", JSONObject.toJSONString(event));
    }

    @Override
    public void fail(String sessionId) {

    }
}
