%define __jar_repack %{nil}

Name:          open-xchange-sms-sipgate
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Send SMS messages via sipgate
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed to send SMS messages via sipgate.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions /opt/open-xchange/etc/sipgate.properties root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/sipgate.properties
/usr/share
%doc /usr/share/doc/open-xchange-sms-sipgate/properties/

%changelog
* Mon Jun 17 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.10.3 release
