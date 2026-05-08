# Template Source Examples

Each folder under `source/` is a complete module example.

## `my_module`

Recommended starter for most developers.

It demonstrates:

- Action routing in `invoke(Context, action, paramsJson)`
- Safe JSON parameter parsing
- Structured business output
- `_displayText` fallback output
- `_displayHtml` mini-card output through `HtmlOutputHelper`
- Module-private storage through `ModuleStorage`
- Opening the H5 page with `_openModule`

Try prompts:

- `Say hello to Alice with my module`
- `Use my module to process "hello panda"`
- `Save preference theme=dark with my module`
- `Read preference theme with my module`
- `Render a summary for apple, banana, cherry with my module`
- `Open my module page`

## `network_demo`

Starter for modules that need HTTP/API access.

It demonstrates:

- `capabilities: ["network"]`
- `android.permission.INTERNET`
- URL scheme validation
- Connection/read timeouts
- Bounded response body previews
- HTML summary output

Try prompts:

- `Fetch https://httpbin.org/json with network demo`
- `Use network demo to GET https://example.com and show a preview`

## When to Add More Capabilities

Do not add capabilities "just in case." Add them only when the module actually touches that surface:

- Use `ModuleStorage` for private data without `file_read` or `file_write`.
- Add `file_read` or `file_write` only for user-visible files in shared storage.
- Add `camera`, `location`, `contacts`, `calendar`, `clipboard`, or `microphone` only when the module calls those Android APIs.

