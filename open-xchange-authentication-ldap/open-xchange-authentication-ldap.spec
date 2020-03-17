%define __jar_repack %{nil}

Name:          open-xchange-authentication-ldap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for authenticating users using a LDAP server
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-authentication
Conflicts:     open-xchange-authentication-database
Conflicts:     open-xchange-authentication-imap

%description
This package installs the OSGi bundle implementing the OSGi AuthenticationService for the backend. The implementation uses a LDAP server to
authenticate login requests.
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
cp -rv --preserve=all ./opt %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions "/opt/open-xchange/etc/ldapauth.properties" root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/ldapauth.properties

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
