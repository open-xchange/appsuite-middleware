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
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
BuildRequires: java-1_7_0-openjdk-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
%endif
Version:       @OXVERSION@
%define        ox_release 27
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


    # SoftwareChange_Request-3954
    PFILE=/opt/open-xchange/etc/client-onboarding.properties
    NAMES=( com.openexchange.client.onboarding.apple.mac.scenarios com.openexchange.client.onboarding.apple.ipad.scenarios com.openexchange.client.onboarding.apple.iphone.scenarios com.openexchange.client.onboarding.android.tablet.scenarios com.openexchange.client.onboarding.android.phone.scenarios com.openexchange.client.onboarding.windows.desktop.scenarios )
    OLDVALUES=( 'davsync, mailsync, driveappinstall' 'davsync, mailsync, eassync, mailappinstall, driveappinstall' 'davsync, mailsync, eassync, mailappinstall, driveappinstall' 'mailmanual, mailappinstall, driveappinstall, syncappinstall' 'mailmanual, mailappinstall, driveappinstall, syncappinstall' 'mailmanual, drivewindowsclientinstall, oxupdaterinstall, emclientinstall' )
    NEWVALUES=( 'driveappinstall, mailsync, davsync' 'mailappinstall, driveappinstall, eassync, mailsync, davsync' 'mailappinstall, driveappinstall, eassync, mailsync, davsync' 'mailappinstall, driveappinstall, mailmanual, syncappinstall' 'mailappinstall, driveappinstall, mailmanual, syncappinstall' 'drivewindowsclientinstall, emclientinstall, mailmanual, oxupdaterinstall' )
    for I in $(seq 1 ${#NAMES[@]}); do
        NAME=${NAMES[$I-1]}
        OLDVALUE="${OLDVALUES[$I-1]}"
        NEWVALUE="${NEWVALUES[$I-1]}"
        VALUE=$(ox_read_property ${NAME} ${PFILE})
        if [ "${OLDVALUE}" == "${VALUE}" ]; then
            ox_set_property ${NAME} "${NEWVALUE}" $PFILE
        fi
    done

    # SoftwareChange_Request-4062
    ox_add_property com.openexchange.client.onboarding.caldav.login.customsource false /opt/open-xchange/etc/client-onboarding-caldav.properties
    ox_add_property com.openexchange.client.onboarding.carddav.login.customsource false /opt/open-xchange/etc/client-onboarding-carddav.properties
    ox_add_property com.openexchange.client.onboarding.eas.login.customsource false /opt/open-xchange/etc/client-onboarding-eas.properties
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
%doc com.openexchange.client.onboarding/doc/examples

%changelog
* Thu Apr 12 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-04-12 (4674)
* Tue Apr 03 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-04-03 (4642)
* Fri Mar 23 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-03-26 (4619)
* Mon Mar 12 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-03-12 (4602)
* Mon Feb 26 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-26 (4583)
* Mon Jan 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-05 (4555)
* Mon Jan 15 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-01-22 (4538)
* Tue Jan 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-01-08 (4516)
* Fri Dec 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-12-11 (4473)
* Thu Nov 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-11-20 (4441)
* Tue Nov 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-11-15 (4448)
* Wed Oct 25 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-30 (4415)
* Mon Oct 23 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-29 (4425)
* Mon Oct 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-16 (4394)
* Wed Sep 27 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-02 (4377)
* Thu Sep 21 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-09-22 (4373)
* Tue Sep 12 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-09-18 (4354)
* Fri Sep 01 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-09-04 (4328)
* Mon Aug 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-21 (4318)
* Tue Aug 01 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-07 (4304)
* Mon Jul 17 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-07-24 (4285)
* Mon Jul 03 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-07-10 (4257)
* Wed Jun 21 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-06-26 (4233)
* Tue Jun 06 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-06-08 (4180)
* Fri May 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Thorben Betten <thorben.betten@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Thorben Betten <thorben.betten@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.3 release
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
