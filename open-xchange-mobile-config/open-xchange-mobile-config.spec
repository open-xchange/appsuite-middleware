
Name:           open-xchange-mobile-config
BuildArch: 	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  java-devel >= 1.6.0
# TODO: version not hardcoded in spec file
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        Creative Commons Attribution-Noncommercial-Share Alike 2.5 Generic
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Config files for the Open-Xchange Mobile UI
Requires:       open-xchange-core >= @OXVERSION@

%description
 This package needs to be installed on the backend hosts of a cluster installation. It adds configuration files to the backend allowing the
 administrator to define some defaults for the mobile web app. Additionally it adds configuration paths on the backend for the Mobile Web Interface
 that allows to store end user preferences.

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
%dir /opt/open-xchange/etc/settings
%dir /opt/open-xchange/etc/meta
%config(noreplace) /opt/open-xchange/etc/settings/*
%config(noreplace) /opt/open-xchange/etc/meta/*

%changelog
