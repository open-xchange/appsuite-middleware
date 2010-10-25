
# norootforbuild

Name:           open-xchange-configjump-generic
Provides:	open-xchange-configjump
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@
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
Summary:        The Open-Xchange generic ConfigJump
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@
#

%package gui
Group:          Applications/Productivity
Requires:       open-xchange-gui >= @OXVERSION@
Summary:        Open-Xchange generic ConfigJump GUI component


%description
The Open-Xchange generic ConfigJump

Authors:
--------
    Open Xchange

%description gui
The Open-Xchange generic ConfigJump GUI component

Authors:
--------
    Open Xchange


%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define guiprefix /var/www/html/ox6
%else
%define guiprefix /srv/www/htdocs/ox6
%endif

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dguiprefix=%{guiprefix} install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
%dir /opt/open-xchange/etc/groupware
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/configjump.properties

%files gui
%defattr(-,root,root)
%dir %{guiprefix}/plugins/com.openexchange.extras
%{guiprefix}/plugins/com.openexchange.extras/*

