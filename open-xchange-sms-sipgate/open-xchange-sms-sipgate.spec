%define __jar_repack %{nil}

Name:          open-xchange-sms-sipgate
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
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
BuildRequires: java-1_7_0-openjdk-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
%endif
Version:       @OXVERSION@
%define        ox_release 27
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Send SMS messages via sipgate
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed to send SMS messages via sipgate.

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
ox_update_permissions /opt/open-xchange/etc/sipgate.properties root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/sipgate.properties

%changelog
* Thu Apr 12 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-04-12 (4674)
* Tue Apr 03 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-04-03 (4642)
* Fri Mar 23 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-03-26 (4619)
* Mon Mar 12 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-03-12 (4602)
* Mon Feb 26 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-02-26 (4583)
* Mon Jan 29 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-02-05 (4555)
* Mon Jan 15 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-01-22 (4538)
* Tue Jan 02 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-01-08 (4516)
* Fri Dec 08 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for Patch 2017-12-11 (4473)
* Thu Nov 16 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-11-20 (4441)
* Tue Nov 14 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-11-15 (4448)
* Wed Oct 25 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-10-30 (4415)
* Mon Oct 23 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-10-29 (4425)
* Mon Oct 16 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-10-16 (4394)
* Wed Sep 27 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-10-02 (4377)
* Thu Sep 21 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-09-22 (4373)
* Tue Sep 12 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-09-18 (4354)
* Fri Sep 01 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-09-04 (4328)
* Mon Aug 14 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-08-21 (4318)
* Tue Aug 01 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-08-07 (4304)
* Mon Jul 17 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-07-24 (4285)
* Mon Jul 03 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-07-10 (4257)
* Wed Jun 21 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-06-26 (4233)
* Tue Jun 06 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-06-08 (4180)
* Fri May 19 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.1 release
* Thu Jan 14 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial release
