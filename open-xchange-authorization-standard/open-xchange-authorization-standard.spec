%define __jar_repack %{nil}

Name:          open-xchange-authorization-standard
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module implementing the default authorization
Autoreqprov:   no
Provides:      open-xchange-authorization
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundle publishing the OSGi AuthorizationService. This bundle implements the default authorization method
for the backend. When a user got authenticated a second step is to verify that he is authorized to use Open-Xchange currently. The default
implementation checks wether the context of the user is enabled.

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
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.2 release
