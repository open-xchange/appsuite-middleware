---
title: Preview
---

This page shows all properties with the tag: Preview

| __Key__ | com.openexchange.preview.cache.enabled |
|:----------------|:--------|
| __Description__ | The switch to enable/disable the preview cache<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quota |
|:----------------|:--------|
| __Description__ | Specify the total quota for preview cache for each context<br>This value is used if no individual context quota is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 10485760 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaPerDocument |
|:----------------|:--------|
| __Description__ | Specify the quota per document for preview cache for each context<br>This value is used if no individual quota per document is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 524288 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.type |
|:----------------|:--------|
| __Description__ | Specifies what type of storage is used for caching previews<br>Either file store ("FS") or database ("DB").<br> |
| __Default__ | FS |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaAware |
|:----------------|:--------|
| __Description__ | Specifies if storing previews in file store affects user's file store quota or not<br>Only applies if "com.openexchange.preview.cache.type" is set to "FS"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
