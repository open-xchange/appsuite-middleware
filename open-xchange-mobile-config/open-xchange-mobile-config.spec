
Name:           open-xchange-mobile-config
BuildArch: 	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  java-devel >= 1.6.0
# TODO: version not hardcoded in spec file
Version:	@OXVERSION@
%define		ox_release 9
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        Creative Commons Attribution-Noncommercial-Share Alike 2.5 Generic
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Config files for the Open-Xchange Mobile UI
Requires:       open-xchange-core >= @OXVERSION@

%description
 This package needs to be installed on the backend hosts of a cluster installation. It adds configuration files to the backend allowing the
 administrator to define some defaults for the mobile web app. Additionally it adds configuration paths on the backend for the Mobile Web Interface
 that allows to store end user preferences.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/settings
%dir /opt/open-xchange/etc/meta
%config(noreplace) /opt/open-xchange/etc/settings/*
%config(noreplace) /opt/open-xchange/etc/meta/*

%changelog
* Mon Mar 04 2013 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Build for patch 2013-03-08
* Tue Feb 26 2013 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Build for patch 2013-02-22
* Mon Jan 21 2013 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Build for patch 2013-01-24
* Thu Jan 03 2013 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Build for public patch 2013-01-15
* Wed Oct 10 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
prepare for 6.22.0
* Tue Jul 03 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build for EDP drop #2
* Mon May 07 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Bugfixbuild
* Mon May 07 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Bugfixbuild for ox.io
* Tue Apr 03 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build for 1.1.0
* Tue Apr 03 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build for 1.1.0
* Tue Apr 03 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build for 1.1.0
* Thu Mar 29 2012 Marcus Klein <jenkins@jenkins.netline.de>
Next test build
* Thu Mar 29 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build
* Wed Mar 28 2012 Marcus Klein <jenkins@hudson-slave-1.netline.de>
Release build
* Thu Feb 16 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial 1.1 release.
