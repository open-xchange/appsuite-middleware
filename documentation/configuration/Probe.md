---
title: Probe
---

Many proxies use a simple http get request to test if backends return an answer and how long it takes to answer so they can do proper
loadbalancing. This file configures the health probe that is used for health checks from proxy servers in front of our backend(cluster).


| __Key__ | com.openexchange.http.probe.alias |
|:----------------|:--------|
| __Description__ | The alias name in the URI namespace at which the probe is registered<br> |
| __Default__ | /healthProbe |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Probe.html">Probe</a> |

---
