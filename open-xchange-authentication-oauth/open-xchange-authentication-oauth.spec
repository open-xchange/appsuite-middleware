
Name:          open-xchange-authentication-oauth
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
Summary:       OAuth 2.0 based AuthenticationService
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-authentication
Conflicts:     open-xchange-authentication-database
Conflicts:     open-xchange-authentication-imap
Conflicts:     open-xchange-authentication-ldap

%description
This package contains an AuthenticationService using the OAuth 2.0 Resource Owner Password Credentials Grant.

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
* Fri Apr 17 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-04-02 (5692)
* Mon Apr 06 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-04-14 (5677)
* Thu Mar 19 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-03-23 (5653)
* Fri Feb 28 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Steffen Templin <steffen.templin@open-xchange.com>
First preview for 7.10.3 release
* Fri Oct 04 2019 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.3 release
