%define __jar_repack %{nil}

Name:          open-xchange-l10n
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
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

%description cs-cz
Package containing Open-Xchange backend localization for cs_CZ

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package da-dk
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for da_DK

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

%description es-es
Package containing Open-Xchange backend localization for es_ES

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package et-ee
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for et_EE

%description et-ee
Package containing Open-Xchange backend localization for et_EE
This localization package are driven by the community.

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

%description hi-in
Package containing Open-Xchange backend localization for hi_IN
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package hu-hu
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for hu_HU

%description hu-hu
Package containing Open-Xchange backend localization for hu_HU

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package it-it
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for it_IT

%description it-it
Package containing Open-Xchange backend localization for it_IT

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ja-jp
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ja_JP

%description ja-jp
Package containing Open-Xchange backend localization for ja_JP

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ko-ko
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ko_KO

%description ko-ko
Package containing Open-Xchange backend localization for ko_KO
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package lv-lv
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for lv_LV

%description lv-lv
Package containing Open-Xchange backend localization for lv_LV

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package nb-no
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for nb_NO

%description nb-no
Package containing Open-Xchange backend localization for nb_NO
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package nl-nl
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for nl_NL

%description nl-nl
Package containing Open-Xchange backend localization for nl_NL

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pl-pl
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for pl_PL

%description pl-pl
Package containing Open-Xchange backend localization for pl_PL

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pt-br
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for pt_BR

%description pt-br
Package containing Open-Xchange backend localization for pt_BR

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package pt-pt
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for pt_PT

%description pt-pt
Package containing Open-Xchange backend localization for pt_PT
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ro-ro
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ro_RO

%description ro-ro
Package containing Open-Xchange backend localization for ro_RO

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package ru-ru
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for ru_RU

%description ru-ru
Package containing Open-Xchange backend localization for ru_RU
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package sk-sk
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for sk_SK

%description sk-sk
Package containing Open-Xchange backend localization for sk_SK

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package sv-se
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for sv_SE
Provides:   open-xchange-l10n-sv-sv = %{version}
Obsoletes:  open-xchange-l10n-sv-sv < %{version}

%description sv-se
Package containing Open-Xchange backend localization for sv_SE

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package tr-tr
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for tr_TR

%description tr-tr
Package containing Open-Xchange backend localization for tr_TR
This localization package are driven by the community.

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package zh-cn
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for zh_CN

%description zh-cn
Package containing Open-Xchange backend localization for zh_CN

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package zh-tw
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for zh_TW

%description zh-tw
Package containing Open-Xchange backend localization for zh_TW

Authors:
--------
    Open-Xchange

#-------------------------------------------------------------------------------------

%package en-gb
Group:      Applications/Productivity
Summary:    Package containing Open-Xchange backend localization for en_GB

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
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files ca-es
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ca_ES*

%files cs-cz
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*cs_CZ*

%files da-dk
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*da_DK*

%files de-ch
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*de_CH*

%files de-de
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*de_DE*

%files el-gr
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*el_GR*

%files en-us
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/

%files es-es
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*es_ES*

%files es-mx
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*es_MX*

%files et-ee
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*et_EE*

%files eu-es
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*eu_ES*

%files fi-fi
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fi_FI*

%files fr-ca
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_CA*

%files fr-fr
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*fr_FR*

%files gl-es
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*gl_ES*

%files he-he
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*he_HE*

%files hi-in
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*hi_IN*

%files hu-hu
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*hu_HU*

%files it-it
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*it_IT*

%files ja-jp
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ja_JP*

%files ko-ko
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ko_KO*

%files lv-lv
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*lv_LV*

%files nb-no
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*nb_NO*

%files nl-nl
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*nl_NL*

%files pl-pl
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pl_PL*

%files pt-br
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pt_BR*

%files pt-pt
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*pt_PT*

%files ro-ro
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ro_RO*

%files ru-ru
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*ru_RU*

%files sk-sk
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*sk_SK*

%files sv-se
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*sv_SE*

%files tr-tr
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*tr_TR*

%files zh-cn
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_CN*

%files zh-tw
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*zh_TW*

%files en-gb
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/i18n/
/opt/open-xchange/i18n/*en_GB*

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.2 release
