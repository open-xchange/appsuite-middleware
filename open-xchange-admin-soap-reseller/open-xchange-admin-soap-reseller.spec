%define __jar_repack %{nil}

Name:          open-xchange-admin-soap-reseller
BuildArch:     noarch
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        SOAP interfaces for the reseller administration level
Autoreqprov:   no
Requires:       open-xchange-soap-cxf >= @OXVERSION@
Requires:	open-xchange-admin-reseller >= @OXVERSION@
Provides:	open-xchange-admin-plugin-reseller-soap = %{version}
Obsoletes:	open-xchange-admin-plugin-reseller-soap < %{version}

%description
This package installs the SOAP interfaces for the reseller level administration RMI interfaces. See the open-xchange-admin-reseller package
for a description of the reseller administration level. The SOAP interfaces allow a lot of programming languages for the reseller
administration clients while RMI only allows Java clients.

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
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
