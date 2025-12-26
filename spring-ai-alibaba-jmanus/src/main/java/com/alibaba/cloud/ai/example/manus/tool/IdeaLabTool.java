package com.alibaba.cloud.ai.example.manus.tool;

import com.alibaba.cloud.ai.example.manus.tool.code.ToolExecuteResult;
import com.alibaba.idealab.client.api.IdealabApi;
import com.alibaba.idealab.client.api.model.ideas.IdealabRunIdeasRequest;
import com.alibaba.idealab.client.api.model.ideas.IdealabRunIdeasResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author muhe.zz
 * @date 2025/12/26
 */
public class IdeaLabTool extends AbstractBaseTool<IdeaLabTool.IdeaLabToolInput> {

    private static final String NAME = "ideaLabTool";

    private static final Logger log = LoggerFactory.getLogger(IdeaLabTool.class);

    public IdeaLabTool() {
    }

    public static class IdeaLabToolInput {
        private String accessKey;
        private String appCode;
        private String appVersion;
        private String userId;
        private String question;

        IdeaLabToolInput() {
        }

        IdeaLabToolInput(String accessKey, String appCode, String appVersion, String userId, String question) {
            this.accessKey = accessKey;
            this.appCode = appCode;
            this.appVersion = appVersion;
            this.userId = userId;
            this.question = question;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    @Override
    public ToolExecuteResult run(IdeaLabToolInput input) {
        IdealabApi idealabService = new IdealabApi(input.getAccessKey());

        // 构造入参
        IdealabRunIdeasRequest request = new IdealabRunIdeasRequest();
        request.setAppCode(input.getAppCode());
        request.setAppVersion(input.getAppVersion()); // 支持其他版本：latest 表示使用最新发布版本, dev 表示使用开发调试版本
        request.setUserId(input.getUserId()); // 工号
        //request.setSessionId(UUID.randomUUID().toString()); // 不填默认UUID
        //request.setMessageId(UUID.randomUUID().toString()); // 不填默认traceId
        request.setQuestion(input.getQuestion());

        // 流式输出每一帧，如果是idealabApi.ideas() 是块式调用
        IdealabRunIdeasResponse response = idealabService.ideasStream(request)
            .doOnNext(line -> System.out.println(line.getContent()))
            .blockLast();

        if (null == response || null == response.getContent()) {
            return new ToolExecuteResult("工具执行失败");
        }

        return new ToolExecuteResult(response.getContent());
    }

    @Override
    public String getServiceGroup() {
        return "default-service-group";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "调用idealab，获取业务自定义Agent的返回结果";
    }

    @Override
    public String getParameters() {
        return """
				{
				    "type": "object",
				    "properties": {
				        "accessKey": {
				            "type": "string",
				            "description": "请求API的密钥"
				        },
				        "appCode": {
				            "type": "string",
				            "description": "请求API的appCode"
				        },
				        "appVersion": {
				            "type": "string",
				            "description": "API的版本"
				        },
				        "userId": {
				            "type": "string",
				            "description": "访问的用户ID"
				        },
				        "question": {
				            "type": "string",
				            "description": "请求API的正文"
				        }
				    },
				    "required": ["accessKey", "appCode", "appVersion", "userId", "question"]
				}
				""";
    }

    @Override
    public Class<IdeaLabToolInput> getInputType() {
        return IdeaLabToolInput.class;
    }

    @Override
    public String getCurrentToolStateString() {
        log.info("Invoke getCurrentToolStateString Method");
        return "任务正在执行中......";
    }

    @Override
    public void cleanup(String planId) {
        log.info("Cleaned up resources for plan: {}", planId);
    }
}