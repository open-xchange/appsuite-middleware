---
title: Microsoft OneDrive
classes: toc
icon: fa-windows
---

To setup the Microsoft OneDrive file store you have to install the package `open-xchange-file-storage-onedrive`.

# Required Permissions

The following Microsoft Graph Permissions are required to enable the OneDrive cloud storage.

 * [Files.Read](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.Read.All](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.Read.Selected](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.All](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.AppFolder](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.Selected](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [offline_access](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [openid](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [profile](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 
 Ensure that in case of an upgrade to 7.10.2 you will need to generate new access tokens. More information [here]({{ site.baseurl }}/middleware/components/oauth/microsoft.html#upgrade-to-microsoft-graph-api).