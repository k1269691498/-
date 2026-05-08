package ai.rorsch.moduleplugins.my_module;

import android.content.Context;

import ai.rorsch.pandagenie.module.runtime.HtmlOutputHelper;
import ai.rorsch.pandagenie.module.runtime.ModulePlugin;
import ai.rorsch.pandagenie.module.runtime.ModuleStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class MyModulePlugin implements ModulePlugin {

    @Override
    public String invoke(Context context, String action, String paramsJson) throws Exception {
        JSONObject params = new JSONObject(paramsJson != null && !paramsJson.isEmpty() ? paramsJson : "{}");

        switch (action) {
            case "hello":
                return hello(params);
            case "doTask":
                return doTask(context, params);
            case "savePreference":
                return savePreference(context, params);
            case "readPreference":
                return readPreference(context, params);
            case "renderSummary":
                return renderSummary(params);
            case "openPage":
                return openPage();
            default:
                return error("Unsupported action: " + action);
        }
    }

    private String hello(JSONObject params) throws Exception {
        String name = params.optString("name", "World");
        JSONObject output = new JSONObject()
                .put("message", "Hello, " + name + "!")
                .put("module", "my_module");
        String html = HtmlOutputHelper.card("OK", "My Module",
                HtmlOutputHelper.keyValue(new String[][]{
                        {"message", output.optString("message")},
                        {"module", output.optString("module")}
                }) + HtmlOutputHelper.successBadge());
        return ok(output, "Hello, " + name + "! This module is working.", html);
    }

    private String doTask(Context context, JSONObject params) throws Exception {
        String input = params.optString("input", "");
        if (input.isEmpty()) {
            return error("Input is required");
        }

        JSONObject output = new JSONObject()
                .put("input", input)
                .put("trimmed", input.trim())
                .put("length", input.length())
                .put("uppercase", input.toUpperCase(Locale.ROOT));

        return ok(output, "Processed: " + input);
    }

    private String savePreference(Context context, JSONObject params) throws Exception {
        String key = params.optString("key", "").trim();
        String value = params.optString("value", "");
        if (key.isEmpty()) return error("key is required");

        ModuleStorage storage = ModuleStorage.from(context);
        JSONObject data = new JSONObject();
        if (storage.exists("preferences.json")) {
            data = new JSONObject(storage.readText("preferences.json"));
        }
        data.put(key, value);
        storage.writeText("preferences.json", data.toString());

        JSONObject output = new JSONObject()
                .put("saved", true)
                .put("key", key)
                .put("value", value)
                .put("usedSpaceBytes", storage.getUsedSpace());
        return ok(output, "Saved preference: " + key);
    }

    private String readPreference(Context context, JSONObject params) throws Exception {
        String key = params.optString("key", "").trim();
        if (key.isEmpty()) return error("key is required");

        ModuleStorage storage = ModuleStorage.from(context);
        JSONObject data = storage.exists("preferences.json")
                ? new JSONObject(storage.readText("preferences.json"))
                : new JSONObject();

        JSONObject output = new JSONObject()
                .put("key", key)
                .put("exists", data.has(key))
                .put("value", data.optString(key, ""));
        return ok(output, data.has(key) ? "Preference found: " + key : "Preference not found: " + key);
    }

    private String renderSummary(JSONObject params) throws Exception {
        JSONArray items = params.optJSONArray("items");
        if (items == null) {
            items = new JSONArray();
            items.put("First item");
            items.put("Second item");
        }

        String[][] rows = new String[items.length()][2];
        for (int i = 0; i < items.length(); i++) {
            rows[i][0] = String.valueOf(i + 1);
            rows[i][1] = items.optString(i, "");
        }

        JSONObject output = new JSONObject()
                .put("count", items.length())
                .put("items", items);
        String html = HtmlOutputHelper.card("LIST", "Summary",
                HtmlOutputHelper.table(new String[]{"#", "Item"}, java.util.Arrays.asList(rows)));
        return ok(output, "Rendered " + items.length() + " item(s).", html);
    }

    private String openPage() throws Exception {
        return new JSONObject()
                .put("success", true)
                .put("output", new JSONObject().put("page", "index.html").toString())
                .put("_openModule", true)
                .put("_displayText", "Opening module page.")
                .toString();
    }

    private String ok(JSONObject output, String displayText) throws Exception {
        return ok(output, displayText, null);
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
                .put("error", message)
                .toString();
    }
}
