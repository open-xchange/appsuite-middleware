
Name:          open-xchange-eas-provisioning
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 15
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Meta package to install all necessary components to provision synchronization with mobile phones
Requires:      open-xchange-eas-provisioning-core >= @OXVERSION@
Requires:      open-xchange-eas >= @OXVERSION@
Requires:      open-xchange-eas-provisioning-action

%description
Meta package to install all necessary components to provision synchronization with mobile phones


Authors:
--------
    Open-Xchange

%package core
Group:         Applications/Productivity
Summary:       Backend extension to provision synchronization with mobile phones
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-mobile-configuration-generator = %{version}
Obsoletes:     open-xchange-mobile-configuration-generator <= %{version}
Provides:      open-xchange-mobile-configuration-json = %{version}
Obsoletes:     open-xchange-mobile-configuration-json <= %{version}

%description core
Backend extension to provision synchronization with mobile phones


Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post core
. /opt/open-xchange/lib/oxfunctions.sh
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc mobileconfig.properties eas-provisioning.properties
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc mobilityconfiguration.properties eas-provisioning-mail.properties
ox_move_config_file /opt/open-xchange/etc/groupware/settings /opt/open-xchange/etc/settings open-xchange-mobile-configuration-gui.properties eas-provisioning-ui.properties

# SoftwareChange_Request-1197
pfile=/opt/open-xchange/etc/settings/eas-provisioning-ui.properties
ptmp=${pfile}.$$
if grep com.openexchange.mobile.configuration.gui $pfile > /dev/null; then
    sed -e 's;com.openexchange.mobile.configuration.gui;com.openexchange.eas.provisioning.ui;' < $pfile > $ptmp
    if [ -s $ptmp ]; then
       cp $ptmp $pfile
    fi
    rm -f $ptmp
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)

%files core
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*.properties
%dir /opt/open-xchange/etc/settings/
%config(noreplace) /opt/open-xchange/etc/settings/*
%dir /opt/open-xchange/templates/
%config(noreplace) /opt/open-xchange/templates/*

%changelog
* Fri May 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 06 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
