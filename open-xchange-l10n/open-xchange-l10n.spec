
Name:          open-xchange-l10n
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 13
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Package containing Open-Xchange backend localization
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
Package containing Open-Xchange backend localization

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ca-es
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ca_ES
Provides:       open-xchange-lang-community-ca-es = %{version}
Obsoletes:      open-xchange-lang-community-ca-es < %{version}

%description ca-es
Package containing Open-Xchange backend localization for ca_ES
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package cs-cz
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for cs_CZ
Provides:       open-xchange-lang-cs-cz = %{version}
Obsoletes:      open-xchange-lang-cs-cz < %{version}

%description cs-cz
Package containing Open-Xchange backend localization for cs_CZ

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package da-dk
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for da_DK
Provides:       open-xchange-lang-community-da-dk = %{version}
Obsoletes:      open-xchange-lang-community-da-dk < %{version}

%description da-dk
Package containing Open-Xchange backend localization for da_DK

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package de-ch
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for de_CH

%description de-ch
Package containing Open-Xchange backend localization for de_CH

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package de-de
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for de_DE

%description de-de
Package containing Open-Xchange backend localization for de_DE

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package el-gr
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for el_GR
Provides:       open-xchange-lang-community-el-gr = %{version}
Obsoletes:      open-xchange-lang-community-el-gr < %{version}

%description el-gr
Package containing Open-Xchange backend localization for el_GR
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package en-us
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for en_US

%description en-us
Package containing Open-Xchange backend localization for en_US

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package es-es
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for es_ES
Provides:       open-xchange-lang-es-es = %{version}
Obsoletes:      open-xchange-lang-es-es < %{version}

%description es-es
Package containing Open-Xchange backend localization for es_ES

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package es-mx
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for es_MX

%description es-mx
Package containing Open-Xchange backend localization for es_MX

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package eu-es
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for eu_ES
Provides:       open-xchange-lang-community-eu-es = %{version}
Obsoletes:      open-xchange-lang-community-eu-es < %{version}

%description eu-es
Package containing Open-Xchange backend localization for eu_ES
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package fi-fi
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for fi_FI

%description fi-fi
Package containing Open-Xchange backend localization for fi_FI

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package fr-ca
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for fr_CA
Provides:       open-xchange-lang-fr-ca = %{version}
Obsoletes:      open-xchange-lang-fr-ca < %{version}

%description fr-ca
Package containing Open-Xchange backend localization for fr_CA

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

#-------------------------------------------------------------------------------------

%package gl-es
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for gl_ES
Provides:       open-xchange-lang-community-gl-es = %{version}
Obsoletes:      open-xchange-lang-community-gl-es < %{version}

%description gl-es
Package containing Open-Xchange backend localization for gl_ES
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package he-he
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for he_HE
Provides:       open-xchange-lang-community-he-he = %{version}
Obsoletes:      open-xchange-lang-community-he-he < %{version}

%description he-he
Package containing Open-Xchange backend localization for he_HE
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package hi-in
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for hi_IN
Provides:       open-xchange-lang-community-hi-in = %{version}
Obsoletes:      open-xchange-lang-community-hi-in < %{version}

%description hi-in
Package containing Open-Xchange backend localization for hi_IN
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package hu-hu
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for hu_HU
Provides:       open-xchange-lang-hu-hu = %{version}
Obsoletes:      open-xchange-lang-hu-hu < %{version}

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
Obsoletes:      open-xchange-lang-it-it < %{version}

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
Obsoletes:      open-xchange-lang-ja-jp < %{version}

%description ja-jp
Package containing Open-Xchange backend localization for ja_JP

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ko-ko
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ko_KO
Provides:       open-xchange-lang-community-ko-ko = %{version}
Obsoletes:      open-xchange-lang-community-ko-ko < %{version}

%description ko-ko
Package containing Open-Xchange backend localization for ko_KO
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package lv-lv
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for lv_LV
Provides:       open-xchange-lang-lv-lv = %{version}
Obsoletes:      open-xchange-lang-lv-lv < %{version}

%description lv-lv
Package containing Open-Xchange backend localization for lv_LV

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package nb-no
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for nb_NO
Provides:       open-xchange-lang-community-no-nb = %{version}
Obsoletes:      open-xchange-lang-community-no-nb < %{version}

%description nb-no
Package containing Open-Xchange backend localization for nb_NO
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package nl-nl
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for nl_NL
Provides:       open-xchange-lang-nl-nl = %{version}
Obsoletes:      open-xchange-lang-nl-nl < %{version}

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
Obsoletes:      open-xchange-lang-pl-pl < %{version}

%description pl-pl
Package containing Open-Xchange backend localization for pl_PL

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pt-br
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for pt_BR
Provides:       open-xchange-lang-community-pt-br = %{version}
Obsoletes:      open-xchange-lang-community-pt-br < %{version}

%description pt-br
Package containing Open-Xchange backend localization for pt_BR

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pt-pt
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for pt_PT
Provides:       open-xchange-lang-community-pt-pt = %{version}
Obsoletes:      open-xchange-lang-community-pt-pt < %{version}

%description pt-pt
Package containing Open-Xchange backend localization for pt_PT
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ro-ro
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for ro_RO
Provides:       open-xchange-lang-ro-ro = %{version}
Obsoletes:      open-xchange-lang-ro-ro < %{version}

%description ro-ro
Package containing Open-Xchange backend localization for ro_RO

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ru-ru
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ru_RU
Provides:       open-xchange-lang-community-ru-ru = %{version}
Obsoletes:      open-xchange-lang-community-ru-ru < %{version}

%description ru-ru
Package containing Open-Xchange backend localization for ru_RU
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package sk-sk
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for sk_SK
Provides:       open-xchange-lang-sk-sk = %{version}
Obsoletes:      open-xchange-lang-sk-sk < %{version}

%description sk-sk
Package containing Open-Xchange backend localization for sk_SK

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package sv-se
Group:          Applications/Productivity
Summary:        Package containing Open-Xchange backend localization for sv_SE
Provides:       open-xchange-lang-community-sv-sv = %{version}
Obsoletes:      open-xchange-lang-community-sv-sv < %{version}
Provides:       open-xchange-l10n-sv-sv = %{version}
Obsoletes:      open-xchange-l10n-sv-sv < %{version}

%description sv-se
Package containing Open-Xchange backend localization for sv_SE

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package tr-tr
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for tr_TR
Provides:       open-xchange-lang-community-tr-tr = %{version}
Obsoletes:      open-xchange-lang-community-tr-tr < %{version}

%description tr-tr
Package containing Open-Xchange backend localization for tr_TR
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

#%package vi-vi
#Group:      Applications/Productivity
#Summary:    Package containing Open-Xchange backend localization for vi_VI
#Provides:       open-xchange-lang-community-vi-vi = %{version}
#Obsoletes:      open-xchange-lang-community-vi-vi < %{version}
#
#%description vi-vi
#Package containing Open-Xchange backend localization for vi_VI
#This localization package are driven by the community.
#
#Authors:
#--------
#    Open-Xchange

#-------------------------------------------------------------------------------------

%package zh-cn
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for zh_CN
Provides:       open-xchange-lang-zh-cn = %{version}
Obsoletes:      open-xchange-lang-zh-cn < %{version}

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
Obsoletes:      open-xchange-lang-zh-tw < %{version}

%description zh-tw
Package containing Open-Xchange backend localization for zh_TW

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package en-gb
Group:		Applications/Productivity
Summary:	Package containing Open-Xchange backend localization for en_GB

%description en-gb
Package containing Open-Xchange backend localization for en_GB

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
for LANG in ca_ES cs_CZ da_DK de_CH de_DE el_GR en_US es_ES es_MX eu_ES fi_FI fr_CA fr_FR he_HE hu_HU it_IT ja_JP ko_KO lv_LV nb_NO nl_NL pl_PL pt_BR pt_PT ro_RO ru_RU sk_SK sv_SE tr_TR zh_CN zh_TW en_GB; do \
    ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -Dlanguage=${LANG} -f build/build.xml clean build; \
done

%clean
%{__rm} -rf %{buildroot}

%files ca-es
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ca_ES*

%files cs-cz
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*cs_CZ*

%files da-dk
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*da_DK*

%files de-ch
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*de_CH*

%files de-de
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*de_DE*

%files el-gr
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*el_GR*

%files en-us
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/

%files es-es
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*es_ES*

%files es-mx
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*es_MX*

%files eu-es
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*eu_ES*

%files fi-fi
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fi_FI*

%files fr-ca
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_CA*

%files fr-fr
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_FR*

%files he-he
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*he_HE*

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

%files ko-ko
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ko_KO*

%files lv-lv
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*lv_LV*

%files nb-no
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*nb_NO*

%files nl-nl
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*nl_NL*

%files pl-pl
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pl_PL*

%files pt-br
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pt_BR*

%files pt-pt
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pt_PT*

%files ro-ro
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ro_RO*

%files ru-ru
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ru_RU*

%files sk-sk
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*sk_SK*

%files sv-se
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*sv_SE*

%files tr-tr
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*tr_TR*

%files zh-cn
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_CN*

%files zh-tw
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_TW*

%files en-gb
%defattr(-,root,root)
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*en_GB*

%changelog
* Thu Jun 16 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-06-27 (3358)
* Wed Jun 01 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-06-06 (3315)
* Tue May 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-05-09 (3270)
* Tue Apr 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-04-25 (3237)
* Mon Mar 21 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-29 (3187)
* Tue Mar 08 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-14 (3147)
* Mon Feb 22 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-29 (3120)
* Wed Feb 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-08 (3072)
* Tue Jan 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-25 (3030)
* Fri Jan 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-14 (3023)
* Thu Jan 07 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-13 (2972)
* Tue Dec 01 2015 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.6.3 release
* Mon Oct 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.6.3 release
* Tue Oct 20 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Wed Sep 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-28  (2767)
* Tue Sep 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Wed Sep 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Mon Aug 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Tue Aug 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Wed Mar 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.3
* Mon Mar 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Marcus Klein <marcus.klein@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-25
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Wed Feb 11 2015 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-26
* Wed Jan 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-29
* Mon Jan 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Mon Jan 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Fri Dec 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.6.2 release
* Thu Dec 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-09
* Tue Dec 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.6.2 release
* Thu Nov 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Wed Nov 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-21
* Tue Nov 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Tue Nov 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-10
* Fri Oct 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Mon Jan 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.2
* Wed Jan 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-22
* Mon Jan 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-29
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Tue Oct 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-11
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-15
* Thu Oct 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-07
* Thu Sep 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-23
* Tue Sep 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Ninth candidate for 7.4.0 release
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Thu Sep 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-05
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-26
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.4.0 release
* Fri Aug 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-30
* Wed Aug 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-03
* Tue Aug 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.0
* Mon Jul 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-22
* Wed Jul 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Wed Apr 25 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
