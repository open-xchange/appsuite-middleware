%define __jar_repack %{nil}

Name:          open-xchange-contact-storage-ldap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Contact storage provider using a LDAP server as backend
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-contacts-ldap = %{version}
Obsoletes:     open-xchange-contacts-ldap < %{version}

%description
Contact storage provider using a LDAP server as backend

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%dir %attr(750,root,open-xchange) /opt/open-xchange/etc/contact-storage-ldap
%attr(640,root,open-xchange) /opt/open-xchange/etc/contact-storage-ldap/*.example
%config(noreplace) /opt/open-xchange/etc/contact-storage-ldap/cache.properties

%changelog
* Mon Jun 17 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.10.3 release
