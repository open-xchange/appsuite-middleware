%define __jar_repack %{nil}

Name:          open-xchange-geoip-maxmind
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 29
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange GeoIP service
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Obsoletes:     open-xchange-geoip < @OXVERSION@
Provides:      open-xchange-geoip = @OXVERSION@

%description
This package provides connectivity to a GeoIP service based on the MaxMind's GeoDatabase.


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

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Jun 24 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-06-30 (5780)
* Mon Jun 15 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-06-15 (5764)
* Fri May 15 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-05-26 (5741)
* Mon May 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-05-11 (5719)
* Mon Apr 06 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-04-14 (5676)
* Wed Apr 01 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-04-02 (5672)
* Thu Mar 19 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-03-23 (5652)
* Wed Feb 26 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-03-02 (5622)
* Wed Feb 12 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-02-10 (5571)
* Mon Jan 20 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-01-20 (5546)
* Tue Dec 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-12-09 (5509)
* Tue Nov 19 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-11-25 (5484)
* Mon Nov 04 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-11-11 (5473)
* Sat Nov 02 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-11-11 (5473)
* Tue Oct 22 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-10-28 (5461)
* Thu Oct 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-10-14 (5439)
* Mon Sep 23 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-09-30 (5420)
* Mon Sep 02 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-09-09 (5397)
* Mon Aug 19 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-08-26 (5374)
* Fri Aug 09 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-08-12 (5359)
* Mon Jul 22 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-07-29 (5341)
* Tue Jul 09 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-07-15 (5310)
* Thu Jun 27 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-07-01 (5291)
* Wed Jun 26 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-06-27 (5299)
* Thu Jun 06 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-06-11 (5261)
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.2 release
