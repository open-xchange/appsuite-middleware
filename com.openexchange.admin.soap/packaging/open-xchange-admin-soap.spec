
# norootforbuild

Name:           open-xchange-admin-soap
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-admin-client >= @OXVERSION@ open-xchange-admin-plugin-hosting-client >= @OXVERSION@ open-xchange-common >= @OXVERSION@ perl
%if 0%{?suse_version} && 0%{?sles_version} < 11
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
%if 0%{?sles_version} >= 11
# SLES11 or higher
BuildRequires:  java-1_6_0-ibm-devel
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
%define		ox_release 6
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin SOAP API
Requires:       open-xchange-admin-client >= @OXVERSION@
Requires:	open-xchange-admin-plugin-hosting-client >= @OXVERSION@
Requires:	open-xchange-axis2 >= @OXVERSION@

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
%dir /opt/open-xchange/lib
/opt/open-xchange/bundles/com.openexchange.axis2/services/*
/opt/open-xchange/lib/soaprmimapper.jar
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/open-xchange-admin-soap.properties
%doc docs
%changelog
* Mon Jul 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14213: Setting configuration file permissions to reduce readability to OX processes.
* Tue Jun 02 2009 - dennis.sieben@open-xchange.com
 - Bugfix #13796: soap interface throws error "reconnect to rmi service" on first call .
     Implemented a second call after the reconnect
