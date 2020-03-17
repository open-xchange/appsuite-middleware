%define __jar_repack %{nil}

Name:          open-xchange-eas-provisioning
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Meta package to install all necessary components to provision synchronization with mobile phones
Autoreqprov:   no
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
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-mobile-configuration-generator = %{version}
Obsoletes:     open-xchange-mobile-configuration-generator < %{version}
Provides:      open-xchange-mobile-configuration-json = %{version}
Obsoletes:     open-xchange-mobile-configuration-json < %{version}

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
cp -rv --preserve=all ./opt %{buildroot}/
cat<<EOF > README.TXT
This is an empty package.
EOF

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

# SoftwareChange_Request-2297
ox_add_property com.openexchange.mobile.configuration.generator.PemFile "" /opt/open-xchange/etc/eas-provisioning.properties

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%doc README.TXT

%files core
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) /opt/open-xchange/etc/settings
%config(noreplace) /opt/open-xchange/templates

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
