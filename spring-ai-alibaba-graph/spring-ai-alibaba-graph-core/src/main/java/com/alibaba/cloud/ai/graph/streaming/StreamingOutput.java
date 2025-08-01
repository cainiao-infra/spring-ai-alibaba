/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.graph.streaming;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.model.ChatResponse;

import static java.lang.String.format;

public class StreamingOutput extends NodeOutput {

	private final String chunk; // null

	private final ChatResponse chatResponse;

	public StreamingOutput(ChatResponse chatResponse, String node, OverAllState state) {
		super(node, state);
		this.chatResponse = chatResponse;
		this.chunk = null;
	}

	public StreamingOutput(String chunk, String node, OverAllState state) {
		super(node, state);
		this.chunk = chunk;
		this.chatResponse = null;
	}

	public String chunk() {
		return chunk;
	}

	public ChatResponse chatResponse() {
		return chatResponse;
	}

	@Override
	public String toString() {
		if (node() == null) {
			return format("StreamingOutput{chunk=%s}", chunk());
		}
		return format("StreamingOutput{node=%s, state=%s, chunk=%s}", node(), state(), chunk());
	}

}
