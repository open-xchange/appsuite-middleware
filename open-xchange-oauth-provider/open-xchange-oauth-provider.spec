%define __jar_repack %{nil}

Name:          open-xchange-oauth-provider
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The OAuth provider feature
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
OX App Suite is able to act as an OAuth 2.0 provider. Registered client
applications can access certain HTTP API calls in the name of users who
granted them access accordingly. This package adds the necessary core
functionality to serve those API calls.

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
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # SoftwareChange_Request-3098
    ox_add_property com.openexchange.oauth.provider.isAuthorizationServer true /opt/open-xchange/etc/oauth-provider.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/hazelcast/authcode.properties
%config(noreplace) /opt/open-xchange/etc/oauth-provider.properties

%changelog
* Mon Jun 17 2019 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.3 release
