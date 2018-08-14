%define __jar_repack %{nil}

Name:          open-xchange-pns-transport-websockets
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 49
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Web Socket backed transport for Push Notification Service
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-pns-impl >= @OXVERSION@

%description
The Web Socket backed transport for Push Notification Service

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
. /opt/open-xchange/lib/oxfunctions.sh

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
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
* Mon Aug 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
