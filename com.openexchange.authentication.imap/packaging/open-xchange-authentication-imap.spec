
# norootforbuild

Name:           open-xchange-authentication-imap
Provides:	open-xchange-authentication
Conflicts:	open-xchange-authentication-database,open-xchange-authentication-ldap 
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common open-xchange-server
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
%if %{?fedora_version} <= 8
BuildRequires:  java-devel-icedtea saxon
%endif
%endif
%if 0%{?centos_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 24
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange IMAP Authentication
Requires:       open-xchange-common open-xchange-server
#

%description
The Open-Xchange IMAP Authentication with multiple IMAP Server Support

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%post

if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # prevent bash from expanding, see bug 13316
   GLOBIGNORE='*'

   # SoftwareChange_Request-153
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imapauth.properties
   if ! ox_exists_property com.openexchange.authentication.imap.imapAuthEnc $pfile; then
       ox_set_property com.openexchange.authentication.imap.imapAuthEnc "UTF-8" $pfile
   fi

fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/imapauth.properties

%changelog
* Wed Nov 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #14891: Using proper login to check IMAP authentication
* Mon Oct 05 2009 - dennis.sieben@open-xchange.com
 - Bugfix #14634: imap authentication bundle doesn't support utf-8 passwords
   - Added configuration setting to configure charset
* Mon Jul 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14213: Setting configuration file permissions to reduce readability to OX processes.
* Wed Dec 24 2008 - manuel.kraft@open-xchange.com
 - Features added:
            Imapservers defined in OX User Accounts can be used instead of single IMAP Server defined in config -> USE_MULTIPLE=false
            Added SSL/TLS Support for config defined imap connections -> IMAP_USE_SECURE=false
* Thu Nov 20 2008 - choeger@open-xchange.com
 - Bugfix ID#12581: [L3] imapauth plugin ships file imapauth.properties but
   requires imapauthplugin.properties
