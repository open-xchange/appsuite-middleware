%define __jar_repack %{nil}

Name:           open-xchange-upsell-multiple
BuildArch:	noarch
BuildRequires:  open-xchange-core
BuildRequires:  open-xchange-admin
Version:        @OXVERSION@
%define        ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The multiple Open-Xchange upsell multiple bundle
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:       open-xchange-core >= @OXVERSION@
Requires:       open-xchange-admin >= @OXVERSION@
Provides:       open-xchange-upsell-generic = %{version}
Obsoletes:      open-xchange-upsell-generic < %{version}

%description
The multiple Open-Xchange upsell multiple bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions "/opt/open-xchange/etc/upsell.properties" root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/upsell.properties
%config(noreplace) /opt/open-xchange/etc/settings/*

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
