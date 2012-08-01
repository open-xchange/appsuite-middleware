
Name:          open-xchange-authentication-ldap
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange LDAP authentication
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-authentication
Conflicts:     open-xchange-authentication-database
Conflicts:     open-xchange-authentication-imap

%description
This package implements an authentication mechanism using an LDAP server.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    if [ -e /opt/open-xchange/etc/groupware/ldapauth.properties ]; then
        mv /opt/open-xchange/etc/ldapauth.properties /opt/open-xchange/etc/ldapauth.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/ldapauth.properties /opt/open-xchange/etc/ldapauth.properties.properties
    fi

    ox_update_permissions "/opt/open-xchange/etc/ldapauth.properties" root:open-xchange 640
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/ldapauth.properties

%changelog
* Tue Apr 17 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
