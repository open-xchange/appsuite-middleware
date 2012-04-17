# norootforbuild
Name:           open-xchange-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
#BuildRequires:  ant >= 1.7 ant-nodeps open-xchange-log4j
%if 0%{?suse_version}  && !0%{?sles_version}
#BuildRequires:  java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
#BuildRequires:  java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
#BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:    @OXVERSION@
%define        ox_release 0
Release:    %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0 
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:		http://www.open-xchange.com/            
Source:        %{name}_%{version}.orig.tar.bz2
Summary:    The main bundles for the Open-Xchange collaboration suite
#PreReq:       PREREQUIRES
Requires:     open-xchange-osgi >= @OXVERSION@

Obsoletes: open-xchange < 6.21.0, open-xchange-ajp, open-xchange-cache
Obsoletes: open-xchange-calendar, open-xchange-charset, open-xchange-common, open-xchange-config-cascade
Obsoletes: open-xchange-config-cascade-context, open-xchange-config-cascade-user, open-xchange-configread
Obsoletes: open-xchange-contactcollector, open-xchange-conversion, open-xchange-conversion-engine
Obsoletes: open-xchange-conversion-servlet, open-xchange-crypto, open-xchange-data-conversion-ical4j
Obsoletes: open-xchange-dataretention, open-xchange-genconf, open-xchange-genconf-mysql
Obsoletes: open-xchange-file-storage, open-xchange-file-storage-composition, open-xchange-file-storage-config
Obsoletes: open-xchange-file-storage-generic, open-xchange-file-storage-infostore, open-xchange-file-storage-json
Obsoletes: open-xchange-folder-json, open-xchange-frontend-uwa, open-xchange-frontend-uwa-json, open-xchange-global
Obsoletes: open-xchange-html, open-xchange-i18n, open-xchange-jcharset, open-xchange-logging, open-xchange-management
Obsoletes: open-xchange-modules-json, open-xchange-modules-model, open-xchange-modules-storage, open-xchange-monitoring
Obsoletes: open-xchange-proxy, open-xchange-proxy-servlet, open-xchange-publish-basic, open-xchange-publish-infostore-online
Obsoletes: open-xchange-push < 6.21.0, open-xchange-push-udp, open-xchange-secret < 6.21.0, open-xchange-secret-recovery
Obsoletes: open-xchange-secret-recovery-json, open-xchange-secret-recovery-mail, open-xchange-server, open-xchange-sessiond
Obsoletes: open-xchange-settings-extensions, open-xchange-sql, open-xchange-templating, open-xchange-templating-base
Obsoletes: open-xchange-threadpool, open-xchange-tx, open-xchange-user-json, open-xchange-xml

#
%description

Authors:
--------
    Open-Xchange
%prep
%setup -q
%build
%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=open-xchange-core -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}
%files
%defattr(-,root,root)
FILES
%doc ChangeLog
%changelog
* Tue Apr 17 2012 Sonja Krause-Harder  <sonja.krause-harder@open-xchange.com>
Internal release build for EDP drop #1
