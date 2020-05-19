---
title: Grafana
icon: fas fa-tachometer-alt
tags: Monitoring, Administration
---

# Grafana

## Dashboards as Code

With App Suite 7.10.4 we are offering Grafana Dashboards as Code for `App Suite` and `Java Virtual Machine`.

### Requirements

- `Jsonnet` is a data templating language &rightarrow; [https://github.com/google/jsonnet](https://github.com/google/jsonnet)
- `Grafonnet` is a `Jsonnet` library for generating Grafana dashboards &rightarrow; [https://github.com/grafana/grafonnet-lib](https://github.com/grafana/grafonnet-lib)

Please refer to the `README.md` files in the corresponding repositories for detailed informations, on how to install `Jsonnet` and `Grafonnet`.

### Build and import dashboards

There are couple of ways to build and generate Grafana dashboards from `jsonnet` source files. The following example will show _how_ to build them locally. To generate for example the `JSON` for the `App Suite` dashboard, just follow the step mentioned below:

```bash
jsonnet -J /path/to/grafonnet-lib /path/to/dashboards/appsuite_mw.jsonnet -o ./appsuite_mw.json
```

or without the `-o` parameter to print the `JSON` to `STDOUT`:

```bash
jsonnet -J /path/to/grafonnet-lib /path/to/dashboards/appsuite_mw.jsonnet
```

The generated `JSON` can be imported or provisioned to Grafana and should look like:

![app_suite_grafana_dashboard](grafana/app_suite_grafana_dashboard.png 'app_suite_grafana_dashboard')

### Add Prometheus datasource

The _default_ datasource should be named `Prometheus` so it is automatically picked up by the graphs:

![prometheus_ds_settings](grafana/prometheus_ds_settings.png 'prometheus_ds_settings')

### Prometheus configuration

The dashboards rely on the `service` label to distinguish the different App Suite services. So please make sure that, in the Prometheus configuration, each of the targets has the `service` label defined. Let's say you want to monitor an App Suite with IP `10.20.30.40` then the excerpt of the config should look like this:

```yaml
scrape_configs:
  - job_name: appsuite
    static_configs:
      - targets: ['10.20.30.40:8009']
        labels:
          env: dev
          job: appsuite
          service: mw
```