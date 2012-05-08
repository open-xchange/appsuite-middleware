
# norootforbuild

Name:           open-xchange-spamsettings-generic
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-server >= @OXVERSION@ 
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
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        This bundle provides a new messaging interface for SMS services
Requires:       open-xchange-common >= @OXVERSION@
Requires:       open-xchange-global >= @OXVERSION@
Requires:       open-xchange >= @OXVERSION@

%package -n     open-xchange-spamsettings-generic-gui
Group:          Applications/Productivity
Summary:        Generic spam settings GUI Bundle
Requires:       open-xchange-gui >= @OXVERSION@

%description -n open-xchange-spamsettings-generic-gui
Generic spam settings GUI Bundle
                                         
                                         
Authors:                                 
--------                                 
    Open-Xchange                         

%description
 This bundle provides a generic interface for spam settings


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

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Dguiprefix=%{docroot}/ox6 install installGui


%post

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/bundles/com.openexchange.spamsettings.generic.jar

%files -n open-xchange-spamsettings-generic-gui
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.spamsettings.generic
%{docroot}/ox6/plugins/com.openexchange.spamsettings.generic/*

%changelog
* Fri Jun 17 2011 - holger.achtziger@open-xchange.com
  - fix for Bug 19520 - ExtendedFormElement.java does not honor the attributes order
* Thu Jun 16 2011 - wolfgang.rosenauer@open-xchange.com
  - Remove superfluous headline in the page
* Mon Mar 14 2011 - benjamin.otterbach@open-xchange.com
  - Initial
