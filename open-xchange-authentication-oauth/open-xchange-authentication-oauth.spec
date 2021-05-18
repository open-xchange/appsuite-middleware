
Name:          open-xchange-authentication-oauth
BuildArch:     noarch
BuildRequires: ant
BuildRequires: open-xchange-core
BuildRequires: java-1.8.0-openjdk-devel
Version:       @OXVERSION@
%define        ox_release 24
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
* Tue May 18 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-05-17 (5993)
* Mon Apr 26 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-05-03 (5988)
* Mon Apr 12 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-04-19 (5981)
* Tue Mar 23 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-03-29 (5975)
* Wed Mar 10 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-03-15 (5972)
* Mon Feb 22 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-02-25 (5960)
* Tue Feb 02 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-02-08 (5945)
* Tue Jan 19 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-01-25 (5937)
* Tue Jan 05 2021 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2021-01-11 (5930)
* Wed Dec 09 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-12-14 (5924)
* Mon Nov 16 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-11-23 (5905)
* Wed Nov 04 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-11-09 (5891)
* Tue Oct 20 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-10-26 (5888)
* Tue Oct 06 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-10-12 (5879)
* Wed Sep 23 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-09-29 (5869)
* Fri Sep 11 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-09-14 (5857)
* Mon Aug 24 2020 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2020-08-24 (5842)
* Wed Aug 05 2020 Steffen Templin <steffen.templin@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Steffen Templin <steffen.templin@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Steffen Templin <steffen.templin@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Steffen Templin <steffen.templin@open-xchange.com>
First preview of 7.10.4 release
* Thu Jan 16 2020 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.4 release
* Thu Nov 28 2019 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Steffen Templin <steffen.templin@open-xchange.com>
First preview for 7.10.3 release
* Fri Oct 04 2019 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.3 release
