%define __jar_repack %{nil}

Name:          open-xchange-saml-ucs
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-authentication-ucs-common
BuildRequires: open-xchange-saml-core
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for authenticating users on a Univention Corporate Server installation via SAML
Autoreqprov:   no
Requires:      open-xchange-authentication-ucs-common >= @OXVERSION@
Requires:      open-xchange-saml-core >= @OXVERSION@

%description
This package installs the OSGi bundle implementing the OSGi SamlBackend for the backend. The implementation uses Univention
Corporate Server to authenticate login requests.

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
/opt/open-xchange/bundles/com.openexchange.saml.ucs.jar
/opt/open-xchange/osgi/bundle.d/com.openexchange.saml.ucs.ini

%changelog
* Tue Apr 14 2020 Marcus Klein <marcus.klein@open-xchange.com>
initial packaging
