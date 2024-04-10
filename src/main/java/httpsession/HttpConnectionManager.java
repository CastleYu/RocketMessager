package httpsession;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.StringJoiner;

public class HttpConnectionManager {
    protected final CookieManager cookieManager = new CookieManager();
    protected HttpURLConnection connection;

    protected String buildQuery(String url) {
        return buildQuery(url, null);
    }

    protected String buildQuery(String url, Map<String, String> queryParams) {
        StringBuilder query = new StringBuilder(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            query.append("?");
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                query.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            query.deleteCharAt(query.length() - 1); // 移除最后一个"&"
        }
        return query.toString();
    }

    protected void buildHeader(Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    protected void buildBody(String postBody) throws IOException {
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(postBody);
            wr.flush();
        }
    }

    private void buildCookies(Map<String, String> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            StringJoiner cookieHeader = new StringJoiner("; ");
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                cookieHeader.add(cookie.getKey() + "=" + cookie.getValue());
            }
            connection.setRequestProperty("Cookie", cookieHeader.toString());
        }
    }

    protected void connect(String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        connection = (HttpURLConnection) url.openConnection();
    }


    // 处理HTTP响应
    protected String send() throws IOException {
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
}
