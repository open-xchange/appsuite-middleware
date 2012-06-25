
Name:          open-xchange-l10n
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
Summary:       TODO
Requires:      open-xchange-core >= @OXVERSION@

%description
TODO

Authors:
--------
    Open-Xchange

%package de-de
Group:          Applications/Productivity
Summary:        German localization for the Open-Xchange backend

%description de-de
This package contains the German localization files for the Open-Xchange backend

Authors:
--------
    Open-Xchange

%package fr-fr
Group:          Applications/Productivity
Summary:        French localization for the Open-Xchange backend

%description fr-fr
This package contains the French localization files for the Open-Xchange backend

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
for LANG in de_DE fr_FR; do \
    PACKAGE_EXTENSION=$(echo ${LANG} | tr '[:upper:]_' '[:lower:]-'); \
    ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -Dlanguage=${LANG} -f build/build.xml clean build; \
done

%clean
%{__rm} -rf %{buildroot}

%files de-de
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*de_DE*

%files fr-fr
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_FR*

%changelog
* Wed Apr 25 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Initial release
