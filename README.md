<div align="center">

# 🐼 PandaGenie Module Template

**Start building your own PandaGenie module in minutes**

[![Use this template](https://img.shields.io/badge/Use%20this-Template-6c5ce7?style=for-the-badge)](https://github.com/Rorschach123/PandaGenie-Module-Template/generate)

</div>

---

## Quick Start

### 1. Use This Template

Click **"Use this template"** button above (or on GitHub) to create your own module repository.

### 2. Pick a Starting Point

This template now includes two examples:

| Example | Use when | Shows |
|---|---|---|
| `source/my_module` | You want the safest default starter | Action routing, parameters, private storage, `_displayHtml`, `_openModule` |
| `source/network_demo` | Your module needs an HTTP/API call | `capabilities`, `android.permission.INTERNET`, URL validation, timeouts |

For most modules, copy `source/my_module`, rename it, then add only the capabilities you need.

### 3. Customize Your Module

Rename and edit the files in `source/my_module/` or copy one of the examples:

| File | What to do |
|------|-----------|
| `manifest.json` | Change `id`, `name`, `description`, and define your `apis` |
| `plugin_src/.../MyModulePlugin.java` | Implement your logic in the `invoke()` method |
| `index.html` | (Optional) Create a settings/config UI page |

Keep these fields in sync:

- `manifest.json` `id`
- `manifest.json` `pluginClass`
- Java `package`
- Folder path under `plugin_src`

### 4. Build & Test

```powershell
# Clone PandaGenieSource for the build toolkit
git clone https://github.com/Rorschach123/PandaGenieSource.git

# Copy your module source into PandaGenieSource/source/
# Or symlink: your source/my_module → PandaGenieSource/source/my_module

cd PandaGenieSource/module-dev-toolkit/

# Generate your developer signing key (first time only)
.\mk_module.ps1 -Action init-dev-signing

# Build your module
.\mk_module.ps1 -Action pack -Modules "my_module"

# Push to device
adb push ..\modules\my_module.mod /sdcard/PandaGenie/modules/
```

### 5. Test on Device

1. Open PandaGenie app → Settings → Enable **Developer Mode**
2. Restart the app
3. Your module is now loaded! Try chatting:
   - *"Test my module"*
   - *"Say hello to Alice"*
   - *"Save preference theme=dark with my module"*
   - *"Fetch https://httpbin.org/json with network demo"*

### 6. Publish

When your module is ready:

- **Option A (Recommended):** Upload to [cf.pandagenie.ai](https://cf.pandagenie.ai) for automatic signing & publishing
- **Option B:** Submit a PR to [PandaGenieSource](https://github.com/Rorschach123/PandaGenieSource)

---

## Module Structure

```
source/my_module/
├── manifest.json                                    ← Module metadata & API definitions
├── index.html                                       ← Optional UI page (WebView)
└── plugin_src/
    └── ai/rorsch/moduleplugins/my_module/
        └── MyModulePlugin.java                      ← Your plugin logic
```

## The Only Interface You Need

```java
public interface ModulePlugin {
    String invoke(Context context, String action, String paramsJson) throws Exception;
}
```

- `action` — which API is being called (matches `apis[].name` in manifest.json)
- `paramsJson` — JSON string with parameters (matches `apis[].params` in manifest.json)
- Return a JSON string: `{"success": true, "output": "..."}` or `{"success": false, "error": "..."}`

Recommended success response shape:

```json
{
  "success": true,
  "output": "{\"count\":2,\"items\":[\"a\",\"b\"]}",
  "_displayText": "Rendered 2 items.",
  "_displayHtml": "<div class='pg-card'>...</div>"
}
```

Optional response fields supported by the app:

| Field | Purpose |
|---|---|
| `_displayText` | Human-readable fallback text |
| `_displayHtml` | Rich mini-card in chat; use `HtmlOutputHelper` |
| `_displayHtmlFull` | Full detail page for larger results |
| `_openModule` | Open the module `index.html` page |
| `_richContent` | Attach generated files/images |
| `_vaultSave` | Offer generated content for user vault/save flows |

## Common Development Patterns

| Pattern | Template/reference |
|---|---|
| Pure logic, no permissions | `source/my_module`: `hello`, `doTask` |
| Private module data | `source/my_module`: `savePreference`, `readPreference` |
| Chat card UI | `source/my_module`: `renderSummary` |
| Open H5 UI | `source/my_module`: `openPage` |
| Network calls | `source/network_demo`: `httpGet` |
| Images/files | See `qrcode`, `image_tools`, `notes` in PandaGenieSource |

Private data via `ModuleStorage.from(context)` does not need `file_read` or `file_write`. Only declare file capabilities when you access user-visible shared storage.

## Writing Good API Descriptions

The AI reads your `manifest.json` to understand what your module can do. Good descriptions = better AI behavior.

**Good example:**
```json
{
  "name": "searchFiles",
  "desc": "在指定目录下搜索文件，支持通配符匹配。当用户说【找文件】【搜索XX文件】时使用",
  "desc_en": "Search files in a directory with wildcard support. Use when user says 'find files' or 'search for XX'",
  "params": ["dir", "pattern"],
  "paramDesc": ["搜索目录路径", "文件名匹配模式，如 *.jpg"],
  "paramDesc_en": ["Directory path to search", "Filename pattern, e.g. *.jpg"]
}
```

**Bad example:**
```json
{
  "name": "search",
  "desc": "搜索",
  "params": ["q"]
}
```

## Resources

- [Vibe Coding Guide](docs/VIBE_CODING_GUIDE.md)
- [Full Module Development Guide](https://github.com/Rorschach123/PandaGenieSource/blob/main/module-dev-toolkit/MODULE_DEVELOPMENT_GUIDE.md)
- [PandaGenie Source Code](https://github.com/Rorschach123/PandaGenieSource)
- [Discord Community](https://discord.gg/Cfc7pjrjt2)

---

## License

LGPL-3.0 — See [LICENSE](LICENSE) for details.
