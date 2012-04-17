# norootforbuild
Name:           open-xchange-osgi
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant ant-nodeps
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
Version:    @OXVERSION@
%define        ox_release 0
Release:    %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:        %{name}_%{version}.orig.tar.bz2
Summary:    OSGi bundles commonly used by all Open-Xchange packages
PreReq:       /usr/sbin/useradd

%if 0%{?suse_version} && 0%{?sles_version} < 11
%if %{?suse_version} <= 1010
# SLES10
Requires: java-1_5_0-ibm >= 1.5.0_sr9
Requires: update-alternatives
%endif
%if %{?suse_version} >= 1100
Requires: java-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
Requires: java-1_5_0-sun
%endif
%endif
%if 0%{?sles_version} >= 11
# SLES11
Requires: java-1_6_0-ibm
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
Requires: java-1.6.0-openjdk
%endif
%if %{?fedora_version} <= 8
Requires: java-icedtea
%endif
%endif
%if 0%{?rhel_version}
# RHEL5 removed sun-java5, but some might still use it, so just depend on sun-java
Requires: java-sun
%endif

#

%description

Authors:
--------
    Open-Xchange
    
%prep
%setup -q
%build
%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=open-xchange-osgi -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
