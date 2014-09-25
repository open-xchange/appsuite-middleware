
Name:          open-xchange-xing-json
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-oauth
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 2
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       This package provides API calls for XING-related actions
Autoreqprov:   no
Requires:      open-xchange-oauth >= @OXVERSION@

%description
This package provides API calls for XING-related actions. 
With this package you can use XING actions like showing the newsfeed and inviting users to XING inside OX.

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
* Tue Sep 23 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-09-23
* Tue Sep 16 2014 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-08-25
* Tue Aug 05 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-07-30
* Tue Jul 15 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-07-15
* Tue Jul 01 2014 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Steffen Templin <steffen.templin@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Steffen Templin <steffen.templin@open-xchange.com>
Sixth release candidate for 7.6.0
* Fri Jun 13 2014 Steffen Templin <steffen.templin@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri May 30 2014 Steffen Templin <steffen.templin@open-xchange.com>
Fourth release candidate for 7.6.0
* Fri May 16 2014 Steffen Templin <steffen.templin@open-xchange.com>
Third release candidate for 7.6.0
* Mon May 05 2014 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 11 2014 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.6.0
* Mon Mar 31 2014 Steffen Templin <steffen.templin@open-xchange.com>
Initial release
