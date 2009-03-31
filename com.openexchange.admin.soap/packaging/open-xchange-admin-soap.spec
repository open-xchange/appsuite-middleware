
# norootforbuild

Name:           open-xchange-admin-soap
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-admin-client open-xchange-admin-plugin-hosting-client open-xchange-common perl
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
Version:	@OXVERSION@
Release:	0
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin SOAP API
Requires:       open-xchange-admin-client >= @OXVERSION@
Requires:	open-xchange-admin-plugin-hosting-client >= @OXVERSION@
Requires:	open-xchange-axis2

%description
Open Xchange Admin SOAP API

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -Ddestdir=%{buildroot} \
	-Ddoccorelink=/usr/share/doc/packages/open-xchange-admin-doc/javadoc/doc \
	-Ddochostinglink=/usr/share/doc/packages/open-xchange-admin-plugin-hosting-doc/javadoc/doc \
	install doc


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/bundles/com.openexchange.axis2/services/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/open-xchange-admin-soap.properties
%doc docs
%changelog
