package translators;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Random;

public class BaiduTranslator {
    private static String APP_ID;
    private static String SECRET_KEY;
    private static final String API_URL = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    static {
        try {
            Properties props = new Properties();
            props.load(BaiduTranslator.class.getClassLoader().getResourceAsStream("config.properties"));
            APP_ID = props.getProperty("appId");
            SECRET_KEY = props.getProperty("secretKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String translate(String query) {
        return translate(query, "auto", Lang.CHINESE_SIMPLIFIED);
    }

    public static String translate(String query, String fromLang, String toLang) {
        try {
            String salt = String.valueOf(new Random().nextInt(10000) + 32768);
            String sign = generateSign(query, salt);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // 构建请求体
            String requestBody = String.format("q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s",
                    query, fromLang, toLang, APP_ID, salt, sign);


            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();
            outputStream.close();

            // 获取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            conn.disconnect();
            String jsonResponse = response.toString();

            return parseResponse(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或处理错误
        }
    }

    public static String parseResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = JSON.parseObject(jsonResponse, Feature.AutoCloseSource, Feature.DisableSpecialKeyDetect);
            return jsonObject.getJSONArray("trans_result").getJSONObject(0).getString("dst");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing JSON response";
        }
    }

    private static String generateSign(String query, String salt) throws Exception {
        String toBeHashed = APP_ID + query + salt + SECRET_KEY;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(toBeHashed.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String result = translate("Hello, world!", "en", "zh");
        System.out.println(result);
    }
}


