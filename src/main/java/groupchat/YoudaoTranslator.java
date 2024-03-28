package groupchat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class YoudaoTranslator {

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("请输入要翻译的文本：");
            String text = reader.readLine();

            translateAndPrint(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String translateAndPrint(String text) {
        String appKey = "451997ed82edae74";
        String secretKey = "yV6bHCP2AwmoH94i2GYUiXhzUiht3wnQ";
        String fromLang = "auto";
        String toLang = "zh-CHS";

        String translatedText = "";
        try {
            translatedText = translate(text, fromLang, toLang, appKey, secretKey);
//            printTranslation(text, translatedText);       // 在producter里面进行输出
        } catch (Exception e) {
            e.printStackTrace();
        }
        return translatedText;
    }

    public static String translate(String text, String fromLang, String toLang, String appKey, String secretKey) throws Exception {
        String apiUrl = "https://openapi.youdao.com/api";
        String salt = String.valueOf(System.currentTimeMillis());
        String signStr = appKey + truncate(text) + salt + secretKey;
        String sign = md5(signStr);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params = "q=" + URLEncoder.encode(text, "UTF-8") +
                "&from=" + fromLang +
                "&to=" + toLang +
                "&appKey=" + appKey +
                "&salt=" + salt +
                "&sign=" + sign;

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(params);
        writer.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        writer.close();
        reader.close();

        // 解析JSON响应并获取翻译结果
        String jsonResponse = response.toString();
        String translatedText = parseTranslation(jsonResponse);
        return translatedText;
    }

    public static String parseTranslation(String jsonResponse) {
        JSONObject jsonObject = JSONObject.parseObject(jsonResponse);
        JSONArray translationArray = jsonObject.getJSONArray("translation");
        String translation = translationArray.getString(0);
        return translation;
    }

    public static String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10, len));
    }

    public static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void printTranslation(String originalText, String translatedText) {
        System.out.println("Original Text: " + originalText);
        System.out.println("Translated Text: " + translatedText);
    }
}
