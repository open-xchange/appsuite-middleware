
Name:          open-xchange-realtime-events-json
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-realtime-json
BuildRequires: open-xchange-realtime-events
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 33
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Realtime event pubsub implementations via a JSON channel
Autoreqprov:   no
Requires:      open-xchange-realtime-events >= @OXVERSION@
Requires:      open-xchange-realtime-json >= @OXVERSION@

%description


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
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Jun 24 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Thu Apr 30 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Tue Apr 14 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Thu Mar 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Mon Mar 23 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-16
* Thu Feb 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-23
* Mon Feb 23 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-25
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-09
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-26
* Mon Jan 12 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-12
* Mon Dec 08 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-15
* Tue Dec 02 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Tue Nov 18 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-17
* Mon Oct 27 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Marc Arens <marc.arens@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Marc Arens <marc.arens@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Marc Arens <marc.arens@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 16 2014 Marc Arens <marc.arens@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 7.6.1
* Thu May 30 2013 Marc Arens <marc.arens@open-xchange.com>
Initial build
