
# norootforbuild

Name:           open-xchange-push-mailnotify
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-push >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-configread >= @OXVERSION@
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
%define		ox_release 4
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Mail Push Bundle
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-push >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-configread >= @OXVERSION@
#

%description
Open-Xchange Mail Push Bundle

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

   # SoftwareChange_Request-683
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/push_mailnotify.properties
   if ! ox_exists_property com.openexchange.push.mail.notify.use_ox_login $pfile; then
       ox_set_property com.openexchange.push.mail.notify.use_ox_login false $pfile
   fi
   if ! ox_exists_property com.openexchange.push.mail.notify.use_full_email_address $pfile; then
       ox_set_property com.openexchange.push.mail.notify.use_full_email_address false $pfile
   fi

   # SoftwareChange_Request-449
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/push_mailnotify.properties
   if ! ox_exists_property com.openexchange.push.mail.notify.udp_listen_multicast $pfile; then
      ox_set_property com.openexchange.push.mail.notify.udp_listen_multicast false $pfile
   fi
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/groupware/
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/push_mailnotify.properties
%changelog
* Mon Mar 14 2011 - choeger@open-xchange.com
 - Bugfix #18615 - [L3] mail push does not work when login does not match mailbox name
* Thu Sep 23 2010 - wolfgang.rosenauer@open-xchange.com
 - Support multicast networking
* Thu Sep 16 2010 - wolfgang.rosenauer@open-xchange.com
 - Bugfix: allow imapLoginDelimiter to be null
 - Minor string changes
* Tue Jun 22 2010 - choeger@open-xchange.com
 - Bugfix #16001 - mail push bundle does not work on installations with cyrus virtdomains switched on
* Thu Apr 08 2010 - choeger@open-xchange.com
 - Bugfix #15822: open-xchange-push-mailnotify uses wrong configuration option names
