%define __jar_repack %{nil}

Name:          open-xchange-cassandra
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
%define        ox_release 19
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange Cassandra Service
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package provides connectivity to a Cassandra cluster via a Cassandra Service


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
* Mon Oct 29 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-11-05 (4933)
* Mon Oct 08 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-10-15 (4918)
* Tue Sep 25 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-10-01 (4897)
* Mon Sep 24 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-09-21 (4900)
* Mon Sep 10 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-09-17 (4882)
* Mon Aug 27 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-09-03 (4870)
* Wed Aug 15 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-08-20 (4863)
* Thu Aug 02 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-08-13 (4853)
* Fri Jul 20 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2018-07-25 (4835)
* Fri Jun 29 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
prepare for 7.10.0 release
* Fri May 19 2017 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First preview of 7.8.4 release
* Tue Dec 13 2016 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Initial release
