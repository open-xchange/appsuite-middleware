
# norootforbuild

Name:           open-xchange-admin-soap
BuildArch:	noarch
BuildRequires:  ant open-xchange-admin-client open-xchange-admin-plugin-hosting
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
BuildRequires:  java-devel-icedtea
%endif
Version:        6.5.0
Release:        0
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin SOAP API
Requires:       open-xchange-admin-client >= 6.5.0
Requires:	open-xchange-admin-plugin-hosting >= 6.5.0
Requires:	open-xchange-axis2
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
Requires:  java-1_5_0-ibm update-alternatives
%endif
%if %{?suse_version} >= 1100
BuildRequires:  java-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
Requires:  java-1_5_0-sun
%endif
%endif
%if 0%{?fedora_version}
Requires:  jre-icedtea
%endif
#

%description
Open Xchange Admin SOAP API

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
ant -Ddestdir=%{buildroot} install


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/bundles/com.openexchange.axis2/services/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/open-xchange-admin-soap.properties
%changelog
