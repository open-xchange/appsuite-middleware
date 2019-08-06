%define __jar_repack %{nil}

Name:          open-xchange-antivirus
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
%define        ox_release 10
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Anti-Virus
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package contains the ICAP client and the Anti-Virus service implementation

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
* Tue Aug 06 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-08-12 (5359)
* Mon Jul 22 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-07-29 (5341)
* Tue Jul 09 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-07-15 (5310)
* Thu Jun 27 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-07-01 (5291)
* Wed Jun 26 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-06-27 (5299)
* Thu Jun 06 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Build for patch 2019-06-11 (5261)
* Fri May 10 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First preview for 7.10.2 release
* Tue Dec 11 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Initial packaging for the Anti-Virus service
