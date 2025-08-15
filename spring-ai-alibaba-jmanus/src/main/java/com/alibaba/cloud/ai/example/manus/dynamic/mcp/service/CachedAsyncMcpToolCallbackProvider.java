package com.alibaba.cloud.ai.example.manus.dynamic.mcp.service;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class CachedAsyncMcpToolCallbackProvider extends AsyncMcpToolCallbackProvider {

	private static final Logger logger = LoggerFactory.getLogger(CachedAsyncMcpToolCallbackProvider.class);

	private final List<McpAsyncClient> mcpClients;

	private final BiPredicate<McpAsyncClient, McpSchema.Tool> toolFilter;

	private static ToolCallback[] cachedToolCallbacks = null;

	private String mcpServerKeys = null;

	public CachedAsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, McpSchema.Tool> toolFilter,
			List<McpAsyncClient> mcpClients) {
		Assert.notNull(mcpClients, "MCP clients must not be null");
		Assert.notNull(toolFilter, "Tool filter must not be null");
		this.mcpClients = mcpClients;
		this.toolFilter = toolFilter;

		this.mcpServerKeys = mcpServerKeys = StringUtils.join(mcpClients.stream().map(ele -> {
			return ele.getClientInfo().name();
		}).collect(Collectors.toList()), ",");

		initToolCallbacksCache();
	}

	private void initToolCallbacksCache() {
		if (McpToolCallbackSchedulePool.hasTask(mcpServerKeys)) {
			return;
		}
		Boolean oldVal = McpToolCallbackSchedulePool.registerTask(mcpServerKeys);
		if (oldVal != null) {
			return;
		}
		logger.info("start initToolCallbacksCache times : {}, mcpServerKeys : {}", new Date(), mcpServerKeys);
		McpToolCallbackSchedulePool.getPool().scheduleWithFixedDelay(() -> {
			try {
				ToolCallback[] tempCachedToolCallbacks = getToolCallbacksInner();
				if (tempCachedToolCallbacks != null && tempCachedToolCallbacks.length > 0) {
					logger.info("{}, load tool callbacks cache success, size : {}", mcpServerKeys,
							tempCachedToolCallbacks.length);
					cachedToolCallbacks = tempCachedToolCallbacks;
				}
			}
			catch (Exception e) {
				logger.error("{}, load tool callbacks cache error", mcpServerKeys, e);
			}

		}, 15, 60, java.util.concurrent.TimeUnit.SECONDS);
	}

	/**
	 * 构造一个缓存的异步MCP工具回调提供者
	 *
	 * <p>
	 * 此构造方法创建一个新的CachedAsyncMcpToolCallbackProvider实例，默认的回调函数始终返回true。
	 * @param mcpClients MCP异步客户端列表
	 * @return 无返回值
	 */
	public CachedAsyncMcpToolCallbackProvider(List<McpAsyncClient> mcpClients) {
		this((mcpClient, tool) -> {
			return true;
		}, mcpClients);
	}

	public CachedAsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, McpSchema.Tool> toolFilter,
			McpAsyncClient... mcpClients) {
		this(toolFilter, List.of(mcpClients));
	}

	public CachedAsyncMcpToolCallbackProvider(McpAsyncClient... mcpClients) {
		this(List.of(mcpClients));
	}

	public ToolCallback[] getToolCallbacks() {
		if (cachedToolCallbacks != null && cachedToolCallbacks.length > 0) {
			return cachedToolCallbacks;
		}
		cachedToolCallbacks = getToolCallbacksInner();
		return cachedToolCallbacks;
	}

	public ToolCallback[] getToolCallbacksInner() {
		List<ToolCallback> toolCallbackList = new ArrayList();
		Iterator var2 = this.mcpClients.iterator();

		while (var2.hasNext()) {
			McpAsyncClient mcpClient = (McpAsyncClient) var2.next();
			ToolCallback[] toolCallbacks = (ToolCallback[]) mcpClient.listTools().map((response) -> {
				return (ToolCallback[]) response.tools().stream().filter((tool) -> {
					return this.toolFilter.test(mcpClient, tool);
				}).map((tool) -> {
					return new AsyncMcpToolCallback(mcpClient, tool);
				}).toArray((x$0) -> {
					return new ToolCallback[x$0];
				});
			}).block();
			this.validateToolCallbacks(toolCallbacks);
			toolCallbackList.addAll(List.of(toolCallbacks));
		}

		return (ToolCallback[]) toolCallbackList.toArray(new ToolCallback[0]);
	}

	private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException(
					"Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
		}
	}

	public static Flux<ToolCallback> asyncToolCallbacks(List<McpAsyncClient> mcpClients) {
		return CollectionUtils.isEmpty(mcpClients) ? Flux.empty()
				: Flux.fromArray((new AsyncMcpToolCallbackProvider(mcpClients)).getToolCallbacks());
	}

}
