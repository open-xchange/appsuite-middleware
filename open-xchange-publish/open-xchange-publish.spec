
Name:          open-xchange-publish
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires: java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-sun-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange backend publish extension
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-publish-json = %{version}
Obsoletes:     open-xchange-publish-json <= %{version}
Provides:      open-xchange-publish-microformats = %{version}
Obsoletes:     open-xchange-publish-microformats <= %{version}
Provides:      open-xchange-templating-json = %{version}
Obsoletes:     open-xchange-templating-json <= %{version}

%description
Add the feature to publish content to the backend installation.

Authors:
--------
    Open-Xchange
    
%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    if [ -e /opt/open-xchange/etc/groupware/microformatWhitelist.properties ]; then
        mv /opt/open-xchange/etc/microformatWhitelist.properties /opt/open-xchange/etc/microformatWhitelist.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/microformatWhitelist.properties /opt/open-xchange/etc/microformatWhitelist.properties
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/templates
%config(noreplace) /opt/open-xchange/etc/*
%config(noreplace) /opt/open-xchange/templates/*

%changelog
