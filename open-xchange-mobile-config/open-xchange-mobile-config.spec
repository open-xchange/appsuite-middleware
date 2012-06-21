
# norootforbuild

Name:           open-xchange-mobile-config
BuildArch: 	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
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
# TODO: version not hardcoded in spec file
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        Creative Commons Attribution-Noncommercial-Share Alike 2.5 Generic
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Config files for the Open-Xchange Mobile UI

%description
 This package needs to be installed on the backend hosts of a cluster installation. It adds configuration files to the backend allowing the
 administrator to define some defaults for the mobile web app. Additionally it adds configuration paths on the backend for the Mobile Web Interface
 that allows to store end user preferences.

Authors:
--------
    Open-Xchange



%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=open-xchange-mobile-config -DbuildType=installConf -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/settings
%dir /opt/open-xchange/etc/groupware/meta
%config(noreplace) /opt/open-xchange/etc/groupware/settings/*
%config(noreplace) /opt/open-xchange/etc/groupware/meta/*

%changelog
* Thu Jun 21 2012 - marcus.klein@open-xchange.com
  - Initial package for new backend
