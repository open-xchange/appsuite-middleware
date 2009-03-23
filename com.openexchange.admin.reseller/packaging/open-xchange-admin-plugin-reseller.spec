
# norootforbuild

Name:           open-xchange-admin-plugin-reseller
BuildArch:	noarch
BuildRequires:  ant
BuildRequires:  open-xchange-admin >= 6.9.0.0
BuildRequires:  open-xchange-admin-plugin-hosting >= 6.9.0.0
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
BuildRequires:  java-1_5_0-ibm >= 1.5.0_sr9
BuildRequires:  java-1_5_0-ibm-devel >= 1.5.0_sr9
BuildRequires:  java-1_5_0-ibm-alsa >= 1.5.0_sr9
BuildRequires:  update-alternatives
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
Version:	6.9.0.0
Release:	0
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin Context Restore Plugin
Requires:       open-xchange-admin >= 6.9.0.0
Requires:       open-xchange-admin-client >= 6.9.0.0
Requires:       open-xchange-admin-plugin-hosting >= 6.9.0.0
#
%package -n	open-xchange-admin-plugin-reseller-soap
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Reseller SOAP client
Requires:       open-xchange-admin-plugin-reseller
Requires:       open-xchange-admin-soap

%description
Open Xchange Admin Reseller Plugin

Authors:
--------
    Open-Xchange

%description -n open-xchange-admin-plugin-reseller-soap
Open Xchange Admin Reseller Plugin

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
%define adminbundle	open_xchange_admin.jar
%define oxprefix	/opt/open-xchange
%define adminhostingbundle open_xchange_admin_plugin_hosting.jar

ant -Dadmin.classpath=%{oxprefix}/bundles/%{adminbundle} \
    -Ddestdir=%{buildroot} -Dprefix=%{oxprefix} \
    -Dadminhosting.classpath=%{oxprefix}/bundles/%{adminhostingbundle} \
    -Ddoccorelink=/usr/share/doc/packages/open-xchange-admin-doc/javadoc/doc \
    doc install install-client install-soap
mv doc javadoc


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/mysql
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/lib
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/mysql/*
/opt/open-xchange/sbin/*
/opt/open-xchange/lib/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/*

%files -n open-xchange-admin-plugin-reseller-soap
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
/opt/open-xchange/bundles/com.openexchange.axis2/services/*

%changelog
