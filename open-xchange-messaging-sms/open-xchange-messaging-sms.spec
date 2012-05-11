
Name:          open-xchange-messaging-sms
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-messaging >= @OXVERSION@
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires:  java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires:  java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        This bundle provides a new messaging interface for SMS services
Requires:       open-xchange-messaging >= @OXVERSION@

%description
This bundle provides a new messaging interface for SMS services

Authors:
--------
    Open-Xchange

%package        gui
Group:          Applications/Productivity
Summary:        General Messaging SMS GUI Bundle
Requires:       open-xchange-messaging-sms-gui-theme >= @OXVERSION@
Requires:       open-xchange-gui

%description    gui
General Messaging SMS GUI Bundle.

Authors:
--------
    Open-Xchange

%package        gui-theme-default
Group:          Applications/Productivity
Summary:        General Messaging SMS GUI Themes Bundle
Requires:       open-xchange-gui
Requires:       open-xchange-messaging-sms-gui >= @OXVERSION@
Provides:       open-xchange-messaging-sms-gui-theme

%description    gui-theme-default
General Messaging SMS GUI Themes Bundle.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define docroot /var/www/html
%else
%define docroot /srv/www/htdocs
%endif

ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=open-xchange-messaging-sms -f build/build.xml build
ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=open-xchange-messaging-sms -DbuildTarget=installGui -f build/build.xml build
ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=open-xchange-messaging-sms -DbuildTarget=installGuiTheme -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/bundles/*

%files gui
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.messaging.sms
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/lang/*
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/*.js

%files gui-theme-default
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.messaging.sms/images
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/images/*

%changelog
* Wed May 09 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Initial release
