
# norootforbuild

Name:           open-xchange-i18n
BuildArch:	noarch
BuildRequires:  ant open-xchange-common open-xchange-configread open-xchange-global
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
BuildRequires:  java-1_5_0-ibm java-1_5_0-ibm-devel java-1_5_0-ibm-alsa update-alternatives
%endif
%if %{?suse_version} >= 1100
BuildRequires:  java-sdk-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
BuildRequires:  java-sdk-1.5.0-sun
%endif
%endif
%if 0%{?rhel_version}
# libgcj seems to be installed whether we want or not and libgcj needs cairo
BuildRequires:  java-sdk-1.5.0-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%if %{?fedora_version} <= 8
BuildRequires:  java-devel-icedtea saxon
%endif
%endif
Version:	6.6.0
Release:	14
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange i18n Bundle
Requires:       open-xchange-common open-xchange-configread open-xchange-global
#

%description
The Open-Xchange i18n Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/*/osgi/bundle.d/
%dir /opt/open-xchange/etc/admindaemon
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/i18n
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/*/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/*.properties
/opt/open-xchange/etc/groupware/*.properties
/opt/open-xchange/i18n/*

