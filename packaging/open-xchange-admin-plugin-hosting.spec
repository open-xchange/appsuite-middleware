
# norootforbuild

Name:           open-xchange-admin-plugin-hosting
BuildArch:	noarch
BuildRequires:  ant open-xchange-admin
#%if 0%{?suse_version} <= 1010
# SLES10
#BuildRequires:  java-1_5_0-ibm-devel java-1_5_0-ibm-alsa update-alternatives
#%endif
#%if 0%{?suse_version} >= 1020
%if 0%{?suse_version}
BuildRequires:  java-1_5_0-sun-devel
%endif
%if 0%{?fedora_version}
BuildRequires:  java-devel-icedtea
%endif
#%endif
Version:        6.5.0
Release:        1
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin Hosting Plugin
Requires:       open-xchange-admin
%if 0%{?suse_version}
%if 0%{?suse_version} <= 1010
# SLES10
Requires:  java-1_5_0-ibm java-1_5_0-ibm-alsa update-alternatives
%endif
%if 0%{?suse_version} >= 1020
Requires:  java-1_5_0-sun
%endif
%endif
%if 0%{?fedora_version}
Requires:  jre-icedtea
%endif
Conflicts:	open-xchange-admin-plugin-context-light
#


%description
Open Xchange Admin Hosting Plugin

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
%define adminbundle	open_xchange_admin.jar
%define oxprefix	/opt/open-xchange

ant -Dadmin.classpath=%{oxprefix}/bundles/%{adminbundle} \
    -Ddestdir=%{buildroot} -Dprefix=%{oxprefix} install install-client


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/lib
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/sbin/*
/opt/open-xchange/lib/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/*

