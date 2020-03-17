%define __jar_repack %{nil}

Name:          open-xchange-dav
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange CardDAV and CalDAV implementation
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-pns-impl >= @OXVERSION@
Provides:      open-xchange-caldav = %{version}
Obsoletes:     open-xchange-caldav < %{version}
Provides:      open-xchange-carddav = %{version}
Obsoletes:     open-xchange-carddav < %{version}
Provides:      open-xchange-webdav-directory = %{version}
Obsoletes:     open-xchange-webdav-directory < %{version}
Provides:      open-xchange-webdav-acl = %{version}
Obsoletes:     open-xchange-webdav-acl < %{version}

%description
The Open-Xchange CardDAV and CalDAV implementation.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) /opt/open-xchange/etc/meta
%config(noreplace) /opt/open-xchange/etc/contextSets
/usr/share
%doc /usr/share/doc/open-xchange-dav/properties/

%changelog
* Mon Jun 17 2019 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
