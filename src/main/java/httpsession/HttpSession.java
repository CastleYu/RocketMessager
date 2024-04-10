package httpsession;

import java.io.IOException;
import java.util.Map;

public class HttpSession extends HttpConnectionManager {
    // 发送GET请求
    public String get(String urlStr, Map<String, String> queryParams, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        String query = buildQuery(urlStr, queryParams);
        connect(query);
        buildHeader(headers);
        connection.setRequestMethod("GET");
        return send();
    }

    // 发送POST请求
    public String post(String urlStr, String postBody, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        connect(urlStr);
        buildHeader(headers);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        buildBody(postBody);

        return send();
    }

//    public String post(String urlStr, String postBody, Map<String, String> headers, Map<String, String> cookies) throws IOException {
//        connect(urlStr);
//        buildHeader(headers);
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//        buildBody(postBody);
//
//        return send();
//    }

}
