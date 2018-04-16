%define __jar_repack %{nil}

Name:          open-xchange-client-onboarding
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
%define        ox_release 40
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange On-Boarding Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
Open-Xchange on-boarding package

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
. /opt/open-xchange/lib/oxfunctions.sh

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

if [ ${1:-0} -eq 2 ]; then # only when updating

    #Bug 44352, simply update documentation of properties
    key=com.openexchange.client.onboarding.caldav.url
    pfile=/opt/open-xchange/etc/client-onboarding-caldav.properties
    oldValue=$(ox_read_property ${key} ${pfile}) 
    if [ -n "${oldValue}" ]; then
      ox_set_property ${key} "${oldValue}" ${pfile}
    fi

    key=com.openexchange.client.onboarding.carddav.url
    pfile=/opt/open-xchange/etc/client-onboarding-carddav.properties
    oldValue=$(ox_read_property ${key} ${pfile}) 
    if [ -n "${oldValue}" ]; then
      ox_set_property ${key} "${oldValue}" ${pfile}
    fi

    key=com.openexchange.client.onboarding.eas.url
    pfile=/opt/open-xchange/etc/client-onboarding-eas.properties
    oldValue=$(ox_read_property ${key} ${pfile}) 
    if [ -n "${oldValue}" ]; then
      ox_set_property ${key} "${oldValue}" ${pfile}
    fi

    # SoftwareChange_Request-3409
    key=com.openexchange.client.onboarding.syncapp.store.google.playstore
    pfile=/opt/open-xchange/etc/client-onboarding-syncapp.properties
    oldValue=$(ox_read_property ${key} ${pfile})
    if [ 'https://play.google.com/store/apps/details?id=org.dmfs.caldav.icloud'  == "${oldValue}" ]; then
      ox_set_property ${key} "" ${pfile}
    fi

    # SoftwareChange_Request-3414
    sed -i 's/-> Requires "emclient" capability/-> Requires "emclient_basic" or "emclient_premium" capability/' /opt/open-xchange/etc/client-onboarding-scenarios.yml

    # SoftwareChange_Request-3823
    pfile=/opt/open-xchange/etc/client-onboarding-mail.properties
    ox_add_property com.openexchange.client.onboarding.mail.imap.login.customsource false ${pfile}
    ox_add_property com.openexchange.client.onboarding.mail.smtp.login.customsource false ${pfile}

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
%dir /opt/open-xchange/templates/
/opt/open-xchange/templates/*
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
* Mon Apr 16 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-04-23 (4668)
* Mon Jan 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-05 (4553)
* Fri Dec 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-12-11 (4471)
* Mon Oct 23 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-30 (4424)
* Mon Oct 09 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-10-16 (4392)
* Mon Aug 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-21 (4316)
* Fri May 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-19 (4175)
* Mon May 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-15 (4131)
* Fri Apr 21 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-02 (4112)
* Wed Apr 12 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-18 (4083)
* Fri Mar 31 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-03 (4049)
* Wed Mar 22 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-23 (4046)
* Thu Mar 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-20 (4015)
* Mon Mar 06 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-03-06 (3984)
* Fri Feb 24 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-24 (3993)
* Wed Feb 15 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-20 (3951)
* Fri Jan 27 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-06 (3917)
* Thu Jan 26 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-26 (3924)
* Thu Jan 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-23 (3878)
* Fri Jan 06 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-18 (3867)
* Wed Jan 04 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-09 (3848)
* Wed Dec 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-19 (3813)
* Tue Dec 13 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-12 (3817)
* Mon Dec 05 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-12-05 (3763)
* Sat Nov 12 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-21 (3731)
* Tue Nov 08 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-07 (3678)
* Wed Oct 26 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-08 (3699)
* Mon Oct 17 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-10-24 (3630)
* Thu Oct 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-10-10 (3597)
* Mon Sep 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-26 (3572)
* Mon Sep 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-08 (3580)
* Mon Sep 05 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-12 (3547)
* Mon Aug 22 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-29 (3522)
* Mon Aug 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-26 (3512)
* Mon Aug 08 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-15 (3490)
* Fri Jul 22 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-01 (3467)
* Tue Jul 12 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.1 release
* Fri Dec 04 2015 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
