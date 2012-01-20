
# norootforbuild

Name:           open-xchange-spamhandler-spamexperts
Provides:	open-xchange-spamhandler
Conflicts:      open-xchange-spamhandler-default
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-server
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
%define		ox_release 1
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Spamexperts Plugin
Requires:       open-xchange-common open-xchange-global open-xchange-server
#

%package gui
Group:          Applications/Productivity
Requires:       open-xchange-gui >= @OXVERSION@
Summary:        The Open-Xchange Spamexperts Plugin GUI part

%description
The Open-Xchange Spamexperts Plugin

Authors:
--------
    Open-Xchange

%description gui
The Open-Xchange Spamexperts Plugin GUI part

Authors:
--------
    Open-Xchange


%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define docroot /var/www/html
%else
%define docroot /srv/www/htdocs
%endif

ant -Dguiprefix=%{docroot}/ox6 -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/groupware/settings
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/settings/*.properties
%config(noreplace) /opt/open-xchange/etc/groupware/*.properties

%files gui
%defattr(-,root,root)
%{docroot}/ox6/plugins/com.openexchange.custom.spamexperts

%changelog
* Thu Jan 19 2012 - karsten.will@open-xchange.com
  - moved to CVS_HEAD as it will be generally available from now on
* Thu Jan 12 2012 - karsten.will@open-xchange.com
  - Changed name to open-xchange-spamhandler-spamexperts
  - runs with cvs_head again
* Fri Oct 08 2010 - manuel.kraft@open-xchange.com
  - Initial Import
