#! /bin/bash
export OX_PROJECT_NAME=core-mw
export APP_VERSION=$(cat helm/$OX_PROJECT_NAME/Chart.yaml | grep ^appVersion | sed 's/appVersion: //')
export CHART_VERSION=$(cat helm/$OX_PROJECT_NAME/Chart.yaml | grep ^version | sed 's/version: //')
export OX_CHART_REPOSITORY=cm://registry.open-xchange.com/chartrepo/middleware
export OX_REGISTRY=registry.open-xchange.com/middleware/$OX_PROJECT_NAME
export LOCAL_REPOSITORY_NAME=ox-middleware
export HELM_EXPERIMENTAL_OCI=1
export PACKAGE_TMP_DIR=state/helm
export HELM_CHART_DIR=helm/$OX_PROJECT_NAME