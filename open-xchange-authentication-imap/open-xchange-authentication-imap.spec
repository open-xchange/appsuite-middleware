%define __jar_repack %{nil}

Name:          open-xchange-authentication-imap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for authenticating users using the IMAP server
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-authentication
Conflicts:     open-xchange-authentication-ldap
Conflicts:     open-xchange-authentication-database

%description
 This package installs the OSGi bundle implementing the OSGi AuthenticationService for the backend. The implementation uses some IMAP server
 to authenticate login requests.
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

%post
. /opt/open-xchange/lib/oxfunctions.sh
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # SoftwareChange_Request-3025
    ox_add_property USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP false /opt/open-xchange/etc/imapauth.properties

    # SoftwareChange_Request-3327
    ox_add_property USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP false /opt/open-xchange/etc/imapauth.properties

    # SoftwareChange_Request-3554
    ox_add_property LOWERCASE_FOR_CONTEXT_USER_LOOKUP false /opt/open-xchange/etc/imapauth.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
/usr/share
%doc /usr/share/doc/open-xchange-authentication-imap/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
