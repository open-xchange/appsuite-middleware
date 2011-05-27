
# norootforbuild

Name:           open-xchange-oauth-linkedin
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-subscribe >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-xml >= @OXVERSION@ open-xchange-oauth >= @OXVERSION@
%if 0%{?suse_version} && 0%{?sles_version} < 11
%if %{?suse_version} <= 1010
# SLES10
BuildRequires:  open-xchange-xerces-ibm
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
BuildRequires:  open-xchange-xerces-sun
BuildRequires:  java-sdk-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%endif
%if 0%{?centos_version}
BuildRequires:  open-xchange-xerces-sun
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 11
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        LinkedIn via OAuth for Open-Xchange Server 6
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-subscribe >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-xml >= @OXVERSION@ open-xchange-oauth >= @OXVERSION@

%if 0%{?sles_version} >= 10
Requires:   open-xchange-xerces-ibm
Conflicts:  open-xchange-xerces-sun
%else
Requires:   open-xchange-xerces-sun
Conflicts:  open-xchange-xerces-ibm
%endif
#

%description
Subscribe Microformat feeds
  
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
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/*/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/*/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/*.properties
