
# norootforbuild

Name:           open-xchange-messaging-facebook
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-genconf-mysql >= @OXVERSION@ open-xchange-messaging >= @OXVERSION@ open-xchange-messaging-generic >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-html >= @OXVERSION@ open-xchange-oauth >= @OXVERSION@ open-xchange-secret >= @OXVERSION@
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
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 12
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Messaging Facebook Bundle
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-genconf-mysql >= @OXVERSION@ open-xchange-messaging >= @OXVERSION@ open-xchange-messaging-generic >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-html >= @OXVERSION@ open-xchange-oauth >= @OXVERSION@ open-xchange-oauth-facebook >= @OXVERSION@ open-xchange-secret >= @OXVERSION@
%if 0%{?sles_version} >= 10
Requires:   open-xchange-xerces-ibm
Conflicts:  open-xchange-xerces-sun
%else
Requires:   open-xchange-xerces-sun
Conflicts:  open-xchange-xerces-ibm
%endif
#

%description
The Open-Xchange Messaging Facebook Bundle

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

   # SoftwareChange_Request-735
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/facebookmessaging.properties
   if ! ox_exists_property com.openexchange.messaging.facebook $pfile; then
      ox_set_property com.openexchange.messaging.facebook "true" $pfile
   fi
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/groupware/
%dir /opt/open-xchange/etc/admindaemon/
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/facebookmessaging.properties
