%define __jar_repack %{nil}

Name:           open-xchange-cluster-upgrade-from-7102
BuildArch:      noarch
BuildRequires: ant
BuildRequires:  open-xchange-core
BuildRequires: java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 25
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module to invalidate cluster nodes running v7.10.2 of the Open-Xchange server (Hazelcast v3.11.x) during upgrade
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Server module to invalidate cluster nodes running v7.10.2 of the Open-Xchange server (Hazelcast v3.11.x) during upgrade

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
* Tue May 25 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-06-01 (5999)
* Tue May 18 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-05-17 (5993)
* Mon Apr 26 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-05-03 (5988)
* Mon Apr 12 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-04-19 (5981)
* Tue Mar 23 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-03-29 (5975)
* Wed Mar 10 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-03-15 (5972)
* Mon Feb 22 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-02-25 (5960)
* Tue Feb 02 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-02-08 (5945)
* Tue Jan 19 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-01-25 (5937)
* Tue Jan 05 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-01-11 (5930)
* Wed Dec 09 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-12-14 (5924)
* Mon Nov 16 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-11-23 (5905)
* Wed Nov 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-11-09 (5891)
* Tue Oct 20 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-10-26 (5888)
* Tue Oct 06 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-10-12 (5879)
* Wed Sep 23 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-09-29 (5869)
* Fri Sep 11 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-09-14 (5857)
* Mon Aug 24 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-08-24 (5842)
* Wed Aug 05 2020 Thorben Betten <thorben.betten@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Thorben Betten <thorben.betten@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Thorben Betten <thorben.betten@open-xchange.com>
First preview of 7.10.4 release
* Thu Jan 16 2020 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.4 release
* Thu Nov 28 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.3 release
* Wed Nov 20 2019 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
