
Name:           open-xchange-drive-comet
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-drive
BuildRequires:  open-xchange-grizzly
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Drive push implementation using Comet and using less system resources
Autoreqprov:    no
Requires:       open-xchange-drive >= @OXVERSION@
Requires:       open-xchange-grizzly >= @OXVERSION@

%description
This package should be installed if a real push implementation for the drive synchronization is wanted. This push implementation uses less
system resources by using the Grizzly application server which allows freeing threads although the request is still active.

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
* Mon Jul 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-15
* Thu Jul 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.2
* Mon Jan 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-07
* Tue Sep 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Ninth candidate for 7.4.0 release
* Thu Sep 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
