---
title: Zabbix - Prometheus Integration
icon: fas fa-tachometer-alt
tags: Monitoring, Administration
---

# Zabbix - Prometheus Integration

## Requirements

* The [Zabbix](https://www.zabbix.com) - [Prometheus](https://prometheus.io) integration is available in *version 4.2* and higher.
* The new metrics for App Suite are available in *version 7.10.4* and higher.

## Host configuration

* Go to **Configuration** > **Hosts**.
* Click on **Create host** to open the host configuration page.
* Enter a hostname `Prometheus` and select at least one host group.

## Simple items

First create an `HTTP agent master item` to do any Prometheus monitoring:

* Go to **Configuration** > **Hosts**.
* Click on the `Prometheus` host > **Items**.
* Click on **Create item**.
* Enter or change item parameters:
   * Name: **`Get App Suite Metrics`**
   * Type: **`HTTP agent`**
   * Key: **`appsuite_metrics.get`**
   * URL: **`http://<APPSUITE_HOST>:<APPSUITE_PORT>/metrics`**
   * Type of information: **`Text`** 
   * Update interval: **`10s`**
   * History storage period: **`Do not keep history`**
* Click on **Add** to save item.

Here is an example on how to extract the amount of active App Suite sessions based on the output of the *master* item.

* Create another item for the `Prometheus` host.
* Enter or change item parameters:
   * Name: **`Active session`**
   * Type: **`Dependent item`**
   * Key: **`appsuite.sessions.active.total`**
   * Master item: Select the master item from above **`Get App Suite Metrics`**
   * Type of information: **`Numeric (unsigned)`**
* Now click on the **`Preprocessing`** tab:
   * Add a preprocessing step: `Prometheus pattern`
   * Pattern: **`appsuite_sessions_active_total{client=~".*"}`**
* And save item.

## Low-level discovery

There is another way to automatically create items called `low-level discovery`. The next example shows how to create a discovery rule and automatically create items for the `App Suite DB Pools`.

* Go to **Configuration** > **Host** and select the **Prometheus** host > **Discovery rules**
* Click on **Create discovery rule**
* Enter or change item parameters:
   * Name: **`DB Pool connections LLD`**
   * Type: **`Dependent item`**
   * Key: **`appsuite.dbpool.discovery`**
   * Master item: Select the master item **`Get App Suite Metrics`**
* Click on the **`Preprocessing`** tab:
   * Add a preprocessing step: `Prometheus to JSON`
   * Parameters: `{__name__=~"^appsuite_mysql_connections(?:_total)?$"}`
* Click on the **`LLD macros`** tab:
   * Add the following `LLD macros`

   | LLD macro | JSONPath |
   |-----------|----------|
   | `{#DBCLASS}` | `$.labels.class` |
   | `{#DBPOOL}` | `$.labels.pool` |
   | `{#DBTYPE}` | `$.labels.type` |
   | `{#HELP}` | `$.help` |
   | `{#METRIC}` | `$.name` |
* Save discovery rule.

Now create an item prototype for that discovery rule.

* Click on **Create item prototype**
* Enter or change item prototype parameters:
   * Name: **`appsuite_mysql_connections_active: "{#DBCLASS}", Pool "{#DBPOOL}", Type "{#DBTYPE}"`**
   * Type: **`Dependent item`**
   * Key: **`appsuite.mysql.connections.active["{#DBCLASS}","{#DBPOOL}","{#DBTYPE}"]`**
   * Master item: Select the master item **`Get App Suite Metrics`**
   * Description: **`{#HELP}`**
* Click on the **`Preprocessing`** tab:
   * Add a preprocessing step: `Prometheus pattern`
   * Pattern: **`appsuite_mysql_connections_active{type="{#DBTYPE}",class="{#DBCLASS}",pool="{#DBPOOL}"}`**
* Save item prototype.

## Template

There is an example template which contains the items and the low-level discovery rule from this walkthrough. To import the template go to

* **Configuration** > **Templates**
* Click on **Import**
* Import file **`template_ox_appsuite_prom.xml`**

To apply now this template e.g. to host `10.20.30.40`

* Go to **Configuration** > **Hosts**
* Click on **Create host** to open the host configuration page.
* Enter a hostname `appsuite-node1` and select at least one host group.
* Click on **Templates** tab
* Link new template **`Template OX App Suite`**
* Click on **Macros** tab and select **Inherited and host macros**
* Change macro **`{$APPSUITE.CONN}`** value from `127.0.0.1` to `10.20.30.40`
* Click on **Add** to save host

Switch to **Monitoring** > **Latest data** to check whether the template is applied properly or not. If everything is fine, then there should be some values displayed e.g. 

![latest_data](zabbix_prom_integration/zabbix_latest_data.png "latest_data")