# Helm Chart Middleware Core

This Helm Chart deploys middleware core in a kubernetes cluster.

## Introduction

This Chart includes the following components:

* Deployment with application container and a sidecar for logging purposes, init jobs, config map, service, ingress and secrets to deploy a middleware core component in a kubernetes cluster.       

## Requirements

Requires Kubernetes v1.19+

## Dependencies

This section will provide details about specific requirements in addition to this Helm Chart.

## Test installation

To run a test against a cluster deployment run

```shell
helm repo add middleware cm://registry.open-xchange.com/chartrepo/middleware
helm repo update
helm install --dry-run --debug --generate-name --version [VERSION] middleware/core-mw
```

## Installing the chart

To install the Chart with the release name “alice” run:

```shell
helm repo add middleware cm://registry.open-xchange.com/chartrepo/middleware
helm repo update
helm install alice --version [VERSION] middleware/core -f path/to/values_with_credentials.yaml
```

### Configuration

## Global Configuration 
| Parameter                           | Description                                         | Default                                         |
|-------------------------------------|-----------------------------------------------------|-------------------------------------------------|
| `global.image.registry`             | The image registry                                  | `gitlab.open-xchange.com:4567`                  |
| `image.repository`                  | The image repository                                | `sre/appsuite-in-a-box/as-mw`                   |
| `image.pullPolicy`                  | The imagePullPolicy for the deployment              | `IfNotPresent`                                  |
| `imagePullSecrets`                  | Some references to secrets for image registries     | `[]`                                            |
| `masterAdmin`                       | Base64 encoded                                      | `nn`                                            |
| `masterPassword`                    | Base64 encoded                                      | `nn`                                            |
| `hzGroupName`                       | Base64 encoded                                      | `nn`                                            |
| `hzGroupPassword`                   | Base64 encoded                                      | `nn`                                            |
| `basicAuthLogin`                    | Base64 encoded                                      | `nn`                                            |
| `basicAuthPassword`                 | Base64 encoded                                      | `nn`                                            |
| `jolokiaLogin`                      | Base64 encoded                                      | `nn`                                            |
| `jolokiaPassword`                   | Base64 encoded                                      | `nn`                                            |
| `credstoragePasscrypt`              | Base64 encoded                                      | `nn`                                            |
| `doveAdmAPIKey`                     | Base64 encoded                                      | `nn`                                            |

## Init Configuration
| Parameter                 | Description                                         | Default                                         |
|---------------------------|-----------------------------------------------------|-------------------------------------------------|
| `init.image.repository`   | The image to be used for the deployment             | `sre/appsuite-in-a-box/as-admin`                |

