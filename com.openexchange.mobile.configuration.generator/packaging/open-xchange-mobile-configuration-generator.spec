
# norootforbuild

Name:           open-xchange-mobile-configuration-generator
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-templating >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@
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
BuildRequires:  java-sdk-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%endif
%if 0%{?centos_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 18
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        This bundle provides methods to configure mobile devices for Active Sync
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange >= @OXVERSION@ open-xchange-templating >= @OXVERSION@ open-xchange-eas >= @OXVERSION@
%description
This bundle provides methods to configure mobile devices for Active Sync

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install


%post

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
%dir /opt/open-xchange/templates/
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/bundles/com.openexchange.mobile.configuration.generator.jar
%config(noreplace) /opt/open-xchange/etc/groupware/mobileconfig.properties
%config(noreplace) /opt/open-xchange/templates/*

%changelog
* Wed May 09 2010 - dennis.sieben@open-xchange.com
 - Initial version
