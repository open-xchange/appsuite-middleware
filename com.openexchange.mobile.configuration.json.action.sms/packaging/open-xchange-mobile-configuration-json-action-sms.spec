
# norootforbuild

Name:           open-xchange-mobile-configuration-json-action-sms
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-xml >= @OXVERSION@ open-xchange-mobile-configuration-json >= @OXVERSION@
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
Summary:        Mobility Configuration SMS transport.
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-mobile-configuration-json >= @OXVERSION@
#

%description
Mobility Configuration SMS transport.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/bundles
%config(noreplace) /opt/open-xchange/etc/groupware/mobile_configuration_action_sms.properties
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*

%changelog
* Wed Mar 16 2011 - choeger@open-xchange.com
  - Bugfix #18662 - Mobile configuration packages cant be installed on debian squeeze
* Sat Jun 26 2010 - manuel.kraft@open-xchange.com
  - adding SMS implementation and fixed libs in build file
* Fri Jun 25 2010 - benjamin.otterbach@open-xchange.com
  - Initial import
