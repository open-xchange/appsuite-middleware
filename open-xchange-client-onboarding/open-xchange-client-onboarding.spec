%define __jar_repack %{nil}

Name:          open-xchange-client-onboarding
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
%define        ox_release 13
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
    PFILE=/opt/open-xchange/etc/client-onboarding-scenarios.yml
    $(contains '-> Requires "emclient" capability' $PFILE) && sed -i 's/-> Requires "emclient" capability/-> Requires "emclient_basic" or "emclient_premium" capability/' $PFILE

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

    # SCR-120
    scenarios=com.openexchange.client.onboarding.windows.desktop.scenarios
    enabled_scenarios=com.openexchange.client.onboarding.enabledScenarios
    pfile=/opt/open-xchange/etc/client-onboarding.properties

    scenarios_line=$(ox_read_property ${scenarios} ${pfile})
    newline=$(sed -r -e 's/oxupdaterinstall *,? *| *,? *oxupdaterinstall//' <<<${scenarios_line})
    ox_set_property ${scenarios} "${newline}" ${pfile}

    enabled_scenarios_line=$(ox_read_property ${enabled_scenarios} ${pfile})
    newline=$(sed -r -e 's/oxupdaterinstall *,? *| *,? *oxupdaterinstall//' <<<${enabled_scenarios_line})
    ox_set_property ${enabled_scenarios} "${newline}" ${pfile}

    # SCR-187
    pfile=/opt/open-xchange/etc/client-onboarding-mailapp.properties
    playstore_key=com.openexchange.client.onboarding.mailapp.store.google.playstore
    playstore_old_default=https://play.google.com/store/apps/details?id=com.openexchange.mobile.mailapp.enterprise
    playstore_new_default=https://play.google.com/store/apps/details?id=com.openxchange.mobile.oxmail
    appstore_key=com.openexchange.client.onboarding.mailapp.store.apple.appstore
    appstore_old_default=https://itunes.apple.com/us/app/ox-mail/id1008644994
    appstore_new_default=https://itunes.apple.com/us/app/ox-mail-v2/id1385582725

    playstore_curr_val=$(ox_read_property ${playstore_key} ${pfile})
    if [ "${playstore_curr_val}" == ${playstore_old_default} ]
    then
      ox_set_property ${playstore_key} ${playstore_new_default} ${pfile}
    fi

    appstore_curr_val=$(ox_read_property ${appstore_key} ${pfile})
    if [ "${appstore_curr_val}" == "${appstore_old_default}" ]
    then
      ox_set_property ${appstore_key} ${appstore_new_default} ${pfile}
    fi

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
* Wed Aug 15 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-20 (4863)
* Thu Aug 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-13 (4853)
* Fri Jul 20 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-07-25 (4835)
* Fri Jun 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Thorben Betten <thorben.betten@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Thorben Betten <thorben.betten@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.0 release
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
