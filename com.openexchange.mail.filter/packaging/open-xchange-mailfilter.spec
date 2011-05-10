
# norootforbuild

Name:           open-xchange-mailfilter
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-server >= @OXVERSION@
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
%define		ox_release 9
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Server Mailfilter Bundle
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange >= @OXVERSION@
#

%description
The Open-Xchange Server Mailfilter Bundle

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

   # SoftwareChange_Request-657
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property com.openexchange.mail.filter.useUTF7FolderEncoding $pfile; then
      ox_set_property com.openexchange.mail.filter.useUTF7FolderEncoding false $pfile
   fi

   # SoftwareChange_Request-392
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property com.openexchange.mail.filter.passwordSource $pfile; then
      ox_set_property com.openexchange.mail.filter.passwordSource session $pfile
   fi
   if ! ox_exists_property com.openexchange.mail.filter.masterPassword $pfile; then
      ox_set_property com.openexchange.mail.filter.masterPassword "" $pfile
   fi

   # SoftwareChange_Request-305
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property com.openexchange.mail.filter.connectionTimeout $pfile; then
       ox_set_property com.openexchange.mail.filter.connectionTimeout 30000 $pfile
   fi

   # SoftwareChange_Request-228
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property VACATION_DOMAINS $pfile; then
       ox_set_property VACATION_DOMAINS "" $pfile
   fi

   # SoftwareChange_Request-191
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property NON_RFC_COMPLIANT_TLS_REGEX $pfile; then
      ox_set_property NON_RFC_COMPLIANT_TLS_REGEX '^Cyrus.*v([0-1]\\.[0-9].*|2\\.[0-2].*|2\\.3\\.[0-9]|2\\.3\\.[0-9][^0-9].*)$' $pfile
   fi
   if ! ox_exists_property TLS $pfile; then
      ox_set_property TLS "true" $pfile
   fi

   # SoftwareChange_Request-142
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailfilter.properties
   if ! ox_exists_property SIEVE_AUTH_ENC $pfile; then
       ox_set_property SIEVE_AUTH_ENC "UTF-8" $pfile
   fi

   ox_update_permissions "/opt/open-xchange/etc/groupware/mailfilter.properties" root:open-xchange 640
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace)%attr(640,root,open-xchange) /opt/open-xchange/etc/groupware/mailfilter.properties
%changelog
* Mon Apr 11 2011 - dennis.sieben@open-xchange.com
 - Bugfix #18918 - OXSieveHandlerInvalidCredentialsException must be moved to the export package
* Tue Mar 01 2011 - marcus.klein@open-xchange.com
 - Bugfix #18465: Compiling sources everywhere to Java5 compatible class files.
* Wed Dec 08 2010 - dennis.sieben@open-xchange.com
  - Bugfix #17776: "not" tests are not written back correctly
    - Added special case for "not" in RuleConverter
* Tue Sep 21 2010 - steffen.templin@open-xchange.com
 - Bugfix #16747: After removal of the admin mode oxadmin is able to manage 
 				  the users sieve scripts again within a normal groupware session.
* Tue Aug 31 2010 - dennis.sieben@open-xchange.com
  - Added possibility to define the password source. It is now possible to
    switch between the password from the session or a global defined one.
* Wed Jun 02 2010 - dennis.sieben@open-xchange.com
  - Bugfix #16149: [L3] connection timeout for SieveHandler
    - Made timeout configurable
* Tue Feb 16 2010 - dennis.sieben@open-xchange.com
  - Added possibility to define if vacation messages should only be sent to
    specific domains
* Wed Dec 16 2009 - dennis.sieben@open-xchange.com
  - Added ability to disable TLS and define the regex for non-correct working
    TLS implementations in the config file
* Wed Dec 02 2009 - dennis.sieben@open-xchange.com
  - Bugfix #14655: [L3] Sieve capability wrong if TLS is used
    - Fixed regex once again to include all Cyrus versions including 2.3.9 to 
      the implementations which aren't working correct 
