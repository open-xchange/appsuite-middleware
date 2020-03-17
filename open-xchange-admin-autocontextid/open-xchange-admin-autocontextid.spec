%define __jar_repack %{nil}

Name:          open-xchange-admin-autocontextid
BuildArch:     noarch
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Administrative extension to automatically create context identifiers
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-autocontextid = %{version}
Obsoletes:     open-xchange-admin-plugin-autocontextid < %{version}
Provides:      open-xchange-admin-plugin-autocontextid-client = %{version}
Obsoletes:     open-xchange-admin-plugin-autocontextid-client < %{version}

%description
This package adds the administrative OSGi bundle that creates for every newly created context a straight rising context identifier. Without
this extension an identifier must be given when creating a context.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
cp -rv --preserve=all ./opt %{buildroot}/

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
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*

%changelog
* Mon Jun 17 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.10.3 release
