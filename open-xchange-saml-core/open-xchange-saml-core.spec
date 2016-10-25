
Name:          open-xchange-saml-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 37
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
* Thu Oct 13 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-10-24 (3626)
* Tue Sep 20 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-09-26 (3568)
* Fri Aug 19 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-08-29 (3518)
* Fri Jul 22 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-08-01 (3463)
* Thu Jun 30 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-07-07 (3398)
* Thu Jun 16 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-06-24 (3362)
* Wed Jun 01 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-06-06 (3314)
* Mon May 02 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-05-09 (3269)
* Thu Apr 28 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-04-28 (3253)
* Tue Apr 12 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-04-19 (3241)
* Wed Mar 23 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-03-29 (3186)
* Wed Mar 09 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-03-14 (3162)
* Mon Feb 22 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-02-24 (3129)
* Mon Feb 01 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-02-08 (3071)
* Fri Jan 22 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-01-25 (3052)
* Wed Jan 20 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-01-25 (3029)
* Mon Jan 11 2016 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2016-01-13 (2980)
* Mon Dec 14 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-12-21 (2952)
* Thu Dec 03 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-12-07 (2916)
* Thu Dec 03 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-12-02 (2930)
* Tue Nov 17 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-11-23 (2883)
* Wed Nov 11 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-11-16 (2862)
* Fri Nov 06 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-11-09 (2841)
* Fri Oct 30 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-11-03 (2851)
* Wed Oct 28 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-11-03 (2827)
* Tue Oct 20 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Wed Sep 30 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-28  (2767)
* Tue Sep 08 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Wed Sep 02 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Mon Aug 17 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-12 (2671)
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
