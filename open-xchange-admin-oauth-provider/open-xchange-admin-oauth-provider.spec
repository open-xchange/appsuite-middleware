%define __jar_repack %{nil}

Name:          open-xchange-admin-oauth-provider
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The OAuth provider management interfaces
Autoreqprov:   no
Requires:      open-xchange-oauth-provider >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@

%description
This package adds the management interfaces for the OAuth 2.0 provider
feature.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/
(cd %{buildroot}/opt/open-xchange/lib/ && ln -s com.openexchange.oauth.provider.clt.jar com.openexchange.oauth.provider.rmi.jar)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient disableoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient enableoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient getoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient listoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient removeoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient revokeoauthclient)
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s createoauthclient updateoauthclient)

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
* Mon Jun 17 2019 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.3 release
