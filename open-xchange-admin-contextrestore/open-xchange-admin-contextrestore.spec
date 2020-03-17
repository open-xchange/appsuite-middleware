%define __jar_repack %{nil}

Name:          open-xchange-admin-contextrestore
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Extension to restore context data from a database dump
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-contextrestore = %{version}
Obsoletes:     open-xchange-admin-plugin-contextrestore < %{version}

%description
This package adds the OSGi bundle that allows to restore a complete context from a MySQL database dump file. Only the table rows for the
given context are extracted from the database dump file and inserted into the currently registered database servers. This can be used to
restore accidentially deleted contexts.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/
(cd %{buildroot}/opt/open-xchange/lib/ && ln -s ../bundles/com.openexchange.admin.contextrestore.rmi.jar)

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*

%changelog
* Mon Jun 17 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.10.3 release
