---
title: Flags and bitmasks
classes: no-affix
---

# Permission flags

| Bits    | Value                   |
|---------|-------------------------|
| **0&#x2011;6** | **Folder permissions:** |
|   | 0 (no permissions) |
|   | 1 (see the folder) |
|   | 2 (create objects in folder) |
|   | 4 (create subfolders) |
|   | 64 (all permissions) |
| **7&#x2011;13** | **Read permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 1 (read only own objects) |
|   | 2 (read all objects) |
|   | 64 (all permissions) |
| **14&#x2011;20** | **Write permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 1 (modify only own objects) |
|   | 2 (modify all objects) |
|   | 64 (all permissions) |
| **21&#x2011;27** | **Delete permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 1 (delete only own objects) |
|   | 2 (delete all objects) |
|   | 64 (all permissions) |
| **28** | **Admin flag:** |
|   | 0 (no permissions) |
|   | 1 (every operation modifying the folder in some way requires this permission (e.g. changing the folder name) |

