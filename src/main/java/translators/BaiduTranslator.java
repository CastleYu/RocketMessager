package translators;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
            // 加载配置文件
            Properties props = new Properties();
            props.load(BaiduTranslator.class.getClassLoader().getResourceAsStream("config.properties"));
            APP_ID = props.getProperty("appId");
            SECRET_KEY = props.getProperty("secretKey");
        } catch (Exception e) {
            e.printStackTrace();
            // 处理错误或提供默认值
        }
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
            // 假设返回的JSON格式非常简单，仅包含一个翻译后的文本
            String jsonResponse = response.toString();
            // 这里需要手动解析JSON字符串，这种方式仅适用于非常简单的JSON格式
//            String translatedText = extractTranslatedText(jsonResponse);

            return parseResponse(jsonResponse);
            // 这里简化处理，只返回响应的字符串，实际中你可能需要解析JSON格式的响应
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或处理错误
        }
    }

    public static String parseResponse(String jsonResponse) {
        try {
            // 使用安全的解析选项，例如Feature.AutoCloseSource等
            JSONObject jsonObject = JSON.parseObject(jsonResponse, Feature.AutoCloseSource, Feature.DisableSpecialKeyDetect);
            JSONArray translationArray = jsonObject.getJSONArray("translation");
            return translationArray.getString(0);
        } catch (Exception e) {
            // 异常处理，可以根据需要记录日志或返回一个错误信息
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
        System.out.println(result); // 打印翻译结果
    }
}


