%define __jar_repack %{nil}

Name:           open-xchange-file-storage-googledrive
BuildArch:      noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires:  ant
%else
BuildRequires:  ant-nodeps
%endif
BuildRequires:  open-xchange-core
BuildRequires:  open-xchange-oauth
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:        @OXVERSION@
%define         ox_release 50
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend Google Drive file storage extension
Autoreqprov:   no
Requires:       open-xchange-core >= @OXVERSION@
Requires:       open-xchange-oauth >= @OXVERSION@
Provides:       open-xchange-file-storage-googledrive = %{version}

%description
Adds Google Drive file storage service to the backend installation.

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
* Wed Aug 15 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-20 (4861)
* Mon Jun 18 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-06-25 (4790)
* Fri Apr 20 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-04-23 (4669)
* Mon Mar 19 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-03-26 (4618)
* Mon Mar 05 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-03-12 (4601)
* Mon Feb 19 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-26 (4582)
* Mon Jan 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-05 (4554)
* Mon Jan 15 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-01-22 (4537)
* Tue Jan 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-01-08 (4515)
* Fri Dec 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-12-11 (4472)
* Thu Nov 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-11-20 (4440)
* Tue Nov 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-11-15 (4447)
* Wed Oct 25 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-30 (4414)
* Mon Oct 23 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-29 (4426)
* Mon Oct 09 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-10-16 (4393)
* Wed Sep 27 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-02 (4376)
* Mon Sep 11 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-09-18 (4353)
* Wed Aug 30 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-09-04 (4327)
* Mon Aug 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-21 (4317)
* Mon Jul 31 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-07 (4303)
* Mon Jul 17 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-07-24 (4284)
* Wed Jul 05 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-07-10 (4256)
* Tue Jun 27 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-06-27 (4244)
* Mon Jun 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-06-26 (4223)
* Tue Jun 06 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-06-12 (4186)
* Fri May 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-29 (4161)
* Fri May 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-19 (4176)
* Mon May 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-15 (4132)
* Fri Apr 21 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-02 (4113)
* Wed Apr 12 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-18 (4084)
* Fri Mar 31 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-03 (4050)
* Tue Mar 28 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-27 (4066)
* Thu Mar 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-20 (4016)
* Mon Mar 06 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-06 (3985)
* Fri Feb 24 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-24 (3994)
* Wed Feb 22 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-22 (3969)
* Tue Feb 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-20 (3952)
* Tue Jan 31 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-06 (3918)
* Thu Jan 26 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-26 (3925)
* Wed Jan 18 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-23 (3879)
* Wed Jan 04 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-09 (3849)
* Tue Dec 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-23 (3857)
* Wed Dec 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-19 (3814)
* Tue Dec 13 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-14 (3806)
* Tue Dec 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-12 (3775)
* Fri Nov 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.8.3 release
* Tue Nov 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.1 release
* Mon Oct 19 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Thu Oct 08 2015 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Thorben Betten <thorben.betten@open-xchange.com>
Sixth candidate for 7.8.0 release
* Fri Sep 25 2015 Thorben Betten <thorben.betten@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.8.0 release
* Mon Sep 07 2015 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.8.0 release
* Fri Aug 21 2015 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.0 release
* Wed Aug 05 2015 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.8.0
* Fri Dec 12 2014 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.6.2 release
* Tue Dec 02 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.6.2 release
* Tue Nov 18 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-11-17
* Wed Nov 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.0 release
* Fri Oct 31 2014 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.6.2 release
* Mon Oct 27 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Thorben Betten <thorben.betten@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Thorben Betten <thorben.betten@open-xchange.com>
Third release candidate for 7.6.1
* Wed Sep 17 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.6.1
* Thu Jun 26 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.6.1
