package com.example.similarity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.RequestBody;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class BridgeController {

    // ====================== 【你只需要改这里】======================
    private static final String FEISHU_APP_ID = "cli_a927ff2fe7f85bc8";
    private static final String FEISHU_APP_SECRET = "rJIpd9gEpqYtXYGEURNd4cUXWuBRMYLY";
    private static final String OPENCLAW_API = "http://127.0.0.1:18789/exec";
    private static final String GROUP_ID = "oc_b09887806f8572556927ffbf42bedd5a";
    private static final String AT_USER_ID = "ou_cfa093ba25145df3ff32d119a35d998f";
    private static final String FEISHU_VERIFY_TOKEN = "1sANOBaXsECpFcwUb1tsdfTt0hkcRPrD";
    // =================================================================

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    // ====================== 飞书回调（修复校验 + 消息解析）======================
    @PostMapping("/feishu/event")
    public Map<String, Object> receiveFeishuEvent(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        try {
            if ("url_verification".equals(body.get("type"))) {
                return Map.of("challenge", body.get("challenge"));
            }

            if (!"im.message.receive_v1".equals(body.get("event_type"))) {
                return Map.of("code", 0);
            }

            Map<String, Object> event = (Map<String, Object>) body.get("event");
            Map<String, Object> message = (Map<String, Object>) event.get("message");
            String contentJson = (String) message.get("content");
            Map<String, String> content = objectMapper.readValue(contentJson, Map.class);
            String text = content.get("text");

            System.out.println("✅ 飞书指令：" + text);

            Map<String, String> req = new HashMap<>();
            req.put("command", text);

            String jsonReq = objectMapper.writeValueAsString(req);
            RequestBody requestBody = RequestBody.create(jsonReq.getBytes(), JSON_MEDIA_TYPE);

            Request request = new Request.Builder()
                    .url(OPENCLAW_API)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("请求失败：" + response.code());
                }
            }

            return Map.of("code", 0, "msg", "success");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", e.getMessage());
        }
    }

    // ====================== OpenClaw → 飞书发消息 ======================
    @GetMapping("/openclaw/toFeishu")
    public String sendToFeishu(@RequestParam String msg) {
        try {
            String token = getTenantAccessToken();

            Map<String, String> content = new HashMap<>();
            content.put("text", "<at user_id=\"" + AT_USER_ID + "\"></at> " + msg);

            Map<String, Object> data = new HashMap<>();
            data.put("receive_id", GROUP_ID);
            data.put("msg_type", "text");
            data.put("content", objectMapper.writeValueAsString(content));

            String jsonBody = objectMapper.writeValueAsString(data);
            RequestBody body = RequestBody.create(jsonBody.getBytes(), JSON_MEDIA_TYPE);

            Request request = new Request.Builder()
                    .url("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("发送失败：" + response.code());
                }
            }

            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // ====================== 获取飞书 Token ======================
    private String getTenantAccessToken() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", FEISHU_APP_ID);
        params.put("app_secret", FEISHU_APP_SECRET);

        String jsonParams = objectMapper.writeValueAsString(params);
        RequestBody body = RequestBody.create(jsonParams.getBytes(), JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("获取 Token 失败：" + response.code());
            }
            Map<String, Object> result = objectMapper.readValue(response.body().string(), Map.class);
            return (String) result.get("tenant_access_token");
        }
    }
}
