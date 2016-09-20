%define __jar_repack %{nil}

Name:          open-xchange-saml-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 21
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Core package to support SAML authentication
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package contains the core bundles to support SAML as authentication
mechanism. It must always be complemented by a custom implementation that
performs several deployment-specific tasks.

Authors:
--------
    Open-Xchange

%package -n open-xchange-saml
Group:         Applications/Productivity
Summary:       Meta package to install necessary components to support SAML authentication
Requires:      open-xchange-saml-core >= @OXVERSION@
Requires:      open-xchange-saml-backend

%description -n open-xchange-saml
Install this package and its dependencies will install the necessary components to support SAML authentication.

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PFILE=/opt/open-xchange/etc/saml.properties

    # SoftwareChange_Request-2673
    ox_add_property com.openexchange.saml.enableAutoLogin false $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*
%dir /opt/open-xchange/etc/hazelcast
%config(noreplace) /opt/open-xchange/etc/hazelcast/*
%dir /opt/open-xchange/templates
/opt/open-xchange/templates/*

%files -n open-xchange-saml
%defattr(-,root,root)

%changelog
* Mon Sep 05 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-09-12 (3546)
* Fri Aug 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-08-29 (3521)
* Mon Aug 08 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-08-15 (3489)
* Thu Jul 28 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-08-01 (3466)
* Thu Jul 14 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-07-18 (3433)
* Thu Jun 30 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-07-04 (3400)
* Wed Jun 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-06-20 (3347)
* Fri Jun 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-06-06 (3317)
* Fri May 20 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-05-23 (3294)
* Thu May 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-05-19 (3305)
* Fri May 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-05-09 (3272)
* Mon Apr 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-04-25 (3263)
* Fri Apr 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-04-25 (3239)
* Thu Apr 07 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-04-07 (3228)
* Wed Mar 30 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Wed Mar 23 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-29 (3188)
* Tue Mar 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fifth preview for 7.8.1 release
* Mon Mar 07 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-14 (3148)
* Fri Mar 04 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fourth preview for 7.8.1 release
* Fri Feb 26 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-29 (3141)
* Mon Feb 22 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-29 (3121)
* Sat Feb 20 2016 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.1 release
* Mon Feb 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-18 (3106)
* Wed Feb 10 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-08 (3073)
* Wed Feb 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-19 (3062)
* Tue Jan 26 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Mon Jan 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-25 (3031)
* Sat Jan 23 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-05 (3058)
* Sat Jan 23 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-05 (3058)
* Fri Jan 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-15 (3028)
* Wed Jan 13 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-13 (2982)
* Tue Jan 12 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-12-23 (3011)
* Tue Dec 29 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-05 (2989)
* Tue Dec 22 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-12-23 (2971)
* Fri Dec 11 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-12-21 (2953)
* Tue Dec 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-12-07 (2918)
* Thu Nov 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-11-23 (2878)
* Thu Nov 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-11-09 (2840)
* Fri Oct 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-11-02 (2853)
* Tue Oct 20 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Mon Oct 12 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Thu Oct 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.8.0 release
* Wed Sep 30 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-28  (2767)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.8.0 release
* Tue Sep 08 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Mon Sep 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.0 release
* Wed Sep 02 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Fri Aug 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.0 release
* Mon Aug 17 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Wed Aug 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.8.0
* Tue Aug 04 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Tue Jul 14 2015 Steffen Templin <steffen.templin@open-xchange.com>
Prepare for 7.6.2 hotfix
* Tue Mar 24 2015 Steffen Templin <steffen.templin@open-xchange.com>
initial packaging for SAML core package
* Wed Nov 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.0 release
