
Name:          open-xchange-authentication-application-storage-rdb
BuildArch:     noarch
BuildRequires: ant
BuildRequires: open-xchange-core
BuildRequires: java-1.8.0-openjdk-devel
Version:       @OXVERSION@
%define        ox_release 9
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Database-backed storage for application-specific passwords
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-authentication-application

%description
This package contains a database-backed storage and authenticator to allow separate passwords for external applications accessing the middleware APIs.  

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
* Fri Sep 11 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2020-09-14 (5857)
* Mon Aug 24 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2020-08-24 (5842)
* Wed Aug 05 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First preview of 7.10.4 release
* Fri Apr 17 2020 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.10.4 release
