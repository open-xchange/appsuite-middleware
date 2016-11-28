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
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 17
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

%changelog
* Mon Nov 28 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-12-05 (3763)
* Sat Nov 12 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-11-21 (3731)
* Tue Nov 08 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-11-07 (3678)
* Wed Oct 26 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-08 (3699)
* Mon Oct 17 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-10-24 (3630)
* Thu Oct 06 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-10-10 (3597)
* Mon Sep 19 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-26 (3572)
* Mon Sep 19 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-08 (3580)
* Mon Sep 05 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-12 (3547)
* Mon Aug 22 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-29 (3522)
* Mon Aug 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-26 (3512)
* Mon Aug 08 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-15 (3490)
* Fri Jul 22 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-01 (3467)
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
