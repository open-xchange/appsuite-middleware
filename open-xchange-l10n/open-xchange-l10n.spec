
Name:          open-xchange-l10n
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 3
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Package containing Open-Xchange backend localization
Requires:      open-xchange-core >= @OXVERSION@

%description
Package containing Open-Xchange backend localization

Authors:
--------
    Open-Xchange

%package de-de
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for de_DE

%description de-de
Package containing Open-Xchange backend localization for de_DE

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package fr-fr
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for fr_FR

%description fr-fr
Package containing Open-Xchange backend localization for fr_FR

Authors:
--------
    Open-Xchange

%package cs-cz
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for cs_CZ
Provides:       open-xchange-lang-cs-cz = %{version}
Obsoletes:      open-xchange-lang-cs-cz <= %{version}

%description cs-cz
Package containing Open-Xchange backend localization for cs_CZ

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package es-es
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for es_ES
Provides:       open-xchange-lang-es-es = %{version}
Obsoletes:      open-xchange-lang-es-es <= %{version}

%description es-es
Package containing Open-Xchange backend localization for es_ES

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package fr-ca
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for fr_CA
Provides:       open-xchange-lang-fr-ca = %{version}
Obsoletes:      open-xchange-lang-fr-ca <= %{version}

%description fr-ca
Package containing Open-Xchange backend localization for fr_CA

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package hu-hu
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for hu_HU
Provides:       open-xchange-lang-hu-hu = %{version}
Obsoletes:      open-xchange-lang-hu-hu <= %{version}

%description hu-hu
Package containing Open-Xchange backend localization for hu_HU

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package it-it
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for it_IT
Provides:       open-xchange-lang-it-it = %{version}
Obsoletes:      open-xchange-lang-it-it <= %{version}

%description it-it
Package containing Open-Xchange backend localization for it_IT

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ja-jp
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for ja_JP
Provides:       open-xchange-lang-ja-jp = %{version}
Obsoletes:      open-xchange-lang-ja-jp <= %{version}

%description ja-jp
Package containing Open-Xchange backend localization for ja_JP

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package lv-lv
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for lv_LV
Provides:       open-xchange-lang-lv-lv = %{version}
Obsoletes:      open-xchange-lang-lv-lv <= %{version}

%description lv-lv
Package containing Open-Xchange backend localization for lv_LV

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package nl-nl
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for nl_NL
Provides:       open-xchange-lang-nl-nl = %{version}
Obsoletes:      open-xchange-lang-nl-nl <= %{version}

%description nl-nl
Package containing Open-Xchange backend localization for nl_NL

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pl-pl
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for pl_PL
Provides:       open-xchange-lang-pl-pl = %{version}
Obsoletes:      open-xchange-lang-pl-pl <= %{version}

%description pl-pl
Package containing Open-Xchange backend localization for pl_PL

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ro-ro
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for ro_RO
Provides:       open-xchange-lang-ro-ro = %{version}
Obsoletes:      open-xchange-lang-ro-ro <= %{version}

%description ro-ro
Package containing Open-Xchange backend localization for ro_RO

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package sk-sk
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for sk_SK
Provides:       open-xchange-lang-sk-sk = %{version}
Obsoletes:      open-xchange-lang-sk-sk <= %{version}

%description sk-sk
Package containing Open-Xchange backend localization for sk_SK

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package zh-cn
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for zh_CN
Provides:       open-xchange-lang-zh-cn = %{version}
Obsoletes:      open-xchange-lang-zh-cn <= %{version}

%description zh-cn
Package containing Open-Xchange backend localization for zh_CN

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package zh-tw
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for zh_TW
Provides:       open-xchange-lang-zh-tw = %{version}
Obsoletes:      open-xchange-lang-zh-tw <= %{version}

%description zh-tw
Package containing Open-Xchange backend localization for zh_TW

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
for LANG in cs_CZ de_DE es_ES fr_CA fr_FR hu_HU it_IT ja_JP lv_LV nl_NL pl_PL ro_RO sk_SK zh_CN zh_TW; do \
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

%files cs-cz
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*cs_CZ*

%files es-es
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*es_ES*

%files fr-ca
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_CA*

%files hu-hu
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*hu_HU*

%files it-it
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*it_IT*

%files ja-jp
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ja_JP*

%files lv-lv
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*lv_LV*

%files nl-nl
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*nl_NL*

%files pl-pl
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pl_PL*

%files ro-ro
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ro_RO*

%files sk-sk
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*sk_SK*

%files zh-cn
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_CN*

%files zh-tw
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_TW*

%changelog
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Wed Apr 25 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
