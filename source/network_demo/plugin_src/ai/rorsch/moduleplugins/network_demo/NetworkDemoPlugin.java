package ai.rorsch.moduleplugins.network_demo;

import android.content.Context;

import ai.rorsch.pandagenie.module.runtime.HtmlOutputHelper;
import ai.rorsch.pandagenie.module.runtime.ModulePlugin;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkDemoPlugin implements ModulePlugin {
    private static final int DEFAULT_MAX_CHARS = 4096;
    private static final int HARD_MAX_CHARS = 32768;

    @Override
    public String invoke(Context context, String action, String paramsJson) throws Exception {
        JSONObject params = new JSONObject(paramsJson != null && !paramsJson.trim().isEmpty() ? paramsJson : "{}");
        try {
            switch (action) {
                case "httpGet":
                    return httpGet(params);
                case "openPage":
                    return openPage();
                default:
                    return error("Unsupported action: " + action);
            }
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    private String httpGet(JSONObject params) throws Exception {
        String urlText = params.optString("url", "").trim();
        if (urlText.isEmpty()) throw new IllegalArgumentException("url is required");

        URL url = new URL(urlText);
        String protocol = url.getProtocol();
        if (!"https".equalsIgnoreCase(protocol) && !"http".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("only http/https URLs are supported");
        }

        int maxChars = params.optInt("maxChars", DEFAULT_MAX_CHARS);
        maxChars = Math.max(256, Math.min(HARD_MAX_CHARS, maxChars));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(12000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "PandaGenie-Module-Template/1.0");

        int status = conn.getResponseCode();
        String contentType = conn.getContentType();
        InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String body = stream != null ? readUpTo(stream, maxChars) : "";
        conn.disconnect();

        JSONObject output = new JSONObject()
                .put("url", urlText)
                .put("status", status)
                .put("contentType", contentType != null ? contentType : "")
                .put("bodyPreview", body)
                .put("truncated", body.length() >= maxChars);

        String html = HtmlOutputHelper.card("NET", "HTTP GET",
                HtmlOutputHelper.keyValue(new String[][]{
                        {"URL", urlText},
                        {"Status", String.valueOf(status)},
                        {"Content-Type", contentType != null ? contentType : ""},
                        {"Preview chars", String.valueOf(body.length())}
                }) + HtmlOutputHelper.p(body.length() > 600 ? body.substring(0, 600) + "..." : body));

        return ok(output, "HTTP " + status + " from " + url.getHost(), html);
    }

    private static String readUpTo(InputStream stream, int maxChars) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        try {
            int n;
            while ((n = reader.read(buf)) != -1 && sb.length() < maxChars) {
                int room = maxChars - sb.length();
                sb.append(buf, 0, Math.min(room, n));
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    private String openPage() throws Exception {
        return new JSONObject()
                .put("success", true)
                .put("output", new JSONObject().put("page", "index.html").toString())
                .put("_openModule", true)
                .put("_displayText", "Opening Network Demo.")
                .toString();
    }

    private String ok(JSONObject output, String displayText, String displayHtml) throws Exception {
        JSONObject result = new JSONObject()
                .put("success", true)
                .put("output", output.toString());
        if (displayText != null) result.put("_displayText", displayText);
        if (displayHtml != null && !displayHtml.isEmpty()) result.put("_displayHtml", displayHtml);
        return result.toString();
    }

    private String error(String message) throws Exception {
        return new JSONObject()
                .put("success", false)
                .put("error", message != null ? message : "unknown_error")
                .toString();
    }
}
