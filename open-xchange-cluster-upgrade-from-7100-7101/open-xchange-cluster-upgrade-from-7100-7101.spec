%define __jar_repack %{nil}

Name:           open-xchange-cluster-upgrade-from-7100-7101
BuildArch:      noarch
BuildRequires: ant
BuildRequires:  open-xchange-core
BuildRequires: java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 7
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module to invalidate cluster nodes running v7.10.0/v7.10.1 of the Open-Xchange server (Hazelcast v3.10.x) during upgrade
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Server module to invalidate cluster nodes running v7.10.0/v7.10.1 of the Open-Xchange server (Hazelcast v3.10.x) during upgrade

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
* Thu Oct 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.2 release
* Fri Feb 01 2019 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
