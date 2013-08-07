
Name:          open-xchange-cluster-discovery-mdns
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 3
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for a cluster discovery service implementation based on Multicast
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-cluster-discovery
Conflicts:     open-xchange-cluster-discovery-static

%description
 This package installs the OSGi bundle implementing the OSGi ClusterDiscoveryService. 
 The implementation configures hazelcast for multicast cluster discovery to find all nodes within the cluster.
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
%config(noreplace) /opt/open-xchange/etc/mdns-cluster-discovery.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Tue Aug 06 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Steffen Templin <steffen.templin@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Steffen Templin <steffen.templin@open-xchange.com>
Feature freeze for 7.2.2 release
* Mon Jun 10 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Steffen Templin <steffen.templin@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Steffen Templin <steffen.templin@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Steffen Templin <steffen.templin@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Steffen Templin <steffen.templin@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Steffen Templin <steffen.templin@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-03-07
* Fri Mar 01 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Mon Feb 25 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Steffen Templin <steffen.templin@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-01-28
* Tue Jan 15 2013 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.0.1
* Fri Dec 28 2012 Steffen Templin <steffen.templin@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Steffen Templin <steffen.templin@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Steffen Templin <steffen.templin@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Steffen Templin <steffen.templin@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Steffen Templin <steffen.templin@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Steffen Templin <steffen.templin@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 6.22.1
* Wed Oct 24 2012 Steffen Templin <steffen.templin@open-xchange.com>
Initial release
* Mon Sep 03 2012 Steffen Templin <steffen.templin@open-xchange.com>
prepare for next EDP drop
