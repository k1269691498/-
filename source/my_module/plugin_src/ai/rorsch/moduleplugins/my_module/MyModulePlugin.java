package ai.rorsch.moduleplugins.logistics_tracker;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LogisticsTrackerPlugin {

    // 1. 快递100自动识别接口 (免费，无需Key)
    private static final String DETECT_API = "https://poll.kuaidi100.com/poll/result.do";
    // 2. 物流查询接口
    private static final String QUERY_API = "https://poll.kuaidi100.com/poll/query.do";

    public String invoke(Context context, String action, String paramsJson) throws Exception {
        JSONObject params = new JSONObject(paramsJson);
        String number = params.getString("trackingNumber");

        try {
            // Step 1: 自动识别快递公司
            String companyCode = detectCompany(number);
            if (companyCode == null || companyCode.isEmpty()) {
                return error("无法识别该快递单号的所属公司，请确认单号是否正确。");
            }

            // Step 2: 查询物流详情
            JSONObject logisticsData = queryDetail(number, companyCode);
            
            // Step 3: 生成漂亮卡片
            String htmlCard = buildHtmlCard(logisticsData, companyCode);

            return success(htmlCard, "已为您查询到最新的物流信息。");

        } catch (Exception e) {
            return error("查询失败，请稍后再试：" + e.getMessage());
        }
    }

    /**
     * 核心：自动识别快递公司
     */
    private String detectCompany(String number) throws Exception {
        // 构造请求体（快递100的识别协议）
        JSONObject param = new JSONObject();
        param.put("num", number);
        
        JSONObject postData = new JSONObject();
        postData.put("result", "false");
        postData.put("num", number);

        String response = post("https://poll.kuaidi100.com/poll/result.do", postData.toString());
        JSONObject json = new JSONObject(response);
        
        // 如果识别成功，返回公司编码（如：yuantong, shunfeng）
        if ("200".equals(json.getString("status"))) {
            return json.getJSONObject("auto").getString("comCode");
        }
        return null;
    }

    /**
     * 查询详细物流信息
     */
    private JSONObject queryDetail(String number, String company) throws Exception {
        JSONObject param = new JSONObject();
        param.put("com", company);
        param.put("num", number);
        param.put("resultv2", "1"); // 开启轨迹聚合

        JSONObject postData = new JSONObject();
        postData.put("customer", "YOUR_CUSTOMER_ID"); // 如果有ID填这里
        postData.put("param", param.toString());
        postData.put("sign", "YOUR_SIGN_KEY"); // 如果有Key填这里

        // 注意：为了Demo能跑，这里用了一个简单的GET方式（实际生产建议用POST签名）
        String url = "https://poll.kuaidi100.com/poll/query.do?com=" + company + "&nu=" + number;
        String response = get(url);
        
        return new JSONObject(response);
    }

    /**
     * 构建聊天卡片 (HTML)
     */
    private String buildHtmlCard(JSONObject data, String company) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<div class='pg-card' style='border-left: 4px solid #4CAF50;'>");
        html.append("<h3>📦 物流状态：<span style='color:#4CAF50;'>")
            .append(data.optString("state", "运输中")).append("</span></h3>");
        html.append("<p><strong>承运公司：</strong>").append(company).append("</p>");
        html.append("<p><strong>快递单号：</strong>").append(data.optString("nu")).append("</p>");
        html.append("<hr style='margin:8px 0;'>");

        JSONArray list = data.getJSONArray("data");
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            html.append("<div style='margin-bottom:8px;'>");
            html.append("<p style='margin:0; font-weight:bold; color:#333;'>")
                .append(item.getString("ftime")).append("</p>");
            html.append("<p style='margin:0; color:#666;'>")
                .append(item.getString("context")).append("</p>");
            html.append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    // --- HTTP 工具方法 ---
    private String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) sb.append(line);
        rd.close();
        return sb.toString();
    }

    private String post(String urlStr, String body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes());
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) sb.append(line);
        rd.close();
        return sb.toString();
    }

    // --- 标准返回格式 ---
    private String success(String html, String text) {
        return "{\"success\":true, \"_displayText\":\"" + text + "\", \"_displayHtml\":\"" + html.replace("\"", "\\\"") + "\"}";
    }

    private String error(String msg) {
        return "{\"success\":false, \"error\":\"" + msg + "\"}";
    }
}
