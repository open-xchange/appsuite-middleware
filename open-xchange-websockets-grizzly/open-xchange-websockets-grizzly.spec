%define __jar_repack %{nil}

Name:          open-xchange-websockets-grizzly
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Enhances Open-Xchange Server by Web Sockets
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-grizzly >= @OXVERSION@

%description
Enhances Open-Xchange Server by Web Sockets

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

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
%config(noreplace) /opt/open-xchange/etc/hazelcast/*

%changelog
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
