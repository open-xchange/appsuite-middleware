%define __jar_repack %{nil}

Name:          open-xchange-realtime-events
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-realtime-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 2
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Realtime event pubsub implementations
Autoreqprov:   no
Requires:      open-xchange-realtime-core >= @OXVERSION@

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
* Sat Oct 29 2016 Marc Arens <marc.arens@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Marc Arens <marc.arens@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Marc Arens <marc.arens@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Marc Arens <marc.arens@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Marc Arens <marc.arens@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.8.1 release
* Tue Oct 20 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 19 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Mon Oct 12 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Thu Oct 08 2015 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Marc Arens <marc.arens@open-xchange.com>
Sixth candidate for 7.8.0 release
* Wed Sep 30 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-09-28  (2767)
* Fri Sep 25 2015 Marc Arens <marc.arens@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Marc Arens <marc.arens@open-xchange.com>
Fourth candidate for 7.8.0 release
* Tue Sep 08 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Mon Sep 07 2015 Marc Arens <marc.arens@open-xchange.com>
Third candidate for 7.8.0 release
* Wed Sep 02 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Fri Aug 21 2015 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.8.0 release
* Mon Aug 17 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Wed Aug 05 2015 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 7.8.0
* Tue Aug 04 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Mon Mar 23 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Marc Arens <marc.arens@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Marc Arens <marc.arens@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Marc Arens <marc.arens@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Marc Arens <marc.arens@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Marc Arens <marc.arens@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-25
* Wed Feb 11 2015 Marc Arens <marc.arens@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Marc Arens <marc.arens@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Marc Arens <marc.arens@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-26
* Mon Jan 12 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-12
* Fri Dec 12 2014 Marc Arens <marc.arens@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Marc Arens <marc.arens@open-xchange.com>
Third candidate for 7.6.2 release
* Tue Dec 02 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.6.2 release
* Tue Nov 18 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-17
* Wed Nov 05 2014 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.8.0 release
* Fri Oct 31 2014 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.6.2 release
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
* Wed Sep 17 2014 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Marc Arens <marc.arens@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 7.6.1
* Thu May 30 2013 Marc Arens <marc.arens@open-xchange.com>
Initial build
