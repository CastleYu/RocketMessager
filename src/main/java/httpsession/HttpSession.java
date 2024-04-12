package httpsession;

import java.io.IOException;
import java.util.Map;

public class HttpSession extends HttpConnectionManager {
    public HttpSession() {
        super();
    }

    // 发送GET请求
    public String get(String urlStr, Map<String, String> queryParams, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        String query = buildQuery(urlStr, queryParams);
        connect(query);
        buildHeader(headers);
        connection.setRequestMethod("GET");
        return send();
    }

    public String get(String urlStr, Map<String, String> queryParams, Map<String, String> headers) throws IOException {
        return get(urlStr, queryParams, headers, null);
    }

    public String get(String urlStr, Map<String, String> queryParams) throws IOException {
        return get(urlStr, queryParams, null, null);
    }

    public String get(String urlStr) throws IOException {
        return get(urlStr, null, null, null);
    }

    // 发送POST请求
    public String post(String urlStr, String postBody, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        String query = buildQuery(urlStr);
        connect(query);
        buildHeader(headers);
        buildCookies(cookies);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        buildBody(postBody);

        return send();
    }

    public String post(String urlStr, String postBody, Map<String, String> headers) throws IOException {
        return post(urlStr, postBody, headers, null);
    }

    public String post(String urlStr, String postBody) throws IOException {
        return post(urlStr, postBody, null, null);
    }


}
