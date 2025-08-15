package com.alibaba.cloud.ai.example.manus.dynamic.mcp.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class McpToolCallbackSchedulePool {

	private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

	private static ConcurrentHashMap<String, Boolean> taskKeys = new ConcurrentHashMap<String, Boolean>();

	public static ScheduledExecutorService getPool() {
		return pool;
	}

	public static Boolean registerTask(String task) {
		return taskKeys.putIfAbsent(task, Boolean.TRUE);
	}

	public static boolean hasTask(String task) {
		return taskKeys.contains(task);
	}

}
