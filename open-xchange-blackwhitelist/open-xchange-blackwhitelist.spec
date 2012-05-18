
Name:           open-xchange-blackwhitelist
BuildArch:      noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core >= @OXVERSION@
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
Summary:        Blackwhitelist
Requires:       open-xchange-core >= @OXVERSION@

%description
Blackwhitelist

Authors:
--------
    Open-Xchange

%package        gui
Group:          Applications/Productivity
Summary:        Blackwhitelist GUI
Requires:       open-xchange-gui

%description    gui
Blackwhitelist GUI

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

ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build
ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=%{name} -DbuildTarget=installGui -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/settings
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/bundles/*
%config(noreplace) /opt/open-xchange/etc/settings/*


%files gui
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.blackwhitelist
%{docroot}/ox6/plugins/com.openexchange.blackwhitelist/lang/*
%{docroot}/ox6/plugins/com.openexchange.blackwhitelist/*.js

%changelog
