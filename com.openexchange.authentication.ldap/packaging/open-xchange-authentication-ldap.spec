
# norootforbuild

Name:           open-xchange-authentication-ldap
Provides:	open-xchange-authentication
Conflicts:	open-xchange-authentication-database
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-configread
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
Summary:        The Open-Xchange LDAP Authentication
Requires:       open-xchange-common open-xchange-global open-xchange-configread
#

%description
The Open-Xchange LDAP Authentication

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%post

if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # prevent bash from expanding, see bug 13316
   GLOBIGNORE='*'

   # SoftwareChange_Request-394
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ldapauth.properties
   if ! ox_exists_property adsBind  $pfile; then
      ox_set_property adsBind "false" $pfile
   fi
   if ! ox_exists_property referral $pfile; then
      ox_set_property referral "follow" $pfile
   fi

   # SoftwareChange_Request-210
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ldapauth.properties
   if ! ox_exists_property subtreeSearch $pfile; then
       ox_set_property subtreeSearch "false" $pfile
   fi
   if ! ox_exists_property searchFilter $pfile; then
       ox_set_property searchFilter "(objectclass=posixAccount)" $pfile
   fi
   if ! ox_exists_property bindDN $pfile; then
       ox_set_property bindDN "" $pfile
   fi
   if ! ox_exists_property bindDNPassword $pfile; then
       ox_set_property bindDNPassword "" $pfile
   fi

   # SoftwareChange_Request-134
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ldapauth.properties
   if ! ox_exists_property ldapReturnField $pfile; then
       ox_set_property ldapReturnField "" $pfile
   fi

   ox_update_permissions "/opt/open-xchange/etc/groupware/ldapauth.properties" root:open-xchange 640
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/groupware/ldapauth.properties

%changelog
* Fri Sep 03 2010 - choeger@open-xchange.com
 - Added new functions adsBind and referral
* Tue Feb 02 2010 - dennis.sieben@open-xchange.com
 - Bugfix #15309: [L3] open-xchange-authentication-ldap: multiple OUs works
   only for LDAP-Server on localhost
   - Enabled usage of config file parameter for user search
