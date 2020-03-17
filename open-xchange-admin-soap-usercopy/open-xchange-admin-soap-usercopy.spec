%define __jar_repack %{nil}

Name:          open-xchange-admin-soap-usercopy
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       SOAP interface for extension to copy user into other contexts
AutoReqProv:   no
Requires:      open-xchange-soap-cxf >= @OXVERSION@
Requires:      open-xchange-admin-user-copy >= @OXVERSION@
Provides:      open-xchange-admin-plugin-user-copy-soap = %{version}
Obsoletes:     open-xchange-admin-plugin-user-copy-soap < %{version}

%description
This package installs the OSGi bundle that provides the administrative SOAP interface to copy users into other contexts. SOAP allows
administrative clients written in any programming language while RMI requires clients written in Java. For a description of copying users
into other contexts see the package description of package open-xchange-admin-user-copy.

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
