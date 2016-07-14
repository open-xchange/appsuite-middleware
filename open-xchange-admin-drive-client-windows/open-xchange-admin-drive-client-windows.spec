%define __jar_repack %{nil}

Name:          open-xchange-admin-drive-client-windows
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-drive-client-windows
BuildRequires: open-xchange-admin
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The windows drive client branding configuration CLT
Autoreqprov:   no
Requires:      open-xchange-drive-client-windows >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@

%description
This package adds the branding configuration command-line tools for the windows drive client updater.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
* Tue Jan 26 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.1 release
* Tue Nov 17 2015 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial packaging
