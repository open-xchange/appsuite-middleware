
Name:          open-xchange-admin-soap-usercopy
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-soap-cxf
BuildRequires: open-xchange-admin-user-copy
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 1
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open Xchange Admin User Copy Bundle SOAP server
Requires:       open-xchange-soap-cxf >= @OXVERSION@
Requires:	open-xchange-admin-user-copy >= @OXVERSION@
Provides:       open-xchange-admin-plugin-user-copy-soap = %{version}
Obsoletes:      open-xchange-admin-plugin-user-copy-soap <= %{version}

%description
Open Xchange Admin User Copy Bundle SOAP server

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
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue Jun 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
