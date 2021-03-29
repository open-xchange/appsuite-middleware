#! /bin/bash
export OX_PROJECT_NAME=core
export APP_VERSION=$(cat helm/core-mw/Chart.yaml | grep ^appVersion | sed 's/appVersion: //')
export CHART_VERSION=$(cat helm/core-mw/Chart.yaml | grep ^version | sed 's/version: //')
export OX_CHART_REPOSITORY=cm://registry.open-xchange.com/chartrepo/middleware
export OX_REGISTRY=registry.open-xchange.com/middleware/$OX_PROJECT_NAME
export LOCAL_REPOSITORY_NAME=ox-mw-registry
export HELM_EXPERIMENTAL_OCI=1
export PACKAGE_TMP_DIR=state/helm
export HELM_CHART_DIR=helm/core-mw