package com.example.similarity.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class BridgeController {

    // ==================== 【配置信息】 ====================
    private static final String OPENCLAW_API = "http://127.0.0.1:18789/api/exec";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // OkHttp 4.12.0 推荐使用 MediaType.parse 或 MediaType.get
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    @PostMapping("/feishu/event")
    public Map<String, Object> receiveFeishuEvent(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        try {
            // 1. 响应飞书校验
            if ("url_verification".equals(body.get("type"))) {
                return Map.of("challenge", body.get("challenge"));
            }

            // 2. 解析消息内容
            if ("event_callback".equals(body.get("type"))) {
                Map<String, Object> event = (Map<String, Object>) body.get("event");
                if (event != null) {
                    Map<String, Object> message = (Map<String, Object>) event.get("message");
                    if (message != null && "text".equals(message.get("message_type"))) {

                        String contentStr = (String) message.get("content");
                        JsonNode contentNode = objectMapper.readTree(contentStr);
                        String userInput = contentNode.get("text").asText();

                        // 3. 核心执行逻辑
                        executeAppCommand(userInput);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Map.of("code", 0, "msg", "ok");
    }

    private void executeAppCommand(String text) {
        if (text.contains("打开") || text.contains("启动")) {
            String softwareName = text.replace("打开", "").replace("启动", "").trim();

            // 安全过滤：防止 Shell 注入，只留文字和数字
            String safeName = softwareName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5 ]", "");

            if (!safeName.isEmpty()) {
                String shellCmd = "open -a \"" + safeName + "\"";
                callOpenClaw(shellCmd);
            }
        }
    }

    private void callOpenClaw(String command) {
        try {
            Map<String, String> payload = Map.of("command", command);
            String jsonStr = objectMapper.writeValueAsString(payload);

            // 【OkHttp 4.12.0 核心修正写法】
            // 在 4.x 版本中，Java 调用的 create 方法签名是 (String, MediaType)
            // 如果 IDEA 还报红，请确保顶部的 import okhttp3.RequestBody; 正确
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, JSON_TYPE);

            Request request = new Request.Builder()
                    .url(OPENCLAW_API)
                    .post(body)
                    .build();

            // 异步发送指令
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.err.println("OpenClaw 请求失败: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 使用 try-with-resources 确保 response 关闭，避免 4.12.0 常见的连接泄露
                    try (Response res = response) {
                        if (res.isSuccessful()) {
                            System.out.println("指令已送达 Mac Mini: " + command);
                        } else {
                            System.err.println("OpenClaw 返回异常码: " + res.code());
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}