
Name:          open-xchange-cluster-discovery-static
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 1
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for a cluster discovery service implementation based on a static configuration
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-cluster-discovery
Conflicts:     open-xchange-cluster-discovery-mdns

%description
 This package installs the OSGi bundle implementing the OSGi ClusterDiscoveryService. 
 The implementation uses a configuration file that specifies all nodes of the cluster.
 This cluster discovery module is mutually exclusive with any other cluster discovery module. 
 Only one cluster discovery module can be installed on the backend.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/static-cluster-discovery.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Oct 24 2012 Steffen Templin <steffen.templin@open-xchange.com>
Initial release
