###
# In order to use the make file run 'source <projectDir>/env/env.zsh' or 'source <projectDir>/env/env.bash'
# Make sure to set environment variables HELM_REPO_USERNAME and HELM_REPO_PASSWORD
###

### Chart Repository
# Create a link to the remote repository
init:
	helm repo add ${LOCAL_REPOSITORY_NAME} ${OX_CHART_REPOSITORY}
# Package the middlware chart locally. Chart version and appVersion defaults to values from Chart.yaml
package:
	helm dep update
	helm package ${OX_PROJECT_DIR}/${HELM_CHART_DIR} -d ${OX_PROJECT_DIR}/${PACKAGE_TMP_DIR} --app-version ${APP_VERSION} --version ${CHART_VERSION}
# Package chart middlware chart and push it to the remote repository
push:
	helm push ${OX_PROJECT_DIR}/${HELM_CHART_DIR} ${LOCAL_REPOSITORY_NAME} -u '${HELM_REPO_USERNAME}' -p ${HELM_REPO_PASSWORD}
# Delete local packaged chart if any and remove linked remote repository
clean:
	helm repo remove ${LOCAL_REPOSITORY_NAME}
	rm ${OX_PROJECT_DIR}/${PACKAGE_TMP_DIR}/*.tgz

### OCI Registry
# Save helm chart locally and push it to ox registry
registry-push:
	helm registry login ${OX_REGISTRY} -u '${HELM_REPO_USERNAME}' -p ${HELM_REPO_PASSWORD}
	helm chart save ${OX_PROJECT_DIR}/${HELM_CHART_DIR} ${OX_REGISTRY}:${CHART_VERSION}
	helm chart push ${OX_REGISTRY}:${CHART_VERSION}
	helm registry logout ${OX_REGISTRY}