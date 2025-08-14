package com.alibaba.cloud.ai.example.manus.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class SreSimpleLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	private static final Logger log = LoggerFactory.getLogger(SreSimpleLoggerAdvisor.class);

	public static final Function<ChatClientRequest, String> DEFAULT_REQUEST_TO_STRING = ChatClientRequest::toString;

	public static final Function<ChatResponse, String> DEFAULT_RESPONSE_TO_STRING = ModelOptionsUtils::toJsonStringPrettyPrinter;

	// private static final Logger logger =
	// LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

	private final Function<ChatClientRequest, String> requestToString;

	private final Function<ChatResponse, String> responseToString;

	private final int order;

	public SreSimpleLoggerAdvisor() {
		this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
	}

	public SreSimpleLoggerAdvisor(int order) {
		this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
	}

	public SreSimpleLoggerAdvisor(@Nullable Function<ChatClientRequest, String> requestToString,
			@Nullable Function<ChatResponse, String> responseToString, int order) {
		this.requestToString = requestToString != null ? requestToString : DEFAULT_REQUEST_TO_STRING;
		this.responseToString = responseToString != null ? responseToString : DEFAULT_RESPONSE_TO_STRING;
		this.order = order;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	private void logRequest(ChatClientRequest request) {
		log.info("llm request: {}", this.requestToString.apply(request));
	}

	private void logResponse(ChatClientResponse chatClientResponse) {
		log.info("llm response: {}", this.responseToString.apply(chatClientResponse.chatResponse()));
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		return SreSimpleLoggerAdvisor.class.getSimpleName();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Function<ChatClientRequest, String> requestToString;

		private Function<ChatResponse, String> responseToString;

		private int order = 0;

		private Builder() {
		}

		public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
			this.requestToString = requestToString;
			return this;
		}

		public Builder responseToString(Function<ChatResponse, String> responseToString) {
			this.responseToString = responseToString;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public SreSimpleLoggerAdvisor build() {
			return new SreSimpleLoggerAdvisor(requestToString, responseToString, order);
		}

	}

}
