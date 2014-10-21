
Name:           open-xchange-drive
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 6
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module for Open-Xchange Drive file synchronization
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Server module for Open-Xchange Drive file synchronization

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

%post
. /opt/open-xchange/lib/oxfunctions.sh

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

# SoftwareChange_Request-1992
ox_set_property com.openexchange.capability.drive false /opt/open-xchange/etc/drive.properties

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/drive.properties
%config(noreplace) /opt/open-xchange/etc/contextSets/drive.yml
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Fri Oct 17 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-10
* Tue Sep 30 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-09-29
* Tue Sep 23 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 7.4.2
* Mon Jan 20 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 10 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-07
* Tue Sep 24 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Ninth candidate for 7.4.0 release
* Mon Sep 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Eighth candidate for 7.4.0 release
* Tue Aug 27 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth candidate for 7.4.0 release
* Mon Aug 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 7.4.0
* Fri Aug 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 09 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Initial release
