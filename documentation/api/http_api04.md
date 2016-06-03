---
title: Flags and bitmasks
classes: no-affix
---

# Permission flags

| Bits | Value |
|------|-------|
| **0-6** | **Folder permissions:** |
| **21-27** | **Delete permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 0 (no permissions) |
|   | 1 (see the folder) |
|   | 1 (delete only own objects) |
|   | 2 (create objects in folder) |
|   | 2 (delete all objects) |
|   | 4 (create subfolders) |
|   | 64 (all permissions) |
|   | 64 (all permissions) |
| **7-13** | **Read permissions for objects in folder:** |
| **28** | **Admin flag:** |
|   | 0 (no permissions) |
|   | 0 (no permissions) |
|   | 1 (read only own objects) |
|   | 1 (every operation modifying the folder in some way requires this permission (e.g. changing the folder name) |
|   | 2 (read all objects) |
|   | 64 (all permissions) |
| **14-20** | **Write permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 1 (modify only own objects) |
|   | 2 (modify all objects) |
|   | 64 (all permissions) |

