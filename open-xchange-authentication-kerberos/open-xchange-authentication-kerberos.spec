%define __jar_repack %{nil}

Name:          open-xchange-authentication-kerberos
BuildArch:     noarch
BuildRequires: open-xchange-sessionstorage-hazelcast
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for authenticating users in a Kerberos domain
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      sed
Provides:      open-xchange-authentication
Conflicts:     open-xchange-authentication-database
Conflicts:     open-xchange-authentication-imap
Conflicts:     open-xchange-authentication-ldap

%description
This package installs the OSGi bundle implementing the OSGi AuthenticationService for the backend. The implementation uses the Kerberos
domain controller (KDC) of the domain to authenticate login requests. Additionally this implementation is able to verify the service ticket
sent by the browser from the desktop login.
This authentication module is mutually exclusive with any other authentication module. Only one authentication module can be installed on
the backend.

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
%config(noreplace) /opt/open-xchange/etc
/usr/share
%doc /usr/share/doc/open-xchange-authentication-kerberos/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
