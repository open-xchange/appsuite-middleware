
# norootforbuild

Name:           open-xchange-management
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
Summary:        The Open-Xchange Management Bundle
Requires:       open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@
#

%description
The Open-Xchange Management Bundle

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

%post

if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # prevent bash from expanding, see bug 13316
   GLOBIGNORE='*'

   # SoftwareChange_Request-135
   # -----------------------------------------------------------------------
   ofile=/opt/open-xchange/etc/groupware/server.properties
   nfile=/opt/open-xchange/etc/groupware/management.properties
   if [ -f $ofile ]; then
      for prop in JMXPort JMXBindAddress JMXLogin JMXPassword; do
          if ox_exists_property $prop $ofile; then
              oldval=$(ox_read_property $prop $ofile)
              ox_set_property $prop "$oldval" $nfile
              ox_remove_property $prop $ofile
          fi
      done
   fi
   ofile=/opt/open-xchange/etc/admindaemon/plugin/hosting.properties
   nfile=/opt/open-xchange/etc/admindaemon/management.properties
   if [ -f $ofile ]; then
      for prop in JMXPort JMXBindAddress JMXLogin JMXPassword; do
          if ox_exists_property $prop $ofile; then
              oldval=$(ox_read_property $prop $ofile)
              ox_set_property $prop "$oldval" $nfile
              ox_remove_property $prop $ofile
          fi
      done
   fi
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/admindaemon
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/*.properties
%config(noreplace) /opt/open-xchange/etc/groupware/*.properties
