# Vibe Coding Guide for PandaGenie Modules

This guide is for developers who want to build modules by hand or with an AI coding assistant.

## Does This Template Cover Real Module Development?

Mostly yes, after the samples in this repo:

- `source/my_module` is the recommended starter. It shows action routing, parameter validation, structured JSON output, module-private storage, HTML mini-card output, and opening an H5 page.
- `source/network_demo` shows the extra pieces needed for a capability-gated module: `capabilities`, Android permissions, URL validation, timeouts, bounded response reads, and safe output previews.

For more complex modules, use these official source modules as references in `PandaGenieSource/source`:

| Need | Good reference |
|---|---|
| Device/system APIs | `device_info` |
| Persistent user data | `notes` |
| Files and external storage | `filemanager`, `archive`, `file_stats` |
| Rich image/file output | `qrcode`, `image_tools`, `notes` |
| Network calls | `weather`, `translator`, `network_tools` |
| H5-heavy interactive module | game modules such as `snake_game`, `tetris_game` |

## Prompt to Give Your Coding Assistant

```text
Build a PandaGenie module under source/<module_id>.

Hard requirements:
- Implement ai.rorsch.pandagenie.module.runtime.ModulePlugin.
- The entry method is invoke(Context context, String action, String paramsJson).
- Route every public action listed in manifest.json apis[].
- Parse paramsJson with org.json.JSONObject and treat null/blank params as "{}".
- Return JSON string responses:
  - success: true/false
  - output: a string containing business JSON for successful calls
  - error: a concise message for failures
  - optional _displayText fallback
  - optional _displayHtml mini-card using HtmlOutputHelper
  - optional _openModule: true to open index.html
- Use ModuleStorage.from(context) for private module data.
- Do not hardcode external storage paths unless the manifest declares file_read/file_write and the user explicitly needs shared files.
- If network is used, declare capabilities ["network"] and android.permission.INTERNET, validate URL scheme, set timeouts, and cap response size.
- Keep manifest id, pluginClass package, and source folder names consistent.
- Do not add devCertFingerprint manually; the packer injects it.
```

## Response Fields Cheat Sheet

| Field | Use |
|---|---|
| `success` | Required boolean. |
| `output` | Business result. Prefer a JSON string so the AI can reason over it. |
| `error` | Required when `success=false`. Keep it short and actionable. |
| `_displayText` | Human-readable fallback text in chat. |
| `_displayHtml` | Inline HTML mini-card. Prefer `HtmlOutputHelper`. |
| `_displayHtmlFull` | Optional full-screen detail HTML. |
| `_openModule` | Set to `true` to open `index.html`. |
| `_richContent` | Optional array for image/file attachments. See `qrcode` and `notes` in PandaGenieSource. |
| `_vaultSave` | Optional signal for saving generated user content. See `notes` in PandaGenieSource. |

## Capability Checklist

Use the smallest capability set that matches the module:

| Capability | When to declare |
|---|---|
| `network` | HTTP/API calls, downloads, remote lookup. |
| `file_read` | Read user-visible files outside private module storage. |
| `file_write` | Write user-visible files outside private module storage. |
| `camera` | Camera preview, scan, image capture. |
| `location` | GPS or coarse location. |
| `microphone` | Audio capture. |
| `clipboard` | Clipboard read/write. |
| `contacts` | Contacts provider access. |
| `calendar` | Calendar provider access. |

Private data saved through `ModuleStorage.from(context)` does not require `file_read` or `file_write`.

## Manifest Quality Rules

The AI dispatcher reads `description`, `apis[].desc`, and `paramDesc`. Write them as routing instructions, not just labels.

Good:

```json
{
  "name": "httpGet",
  "desc": "请求一个 HTTPS/HTTP URL，返回状态码、内容类型和正文预览；适合查询公开 JSON API 或网页摘要",
  "params": ["url", "maxChars"],
  "paramDesc": ["要请求的 URL，仅支持 http/https", "最多读取字符数，默认 4096，最大 32768"]
}
```

Weak:

```json
{
  "name": "get",
  "desc": "请求",
  "params": ["x"]
}
```

## Practical Build Loop

1. Rename `source/my_module` to your real module id.
2. Update `manifest.json`: `id`, `name`, `description`, `pluginClass`, `capabilities`, `permissions`, and `apis`.
3. Rename the Java package path and class.
4. Build with the PandaGenieSource toolkit:

```powershell
cd E:\ProjectAI\PandaGenie\PandaGenieSource\module-dev-toolkit
.\mk_module.ps1 -Action init-dev-signing
.\mk_module.ps1 -Action pack -Modules "your_module_id"
```

5. Push the generated `.mod` to a device:

```powershell
adb push ..\modules\your_module_id.mod /sdcard/PandaGenie/modules/
```

6. Enable Developer Mode in PandaGenie and restart the app.

