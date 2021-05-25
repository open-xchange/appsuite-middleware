{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "core-mw.name" -}}
{{- default .Chart.Name .Values.overrides.name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "core-mw.fullname" -}}
{{- if .Values.overrides.name }}
{{- .Values.overrides.name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.overrides.name }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Add namespace and release version as environment variables
*/}}
{{- define "core-mw.env-variables" -}}
- name: POD_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: HELM_RELEASE
  value: {{ .Release.Name }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "core-mw.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "core-mw.labels" -}}
helm.sh/chart: {{ include "core-mw.chart" . }}
{{ include "core-mw.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "core-mw.selectorLabels" -}}
app.kubernetes.io/name: {{ include "core-mw.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "core-mw.imapHost" -}}
{{- if .Values.overrides.imapHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.imapHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "dovecot" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.smtpHost" -}}
{{- if .Values.overrides.smtpHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.smtpHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "postfix" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.dconvHost" -}}
{{- if .Values.overrides.dconvHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.dconvHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-d-conv" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.iconvHost" -}}
{{- if .Values.overrides.iconvHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.iconvHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-i-conv" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.appsuiteSecret" -}}
{{- if .Values.overrides.appsuiteSecret }}
{{- .Values.overrides.appsuiteSecret }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-mw" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.guardSecret" -}}
{{- if .Values.overrides.guardSecret }}
{{- .Values.overrides.guardSecret }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-mw-guard" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.dovecotSecret" -}}
{{- if .Values.overrides.dovecotSecret }}
{{- .Values.overrides.dovecotSecret }}
{{- else }}
{{- printf "%s-%s" .Release.Name "dovecot" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.serviceAccount" -}}
{{- if .Values.overrides.serviceAccount }}
{{- .Values.overrides.serviceAccount }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-mw" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.filestoresConfigMap" -}}
{{- if .Values.overrides.filestoresConfigMap }}
{{- .Values.overrides.filestoresConfigMap }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-filestores" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.dbsConfigMap" -}}
{{- if .Values.overrides.dbsConfigMap }}
{{- .Values.overrides.dbsConfigMap }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-dbs" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.imageConverterConfigMap" -}}
{{- if .Values.overrides.imageConverterConfigMap }}
{{- .Values.overrides.imageConverterConfigMap }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-i-conv" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.spellcheckHost" -}}
{{- if .Values.overrides.spellcheckHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.spellcheckHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "core-spellcheck" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "asbox.mysql.fullname" -}}
{{- printf "%s-%s" .Release.Name "mysql" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "core-mw.collabMysqlSchema" -}}
{{- if .Values.overrides.mysqlSchema }}
{{- .Values.overrides.mysqlSchema }}
{{- else }}
{{- printf "%s-%s" .Release.Name "dcsdb" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.antivirHost" -}}
{{- if .Values.overrides.antivirHost }}
{{- printf "%s-%s" .Release.Name .Values.overrides.antivirHost | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name "antivir" | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "core-mw.image" -}}
{{- $registry := default .Values.global.image.registry .Values.image.registry }}
{{- if .Values.image.tag }}
{{- printf "%s/%s:%s" $registry .Values.image.repository .Values.image.tag -}}
{{- else -}}
{{- printf "%s/%s:%s" $registry .Values.image.repository .Chart.AppVersion -}}
{{- end -}}
{{- end -}}

{{- define "core-mw.initImage" -}}
{{- $registry := default .Values.global.image.registry .Values.image.registry }}
{{- if .Values.init.image.tag }}
{{- printf "%s/%s:%s" $registry .Values.init.image.repository .Values.init.image.tag -}}
{{- else -}}
{{- printf "%s/%s:%s" $registry .Values.init.image.repository .Chart.AppVersion -}}
{{- end -}}
{{- end -}}

{{/* 
Computes the OX_BLACKLISTED_PACKAGES value based on the given configuration. When a packages is deemed to be disabled
it is added to the list. A package is disabled, if:
  * It is listed under packages.status with a value of 'disabled':
  packages:
    status:
      - open-xchange-authentication-database: 'disabled'
  OR if it is disabled as part of a feature in features.status:
  features:
    status:
      - admin: 'disabled'
  This would disable all packages that are listed at features.definitions.admin, UNLESS they are enabled again in the 
  packages section.

  See the values.yaml file for default values 
*/}}
{{- define "core-mw.blacklistedPackages" -}}
{{/* We'll assemble a list of blacklisted packages */}}
{{-  $packages := dict -}}
{{- $features := .Values.features.definitions -}} 
{{/* Iterate the feature status subtree and add disabled packages to the blacklist, removing enabled packages */}}
{{- range $feature, $status := .Values.features.status -}}
{{-   $featurePackages := get $features $feature -}}
{{-   range $package := $featurePackages  -}}
{{-     if eq $status "enabled " -}}
{{-       $_ := unset $packages $package -}}
{{-     else if eq $status "disabled" -}}
{{-       $_ := set $packages $package true -}}
{{-     else -}}
{{-       printf "Value for feature.status.%s is '%s' but needs to be either 'disabled' or 'enabled'" $feature $status | fail -}}
{{-     end  -}}
{{-   end  -}}
{{- end -}}
{{/* Iterate the package.status subtree and remove enabled packages from the blacklist, adding disabled ones */}}
{{- range $package, $status := .Values.packages.status -}}
{{-   if eq $status "enabled " -}}
{{-     $_ := unset $packages $package -}}
{{-   else if eq $status "disabled" -}}
{{-     $_ := set $packages $package true -}}
{{-   else -}}
{{-     printf "Value for package.status.%s is '%s' but needs to be either 'disabled' or 'enabled'" $package $status | fail -}}
{{-   end  -}}
{{- end -}}
{{/* render space separated list */}}
{{- keys $packages | join " " -}}
{{- end -}}
