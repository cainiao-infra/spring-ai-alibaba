package com.alibaba.cloud.ai.example.manus.dynamic.mcp.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class McpToolCallbackSchedulePool {

	private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

	public static ScheduledExecutorService getPool() {
		return pool;
	}

}
