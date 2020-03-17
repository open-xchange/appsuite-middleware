%define __jar_repack %{nil}

Name:          open-xchange-filestore-swift
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Filestore implementation storing files in a Swift storage
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for storing files in a Swift storage.

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
ox_update_permissions /opt/open-xchange/etc/filestore-swift.properties root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-swift.properties
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-filestore-swift/properties/

%changelog
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
