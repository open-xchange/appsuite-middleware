
Name:          open-xchange-eas-provisioning
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
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
if [ ${1:-0} -eq 2 ]; then
    if [ -e /opt/open-xchange/etc/groupware/mobileconfig.properties ]; then
        mv /opt/open-xchange/etc/eas-provisioning.properties /opt/open-xchange/etc/eas-provisioning.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/mobileconfig.properties /opt/open-xchange/etc/eas-provisioning.properties
    fi
    if [ -e /opt/open-xchange/etc/groupware/mobilityconfiguration.properties ]; then
        mv /opt/open-xchange/etc/eas-provisioning-mail.properties /opt/open-xchange/etc/eas-provisioning-mail.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/mobilityconfiguration.properties /opt/open-xchange/etc/eas-provisioning-mail.properties
    fi
    if [ -e /opt/open-xchange/etc/groupware/settings/open-xchange-mobile-configuration-gui.properties ]; then
        mv /opt/open-xchange/etc/settings/eas-provisioning-ui.properties /opt/open-xchange/etc/settings/eas-provisioning-ui.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/settings/open-xchange-mobile-configuration-gui.properties /opt/open-xchange/etc/settings/eas-provisioning-ui.properties
    fi
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
