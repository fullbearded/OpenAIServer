package com.opaigc.server.application.openai.listener;



import com.opaigc.server.application.openai.domain.chat.MessageQuestion;

/**
 * @author: Runner.dada
 * @date: 2023/3/28
 * @description:
 **/
public interface CompletedCallBack {

	/**
	 * 完成回掉
	 *
	 * @param questions
	 * @param sessionId
	 * @param response
	 */
	void completed(MessageQuestion questions, String sessionId, String response);

	/**
	 * 失败回掉
	 *
	 * @param sessionId
	 */
	void fail(String sessionId);

}
