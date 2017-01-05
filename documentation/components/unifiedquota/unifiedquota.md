---
title: Unified quota
---

This article attempts to outline the unified quota option that is available with Middleware Core v7.8.4 and Cloud-Plugins extensions.

Currently, the unified quota is only applicable for contexts/tenants, in which every user has its own Drive quota configured. See [File Storages per user](https://oxpedia.org/wiki/index.php?title=AppSuite:File_Storages_per_User) to see how to enable/configure a dedicated file storage for a user.

If unified quota is enabled/available for a certain user, the value ``"unified"`` is advertised to clients through ``"io.ox/core//filestoreMode"`` JSlob path (otherwise that path carries the value ``"default"``)
