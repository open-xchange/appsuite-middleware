
Name:          open-xchange-admin-reseller
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-admin
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires: java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange Admin Reseller Plugin
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-reseller = %{version}
Obsoletes:     open-xchange-admin-plugin-reseller <= %{version}

%description
Open Xchange Admin Reseller Plugin

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
    CONFFILES="plugin/reseller.properties mysql/reseller.sql"
    if [ -e /opt/open-xchange/etc/admindaemon/plugin/reseller.properties ]; then
        mv /opt/open-xchange/etc/plugin/reseller.properties /opt/open-xchange/etc/plugin/reseller.properties.rpmnew
        mv /opt/open-xchange/etc/admindaemon/plugin/reseller.properties /opt/open-xchange/etc/plugin/reseller.properties
    fi
    if [ -e /opt/open-xchange/etc/admindaemon/mysql/reseller.sql ]; then
        mv /opt/open-xchange/etc/mysql/reseller.sql /opt/open-xchange/etc/mysql/reseller.sql.rpmnew
        mv /opt/open-xchange/etc/admindaemon/mysql/reseller.sql /opt/open-xchange/etc/mysql/reseller.sql
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%dir /opt/open-xchange/etc/mysql
%config(noreplace) /opt/open-xchange/etc/mysql/*

%changelog
* Fri Jun 15 2012 - jan.bauerdick@open-xchange.com
 - Initial packaging
