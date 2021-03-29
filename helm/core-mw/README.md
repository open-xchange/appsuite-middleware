# Helm Chart Middleware Core

This Helm Chart deploys middleware core in a kubernetes cluster.

## Introduction

This Chart includes the following components:

* Deployment, jobs, config map, service and secrets to deploy a middleware core component in a kubernetes cluster.

## Dependencies

This section will provide details about specific requirements in addition to this Helm Chart.

## Test installation

To run a test against a cluster deployment run

```shell
helm repo add middleware cm://registry.open-xchange.com/chartrepo/middleware
helm install --dry-run --debug --generate-name --version [VERSION] middleware/core-mw
```

## Installing the chart

To install the Chart with the release name “alice” run:

```shell
helm repo add middleware cm://registry.open-xchange.com/chartrepo/middleware
helm install alice --version [VERSION] middleware/core -f path/to/values.yaml
```

### Configuration

## Global Configuration 
| Parameter                           | Description                                         | Default                                         |
|-------------------------------------|-----------------------------------------------------|-------------------------------------------------|
| `global.image.registry`             | The image registry                                  | `gitlab.open-xchange.com:4567`                  |
| `image.repository`                  | The image repository                                | `sre/appsuite-in-a-box/as-mw`                   |
| `image.pullPolicy`                  | The imagePullPolicy for the deployment              | `IfNotPresent`                                  |
| `imagePullSecrets`                  | Some references to secrets for image registries     | `[]`                                            |


## Init Configuration
| Parameter                 | Description                                         | Default                                         |
|---------------------------|-----------------------------------------------------|-------------------------------------------------|
| `init.image.repository`   | The image to be used for the deployment             | `sre/appsuite-in-a-box/as-admin`                |

