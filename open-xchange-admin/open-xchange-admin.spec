
Name:          open-xchange-admin
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange backend administration extension
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-admin-plugin-hosting = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting <= %{version}
Provides:      open-xchange-admin-lib = %{version}
Obsoletes:     open-xchange-admin-lib <= %{version}
Provides:      open-xchange-admin-plugin-hosting-client = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-client <= %{version}
Provides:      open-xchange-admin-plugin-hosting-doc = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-doc <= %{version}
Provides:      open-xchange-admin-client = %{version}
Obsoletes:     open-xchange-admin-client <= %{version}
Provides:      open-xchange-admin-plugin-hosting-lib = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-lib <= %{version}
Provides:      open-xchange-admin-doc = %{version}
Obsoletes:     open-xchange-admin-doc <= %{version}

%description
This package installs the OSGi bundles to the backend that provide the RMI interface to administer the installation.

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
    CONFFILES="AdminDaemon.properties Group.properties ModuleAccessDefinitions.properties RMI.properties Resource.properties Sql.properties User.properties mpasswd plugin/hosting.properties"
    for FILE in ${CONFFILES}; do
        if [ -e /opt/open-xchange/etc/admindaemon/${FILE} ]; then
            mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
            mv /opt/open-xchange/etc/admindaemon/${FILE} /opt/open-xchange/etc/${FILE}
        fi
    done
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
/opt/open-xchange/etc/mysql
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) %attr(750,open-xchange,root) /opt/open-xchange/etc/mpasswd
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
* Tue Apr 17 2012 Sonja Krause-Harder  <sonja.krause-harder@open-xchange.com>
Internal release build for EDP drop #1
