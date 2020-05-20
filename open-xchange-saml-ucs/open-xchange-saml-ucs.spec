%define __jar_repack %{nil}

Name:          open-xchange-saml-ucs
BuildArch:     noarch
BuildRequires: ant
BuildRequires: java-1.8.0-openjdk-devel
BuildRequires: open-xchange-authentication-ucs-common
BuildRequires: open-xchange-saml-core
Version:       @OXVERSION@
%define        ox_release 1
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
* Wed May 20 2020 Felix Marx <felix.marx@open-xchange.com>
First preview of 7.10.4 release
* Thu Jan 16 2020 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.4 release
* Thu Nov 28 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Felix Marx <felix.marx@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.2 release
* Mon Oct 08 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.1 release
