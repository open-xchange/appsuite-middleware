# Grafana Dashboards as Code

* `Jsonnet` is a data templating language &rightarrow; [https://github.com/google/jsonnet](https://github.com/google/jsonnet)
* `Grafonnet` is a `Jsonnet` library for generating Grafana dashboards &rightarrow; [https://github.com/grafana/grafonnet-lib](https://github.com/grafana/grafonnet-lib)

## Getting Started

### Prerequisites

* Grafonnet requires Jsonnet, so follow the installation instructions for [Jsonnet](https://github.com/google/jsonnet#packages) and [Grafonnet](https://github.com/grafana/grafonnet-lib#install-grafonnet).

## Usage

To build a single dashboard from a file called `appsuite_mw.jsonnet` in the current folder:

```bash
jsonnet -J /path/to/grafonnet-lib ./appsuite_mw.jsonnet -o /path/to/grafana/provisioning/dashboards/appsuite_mw.json
```