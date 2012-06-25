
Name:           open-xchange-xerces-ibm
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  java-sdk-ibm >= 1.6.0
Version:    	@OXVERSION@
%define         ox_release 0
Release:     	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Xerces Compat for IBM Java
Requires:       java-ibm >= 1.6.0
Provides:       open-xchange-xerces
Conflicts:      open-xchange-xerces-sun

%description
Xerces compatibility for OX installations on IBM JVM.

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
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Apr 18 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
