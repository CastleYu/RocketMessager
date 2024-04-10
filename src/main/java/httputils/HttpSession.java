package httputils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpSession {
    private final CookieManager cookieManager = new CookieManager();

    // 发送GET请求
    public String sendGetRequest(String urlStr, Map<String, String> headers, Map<String, String> queryParams) throws IOException {
        // 构建查询参数
        StringBuilder query = new StringBuilder(urlStr);
        if (queryParams != null && !queryParams.isEmpty()) {
            query.append("?");
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                query.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            query.deleteCharAt(query.length() - 1); // 移除最后一个"&"
        }

        URL url = new URL(query.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        // 默认GET请求
        connection.setRequestMethod("GET");

        // 发送请求并处理响应
        return processResponse(connection);
    }

    // 发送POST请求
    public String sendPostRequest(String urlStr, Map<String, String> headers, String postBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/json");
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        // 发送POST请求必须设置为true
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        // 发送请求体
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(postBody);
            wr.flush();
        }

        // 发送请求并处理响应
        return processResponse(connection);
    }

    // 处理HTTP响应
    private String processResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        System.out.println("Response Code : " + responseCode);
        return response.toString();
    }
}
