package httpsession;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class HttpConnectionManager {
    protected HttpURLConnection connection;
//    private static boolean cookieManagerSet = false;
//    private CookieManager cookieManager;

    public HttpConnectionManager() {
//        initializeCookieManager();
    }

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

    protected void buildCookies(Map<String, String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return;
        }

        StringJoiner cookieHeader = new StringJoiner("; ");
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            cookieHeader.add(cookie.getKey() + "=" + cookie.getValue());
        }
        connection.setRequestProperty("Cookie", cookieHeader.toString());
    }

//    private void initializeCookieManager() {
//        if (!cookieManagerSet) {
//            cookieManager = new CookieManager();
//            CookieHandler.setDefault(cookieManager);
//            cookieManagerSet = true;
//        }
//    }
//
//    protected void printCookies() {
//        CookieStore cookieStore = cookieManager.getCookieStore();
//        List<HttpCookie> cookies = cookieStore.getCookies();
//        for (HttpCookie cookie : cookies) {
//            System.out.println("Cookie: " + cookie);
//        }
//    }

    protected void connect(String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        connection = (HttpURLConnection) url.openConnection();
    }


    protected String send() throws IOException {
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
        }
        connection.disconnect();
        return response.toString();
    }

    protected byte[] sendBin() throws IOException {
        int responseCode = connection.getResponseCode();
        try (InputStream in = connection.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }

    protected void close() throws IOException {
        connection.disconnect();
        connection = null;
    }
}
