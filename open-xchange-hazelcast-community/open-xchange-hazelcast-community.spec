%define __jar_repack %{nil}

Name:          open-xchange-hazelcast-community
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-osgi >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 17
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Community implementation of open-xchange-hazelcast
Autoreqprov:   no
Requires:      open-xchange-osgi >= @OXVERSION@
Provides:      open-xchange-hazelcast
Conflicts:     open-xchange-hazelcast-enterprise
Conflicts:     open-xchange-core < 7.10.1

%description
This package installs the community version of Hazelcast. The implementation uses the freely available version of Hazelcast with a
limited set of features (e. g. encrypted transport is missing).
This Hazelcast module is mutually exclusive with the enterprise version open-xchange-hazelcast-enterprise

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
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/bundles/com.hazelcast
/opt/open-xchange/bundles/com.hazelcast/*
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/com.hazelcast.ini

%changelog
* Thu Jul 09 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-07-13 (5804)
* Fri Jun 26 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-07-02 (5792)
* Fri Jun 26 2020 Martin Schneider <martin.schneider@open-xchange.com>
skip to 7.10.3-15
* Fri Feb 28 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Martin Schneider <martin.schneider@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Martin Schneider <martin.schneider@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Martin Schneider <martin.schneider@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Martin Schneider <martin.schneider@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Martin Schneider <martin.schneider@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Martin Schneider <martin.schneider@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Martin Schneider <martin.schneider@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Martin Schneider <martin.schneider@open-xchange.com>
prepare for 7.10.2 release
* Thu Oct 11 2018 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.1 release
* Mon Aug 13 2018 Martin Schneider <martin.schneider@open-xchange.com>
Initial release
