%define __jar_repack %{nil}

Name:          open-xchange-oauth-provider
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
%define        ox_release 27
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The OAuth provider feature
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
OX App Suite is able to act as an OAuth 2.0 provider. Registered client
applications can access certain HTTP API calls in the name of users who
granted them access accordingly. This package adds the necessary core
functionality to serve those API calls.

Authors:
--------
    Open-Xchange

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

    # SoftwareChange_Request-3098
    ox_add_property com.openexchange.oauth.provider.isAuthorizationServer true /opt/open-xchange/etc/oauth-provider.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/templates/
/opt/open-xchange/templates/*
%config(noreplace) /opt/open-xchange/etc/hazelcast/authcode.properties
%config(noreplace) /opt/open-xchange/etc/oauth-provider.properties

%changelog
* Tue Mar 12 2019 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2019-03-11 (5148)
* Mon Feb 18 2019 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2019-02-25 (5132)
* Thu Feb 07 2019 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2019-02-11 (5107)
* Fri Jan 18 2019 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2019-01-28 (5075)
* Mon Jan 07 2019 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2019-01-14 (5039)
* Mon Dec 10 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-12-17 (5018)
* Mon Nov 26 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-12-03 (4993)
* Mon Nov 19 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-11-19 (4966)
* Mon Oct 29 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-11-05 (4933)
* Mon Oct 08 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-10-15 (4918)
* Tue Sep 25 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-10-01 (4897)
* Mon Sep 24 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-09-21 (4900)
* Mon Sep 10 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-09-17 (4882)
* Mon Aug 27 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-09-03 (4870)
* Wed Aug 15 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-08-20 (4863)
* Thu Aug 02 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-08-13 (4853)
* Fri Jul 20 2018 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2018-07-25 (4835)
* Fri Jun 29 2018 Steffen Templin <steffen.templin@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Steffen Templin <steffen.templin@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Steffen Templin <steffen.templin@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Steffen Templin <steffen.templin@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Steffen Templin <steffen.templin@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Steffen Templin <steffen.templin@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Steffen Templin <steffen.templin@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.0 release
* Fri May 19 2017 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Steffen Templin <steffen.templin@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Steffen Templin <steffen.templin@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Steffen Templin <steffen.templin@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Steffen Templin <steffen.templin@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Steffen Templin <steffen.templin@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Steffen Templin <steffen.templin@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Steffen Templin <steffen.templin@open-xchange.com>
First candidate for 7.8.1 release
* Mon Oct 19 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Thu Oct 08 2015 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Steffen Templin <steffen.templin@open-xchange.com>
Sixth candidate for 7.8.0 release
* Fri Sep 25 2015 Steffen Templin <steffen.templin@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Steffen Templin <steffen.templin@open-xchange.com>
Fourth candidate for 7.8.0 release
* Mon Sep 07 2015 Steffen Templin <steffen.templin@open-xchange.com>
Third candidate for 7.8.0 release
* Fri Aug 21 2015 Steffen Templin <steffen.templin@open-xchange.com>
Second candidate for 7.8.0 release
* Wed Aug 05 2015 Steffen Templin <steffen.templin@open-xchange.com>
First release candidate for 7.8.0
* Tue Apr 21 2015 Steffen Templin <steffen.templin@open-xchange.com>
Initial packaging
