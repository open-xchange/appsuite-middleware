%define __jar_repack %{nil}

Name:          open-xchange-admin-soap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange administrative SOAP interface
AutoReqProv:   no
Requires:      open-xchange-soap-cxf >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@

%description
This package installs the OSGi bundles that provide a SOAP interface to administer the backend installation. This is an extension to the
administrative RMI interfaces. RMI can only be used with Java while SOAP enables a lot of programming languages for administrative clients.
This package contains the SOAP interfaces for registering, changing and deleting servers, databases and filestores. It also add the
interfaces for creating, changing and deleting contexts, users, groups and resources.

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
