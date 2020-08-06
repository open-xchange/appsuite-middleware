# Grafana Dashboards as Code

* `Jsonnet` is a data templating language &rightarrow; [https://github.com/google/jsonnet](https://github.com/google/jsonnet)
* `Grafonnet` is a `Jsonnet` library for generating Grafana dashboards &rightarrow; [https://github.com/grafana/grafonnet-lib](https://github.com/grafana/grafonnet-lib)

## Getting Started

### Prerequisites

* Docker installation is required, see [official installation docs](https://docs.docker.com/engine/installation/).
* Grafonnet requires Jsonnet, so follow the installation instructions for [Jsonnet](https://github.com/google/jsonnet#packages) and [Grafonnet](https://github.com/grafana/grafonnet-lib#install-grafonnet).

## Usage

There are couple of ways to generate `Grafana Dashboards` from the `jsonnet` source files. One way is to use a `Docker` image which contains `jsonnet` and `grafonnet-lib` and the other way is to have `jsonnet` and `grafonnet-lib` installed locally.

Some examples how to build and generate dashboards.

### Locally

To build a single dashboard from a file called `appsuite_mw.jsonnet` in the current folder:

```bash
jsonnet -J /path/to/grafonnet-lib ./appsuite_mw.jsonnet -o /path/to/grafana/provisioning/dashboards/appsuite_mw.json
```

### Docker image

To build a single dashboard from a file called `appsuite_mw.jsonnet` in the current folder:

```bash
$ docker run -v `pwd`:/tmp andrewfarley/jsonnet-bundler-grafonnet-lib:latest jsonnet /tmp/appsuite_mw.jsonnet > appsuite_mw.json
```

To build all dashboards in the current folder:

```bash
$ for file in ./*.*sonnet; do name=$(basename $file); docker run -v `pwd`:/tmp andrewfarley/jsonnet-bundler-grafonnet-lib:latest jsonnet /tmp/$file > ./${name%.jsonnet}.json; done
```
