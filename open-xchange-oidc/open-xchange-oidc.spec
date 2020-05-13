%define __jar_repack %{nil}

Name:          open-xchange-oidc
BuildArch:     noarch
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
%define        ox_release 29
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange Server OpenId Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
The Open-Xchange Server OpenId Bundle.

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
%config(noreplace) /opt/open-xchange/etc/hazelcast/oidcAuthInfos.properties
%config(noreplace) /opt/open-xchange/etc/hazelcast/oidcLogoutInfos.properties

%changelog
* Mon May 04 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-05-11 (5717)
* Mon Apr 06 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-04-14 (5674)
* Wed Feb 26 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-03-02 (5624)
* Mon Feb 03 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-02-10 (5578)
* Mon Dec 09 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-12-09 (5507)
* Mon Nov 18 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-11-25 (5482)
* Tue Oct 08 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-10-14 (5437)
* Mon Sep 23 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-09-30 (5418)
* Mon Sep 02 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-09-09 (5395)
* Mon Aug 19 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-08-26 (5372)
* Tue Jul 23 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-07-29 (5339)
* Tue Jun 11 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-06-11 (5275)
* Mon May 13 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-05-14 (5246)
* Mon May 06 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-05-13 (5233)
* Tue Apr 23 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-04-29 (5209)
* Mon Mar 25 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-04-01 (5178)
* Tue Mar 12 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-03-01 (5147)
* Wed Feb 27 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-03-01 (5142)
* Mon Feb 18 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-02-25 (5131)
* Mon Feb 04 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-02-11 (5106)
* Wed Jan 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-01-29 (5116)
* Tue Jan 22 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-01-28 (5074)
* Mon Dec 10 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-12-17 (5017)
* Thu Nov 15 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-11-19 (4965)
* Tue Oct 30 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-11-05 (4932)
* Fri Oct 19 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-10-19 (4927)
* Mon Oct 08 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-10-15 (4917)
* Tue Sep 25 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-10-01 (4896)
* Mon Sep 10 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-09-17 (4881)
* Tue Sep 04 2018 Marcus Klein <marcus.klein@open-xchange.com>
Backport to 7.8.4
