
# norootforbuild
%define         configfiles     configfiles.list

Name:           open-xchange-admin
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-server >= @OXVERSION@
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
%define		ox_release 7
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Admin Daemon
Requires:	open-xchange-admin-lib >= @OXVERSION@
Requires:	open-xchange >= @OXVERSION@ open-xchange-publish-basic >= @OXVERSION@ open-xchange-subscribe >= @OXVERSION@
%if 0%{?suse_version}
Requires:  mysql-client >= 5.0.0
%endif
%if 0%{?fedora_version} || 0%{?rhel_version}
Requires:  mysql >= 5.0.0
%endif
#

%package -n	open-xchange-admin-client
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Daemon RMI client library


%description -n open-xchange-admin-client
The Open Xchange Admin Daemon RMI client library

Authors:
--------
    Open-Xchange

%package -n	open-xchange-admin-lib
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Daemon Bundle client library
Requires:       open-xchange-server >= @OXVERSION@


%description -n open-xchange-admin-lib
The Open Xchange Admin Daemon Bundle client library

Authors:
--------
    Open-Xchange

%package -n	open-xchange-admin-doc
Group:          Applications/Productivity
Summary:	Documentation for the Open Xchange RMI client library.


%description -n open-xchange-admin-doc
Documentation for the Open Xchange RMI client library.

Authors:
--------
    Open-Xchange



%description
Open Xchange Admin Daemon containing commandline tools and provisioning
interface to manage users, groups, resources and Open Xchange database and
storage related setup information.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
mkdir -p %{buildroot}/sbin

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb doc install
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb install-client
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb install-bundle
mv doc javadoc
ln -sf ../etc/init.d/open-xchange-admin %{buildroot}/sbin/rcopen-xchange-admin

rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc/admindaemon \
        -maxdepth 1 -type f \
        -not -name mpasswd \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/configdb\.properties)$;$1 %%%attr(640,root,root) $2;' %{configfiles}
cat %{configfiles}

%clean
%{__rm} -rf %{buildroot}

%post


if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

  GLOBIGNORE='*'

   # SoftwareChange_Request-70
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/admindaemon/system.properties
   if ox_exists_property configDB $pfile; then
      ox_remove_property configDB $pfile
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12517
   pfile=/opt/open-xchange/etc/admindaemon/cache.ccf
   if ! ox_exists_property jcs.region.OXFolderCache.elementattributes.IsLateral $pfile; then
      ox_set_property jcs.region.OXFolderCache.elementattributes.IsLateral false $pfile
   else
      oldval=$(ox_read_property jcs.region.OXFolderCache.elementattributes.IsLateral $pfile)
      if [ "$oldval" != "false" ]; then
          ox_set_property jcs.region.OXFolderCache.elementattributes.IsLateral false $pfile
      fi
   fi

   pfile=/opt/open-xchange/etc/admindaemon/configdb.properties
   if ox_exists_property writeOnly $pfile; then
      wonly=$(ox_read_property writeOnly $pfile)
      if [ "$wonly" != "true" ]; then
         ox_set_property writeOnly true $pfile
      fi
   else
      ox_set_property writeOnly true $pfile
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12576
   for pname in User Group; do
      pfile=/opt/open-xchange/etc/admindaemon/${pname}.properties
      ox_system_type
      type=$?
      if [ $type -eq $DEBIAN ]; then
	  ofile="${pfile}.dpkg-dist"
      else
	  ofile="${pfile}.rpmnew"
      fi
      if [ -n "$ofile" ] && [ -e "$ofile" ]; then
	  for ll in NL_NL SV_SV ES_ES; do
	      nl=
	      if [ "$pname" == "User" ]; then
		  vstr="SENT_MAILFOLDER TRASH_MAILFOLDER DRAFTS_MAILFOLDER SPAM_MAILFOLDER CONFIRMED_SPAM_MAILFOLDER CONFIRMED_HAM_MAILFOLDER"
	      else
		  vstr="DEFAULT_CONTEXT_GROUP"
	      fi
	      for pp in $vstr; do
		llpp="${pp}_${ll}"
		if ! ox_exists_property $llpp $pfile; then
		    if [ -z "$nl" ]; then
			echo >> $pfile
		    fi
		    defv=$(ox_read_property $llpp $ofile)
		    ox_set_property $llpp "$defv" $pfile
		    nl=true
		fi
	      done
	  done
      fi
   done

   ox_update_permissions "/opt/open-xchange/etc/admindaemon/configdb.properties" root:root 640
   ox_update_permissions "/opt/open-xchange/etc/admindaemon/mpasswd" root:root 640
fi



%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/etc/admindaemon
%dir /opt/open-xchange/etc/admindaemon/osgi
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
/etc/init.d/*
/sbin/*
%config(noreplace) %attr(600,root,root) /opt/open-xchange/etc/admindaemon/mpasswd
/opt/open-xchange/etc/admindaemon/mysql
/opt/open-xchange/etc/admindaemon/osgi/config.ini.template
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*


%files -n open-xchange-admin-client
%defattr(-,root,root)
%dir /opt/open-xchange/lib/
%dir /opt/open-xchange/sbin
/opt/open-xchange/lib/*
/opt/open-xchange/sbin/*

%files -n open-xchange-admin-lib
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*

%files -n open-xchange-admin-doc
%defattr(-,root,root)
%doc javadoc
%changelog
* Mon Aug 31 2009 - marcus.klein@open-xchange.com
 - Bugfix #14178: Additionally to check on the database a String.equals() check is added. The collation is changed to utf_8_bin on column
   uid of table login2user.
* Mon Aug 31 2009 - thorben.betten@open-xchange.com
 - Bugfix #14421: Added new virtual folder tables to SQL initialization scripts
* Fri Aug 28 2009 - marcus.klein@open-xchange.com
 - Bugfix #13874: Cached information where context is stored must be invalidated if context is removed.
* Thu Jul 30 2009 - dennis.sieben@open-xchange.com
 - Bugfix #14257: allpluginsloaded doesn't honor fragment bundles
     added handling for fragment bundles
* Mon Jul 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14213: Setting configuration file permissions to reduce readability to OX processes.
* Wed Jul 15 2009 - marcus.klein@open-xchange.com
 - Bugfix #14158: Setting attribute value to alias using the PreparedStatement instead of error prone quoting in SQL statement.
* Tue Jun 30 2009 - marcus.klein@open-xchange.com
 - Bugfix #13477: If a user is deleted a connection without timeout is used because a lot of data must be moved taking a lot of time.
* Fri Jun 26 2009 - marcus.klein@open-xchange.com
 - Bugfix #13951: Writing understandable exception to RMI client if database is updated.  
* Thu Jun 25 2009 - marcus.klein@open-xchange.com
 - Bugfix #13987: Removed using of general classes that are not available in command line tools.  
* Tue Jun 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #13852: Adding OSGi services for creating and removing genconf, publish and subscribe tables to admin.
* Mon Jun 15 2009 - marcus.klein@open-xchange.com
 - Bugfix #6692: Renamed group 0 to "All users" and group 1 to "Standard group". An update task fixes values in the database.
* Fri Jun 12 2009 - marcus.klein@open-xchange.com
 - Bugfix #13849: Added missing primary mail account identifier when updating it.
* Thu May 28 2009 - dennis.sieben@open-xchange.com
 - Bugfix #13733: antispam plugin cannot be enabled via soap
     renamed variable inside of user object
* Thu May 21 2009 - dennis.sieben@open-xchange.com
 - Bugfix #13606: [L3] Admin doesn't provide parameters to edit a user's mail_upload quota settings
* Thu Apr 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #13440: Clearing string values with the CLT can be done with an empty string.
* Tue Feb 24 2009 - choeger@open-xchange.com
 - Bugfix #12517: [L3] Foldercache does not synchronize properly
     set jcs.region.OXFolderCache.elementattributes.IsLateral=false on update
* Mon Feb 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #13248: Checking existance of a group on already existing connection to prevent problems with not committed groups. 
* Fri Feb 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #12791: Checking existence of user's default group prior to performing a storage insert
* Thu Jan 29 2009 - choeger@open-xchange.com
 - Bugfix ID#13087 Credentials cache not cleared when deleting a context
* Wed Jan 07 2009 - marcus.klein@open-xchange.com
 - Bugfix #12864: Only creating the statement if the available flag is set.
* Wed Jan 07 2009 - choeger@open-xchange.com
 - Bugfix ID#12576: add new translations for swedish, dutch and spanish with update
* Thu Dec 18 2008 - francisco.laguna@open-xchange.com
 - Bugfix ID#12576: Added translations for swedish, dutch and spanish to config files.
* Tue Dec 16 2008 - francisco.laguna@open-xchange.com
 - Bugfix ID#9782: Abort with an error on invalid values for boolean fields. Be clearer in describing allowed values for boolean fields.
 - Bugfix ID#12012: Added JAVA_XTRAOPTS to createcontext, createuser, oxreport, allpluginsloaded and showruntimestats scripts.
* Mon Dec 15 2008 - francisco.laguna@open-xchange.com
 - Bugfix ID#12052: Throw NoSuchContextException if authentication is not enough to stop a list user call.
* Thu Dec 11 2008 - thorben.betten@open-xchange.com
 - Bugfix ID#12585: Including mail attribute checks performed on user update on user
   creation, too
* Mon Dec 01 2008 - choeger@open-xchange.com
 - Bugfix ID#12643 API: listcontext does not work when authentication is disabled
* Tue Oct 28 2008 - marcus.klein@open-xchange.com
 - Bugfix #12392: Removed the creation of some tables only needed for OXEE.
* Tue Oct 28 2008 - dennis.sieben@open-xchange.com
 - guiPreferences can be set now through SOAP
* Mon Oct 27 2008 - choeger@open-xchange.com
 - Bugfix ID#12287 JAVA_OXCMD_OPTS option missing after upgrade from SP3 to
 SP4 in ox-admin-scriptconf.sh
* Thu Oct 23 2008 - choeger@open-xchange.com
 - Bugfix ID#12286: connection timeouts not added for admin configdb.properties
* Wed Oct 22 2008 - choeger@open-xchange.com
 - Bugfix ID#12288: admin system.properties not needed options kept
   from upgrade SP3 to SP3
 - Bugfix ID#12289: DEFAULT_PASSWORD_MECHANISM=SHA added to AdminDaemon.properties
* Tue Sep 23 2008 - marcus.klein@open-xchange.com
 - Bugfix #12207: Removed OX connection given to context storage.
* Tue Sep 09 2008 - choeger@open-xchange.com
 - Setting writeOnly to true per default on new and updated installations (postinst)
   (see Bug #11595)
* Tue Sep 09 2008 - thorben.betten@open-xchange.com
 - Bugfix ID#11526: Using javax.mail.InternetAddress class to parse address strings
* Wed Sep 03 2008 - choeger@open-xchange.com
 - added new option writeOnly to configdb to be able to
   eleminate any connections to database slave within server api calls
   (see Bug #11595)
* Mon Aug 25 2008 - choeger@open-xchange.com
 - Bugfix ID#12052 listuser on non existent context returns SQL error
* Tue Aug 12 2008 - choeger@open-xchange.com
 - 11722 update from SP3, wrong cache.ccf file for admin in system.properties
 - Bugfix ID#11855 default for access-edit-group access-edit-resource access-edit-password
  is on in cmdline help output
* Mon Aug 11 2008 - choeger@open-xchange.com
 - Bugfix ID#11892 ModuleAccessDefinitions.properties wrong for pim_plus
* Wed Aug 06 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11847: rmi: NULL not accepted any more for auth
* Tue Jul 29 2008 - marcus.klein@open-xchange.com
 - Bugfix #11681: Removed a lot of .toString() in debug messages to prevent
   NullPointerExceptions.
 - Bugfix #11740: Checking for possible null value for guiPreferences attribute
   in User.equals method.
* Fri Jul 18 2008 - choeger@open-xchange.com
 - Bugfix ID#11682 [L3] NullPointerException when switching to debug mode for admin daemon
* Tue Jul 15 2008 - choeger@open-xchange.com
 - Bugfix #11642 RHEL5 Packages don't depend on Sun Java 1.5 and mysql-server
  Packages
 - Bugfix ID#11640 SP3->SP4 update fails for package open-xchange-admin-doc
* Thu Jul 10 2008 - choeger@open-xchange.com
 - RMI API Change:
   User.setTimezone and User.getTimezone now uses java.lang.String instead of java.util.TimeZone
 - Bugfix ID#11594 Unable to create a user via commandline tool when setting a timezone
 - Bugfix ID#11596 Installation fails on SLES10 64Bit
* Mon Jun 30 2008 - choeger@open-xchange.com
 - Bugfix ID#11401 module access changes only take affect after restarting the groupware
* Mon Jun 23 2008 - choeger@open-xchange.com
 - Bugfix ID#11437 ClassNotFoundException instead of real error code on SLES10
   nothing SLES specific; commandline clients do not know anything about mysql
   internal classes; fix: Do not wrap SQLExceptions.
* Wed Jun 11 2008 - choeger@open-xchange.com
 - Bugfix ID#11344 listuser fails with NoClassDefFoundError
 - Bugfix ID#11405 unable to set the password mechanism on commandline
* Mon Jun 02 2008 - marcus.klein@open-xchange.com
 - Bugfix #11327: Checking for possible null values in GUI configuration.
* Mon Jun 02 2008 - manuel.kraft@open-xchange.com
 - Bugfix #11094: no error message when cid is in remove lmappings remove list using changecontext
* Mon Apr 28 2008 - choeger@open-xchange.com
  - Bugfix ID#11139 "No admin user found in context" on changeuser execution
  - Bugfix ID#11179 deleteuser doesn't delete user configuration
  - Bugfix ID#11147 "Last modified from" should not be 0
  - Bugfix ID#11185 No protocol identifier created when adding a user with --imap/smtpserver attribute
* Mon Jan 21 2008 - dennis.sieben@open-xchange.com
  - Bugfix ID#10805 One PreparedStatement isn't closed in OXToolMySQLStorage
* Thu Dec 20 2007 - choeger@open-xchange.com
  - Bugfix ID#10659 [HEAD] "Object was not returned" in database pool after context operations
* Tue Dec 11 2007 - choeger@open-xchange.com
  - Bugfix ID#10596 using read connections results into permanent problems in replication scenarios
* Fri Dec 07 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10577 [HEAD ]admin does breake Database replication
  - Bugfix ID#10578 [HEAD] setDefaultSenderAddress does not work in changeUser
* Tue Nov 13 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10180 RuntimeExceptions aren't logged
  - Bugfix ID#10188 Everytime a write connection is used it should be checked if a rollback is made there
* Mon Nov 12 2007 - choeger@open-xchange.com
  - Bugfix ID#9835 [L3] hanging or long taking command line tools
* Thu Nov 08 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10125 No rollback if filestore creation fails during createuser
* Wed Nov 07 2007 - manuel.kraft@open-xchange.com
  - Bugfix ID#10050 Database leftovers of deleted contexts
* Tue Nov 06 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10072 L3: display name of contact blocks creation of new user
* Wed Oct 31 2007 - manuel.kraft@open-xchange.com
   - Bugfix ID#10005 [HEAD] Connections to database not returned in admindaemon
* Mon Oct 29 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9983 Changing user by name isn't running any more
* Tue Oct 23 2007 - manuel.kraft@open-xchange.com
   - Bugfix ID#9938 [HEAD] cache does not get invalidated when users are added to or removed from groups
* Tue Oct 23 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9620 RMI Api documentation lacks proper language handling description
* Thu Oct 18 2007 - manuel.kraft@open-xchange.com
   - Bugfix ID#9860 Unable to delete Contact: Context 30 Contact 2 on delete context
* Mon Oct 15 2007 - manuel.kraft@open-xchange.com
  - Bugfix ID#9806 [HEAD] username can't be changed
  - Bugfix ID#9805 [HEAD] No error message when username is going to be changed
* Fri Oct 12 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9786 L3: IMAP port is stripped from commandline
* Thu Oct 11 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9777 no adequate description of parameters
* Wed Oct 10 2007 - choeger@open-xchange.com
 - Bugfix ID#9767 [L3] changing admin password with disabled authentication does not work
* Mon Oct 08 2007 - manuel.kraft@open-xchange.com
 - Bugfix ID#9616 Deleted users are not removed from Global Addressbook in Outlook
* Wed Sep 26 2007 - choeger@open-xchange.com
  - Bugfix ID#9582 osgi/config.ini does not exist on upgrading the
  open-xchange-admin
* Thu Sep 20 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9592 Language is not in the output of listuser --csv CLT
* Tue Sep 11 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9362 <addmembers> should be <userid>
* Fri Sep 07 2007 - choeger@open-xchange.com
 - Bugfix #9235 Crypt implementation should be able to handle UTF-8
* Wed Sep 05 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9254 showruntimestats gives NullPointerException
* Fri Aug 31 2007 - choeger@open-xchange.com
  - Bugfix ID#9156 Malformed path to HTMLEntities.properties
* Wed Aug 22 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9023 createcontext name should be added to lmappings
  - Bugfix ID#9047 createuser not needed parameter --name
  - Bugfix ID#9046 createuser text for --extendedoptions printed on sterr
  - Bugfix ID#9034 createcontext null printed instead of cid in case of error
  - Bugfix ID#8735 Some code clean open-xchange-admin
* Tue Aug 21 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8998 --nonl not consistent
  - Bugfix ID#8985 createuser doesn't check --timezone format
* Tue Aug 21 2007 - choeger@open-xchange.com
  - Bugfix ID#8995 oxinstaller return value is always 0
    ox_set_property now checks whether propfile exists
* Mon Aug 20 2007 - manuel.kraft@open-xchange.com
 - Bugfix ID#8875 No email validation for /opt/open-xchange/sbin/createcontext
* Thu Aug 16 2007 - choeger@open-xchange.com
  - Bugfix ID#8895 Misleading server response if "listuser --csv" doesn't find any match
* Thu Aug 16 2007 - manuel.kraft@open-xchange.com
 - Bugfix ID#8839 "Virtual" PIM folders are not updated when changing users displayname
* Thu Aug 16 2007 - dennis.sieben@open-xchange.com
 - Bugfix ID#8830 if user is deleted and plugin error occures, no message is shown.
* Tue Aug 14 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8802 no error message when display_name is changed
  - Bugfix ID#8837 'createuser' command line tool shows wrong default settings for modularization
* Tue Aug 07 2007 - choeger@open-xchange.com
 - Bugfix ID#8679 new admin does not work with log4j
* Tue Aug 07 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8593 Operations by name not possible
  - Bugfix ID#8571 Problems with multiple unique displaynames for internal users
  - Bugfix ID#8703 listcontext has incorrect data-mapping
* Mon Aug 06 2007 - manuel.kraft@open-xchange.com
 - Bugfix ID#8694: Users which are downgraded to "Basic" are still able to create public folders.
* Thu Aug 02 2007 - choeger@open-xchange.com
  - Bugfix ID#8666 imapLogin field get lost after changing module access
  - Bugfix ID#8611 admindaemon must use groupware config files
    now only system.properties, configdb.properties and cache.ccf is part of
    open-xchange-admin. The rest is used from the groupware config files (see system.properties)
* Thu Jul 26 2007 - dennis.sieben@open-xchange.com
 - Bugfix ID#8553 CLT: After running CLT no reasonable message on console appear for user
* Wed Jul 25 2007 - choeger@open-xchange.com
 - Bugfix ID#8556 CLTs: <datevalue> not working in "createuser"
   fixed parsing of dates and added dateformat format environment options,
   see --environment commandline parameter for known options
* Wed Jul 11 2007 - choeger@open-xchange.com
 - Bugfix ID#8397 listgroup: "email" in csv output
* Wed Jul 11 2007 - manuel.kraft@open-xchange.com
 - Bugfix ID#8400 listresource: attribute available not in csv output
* Mon Jul 09 2007 - choeger@open-xchange.com
 - Bugfix ID#8336 Unable to change user using the changeuser script
* Thu Jul 05 2007 - choeger@open-xchange.com
 - Bugfix ID#7682 (user setup) deleted user is not automatically logged off (syntax error message is displayed)
* Mon Jul 02 2007 - choeger@open-xchange.com
 - Bugfix ID#8254 Cryptic error message when updatetask is started
* Wed Jun 27 2007 - manuel.kraft@open-xchange.com
  - Bugfix ID#8122 	RMI Authentication must be able to be switched off!
* Wed Jun 20 2007 - choeger@open-xchange.com
  - Bugfix ID#7310 createuser is not able to set imapLogin in user table
* Tue Jun 19 2007 - choeger@open-xchange.com
  - Bugfix ID#8076 German user created with english folders
* Mon Jun 18 2007 - choeger@open-xchange.com
 - Bugfix ID#7843 admin can only handle unix crypt in RMI auth
 - Bugfix ID#7757 (spam) spam training in GUI is available if spam is disabled for user
   added missing spam_trainer disable method
* Mon Jun 18 2007 - manuel.kraft@open-xchange.com
 - Bugfix #7833	  	Renaming a resource with the same mail address doesn't work
* Fri Jun 15 2007 - manuel.kraft@open-xchange.com
 - Bugfix #8019  	OXEE password of new created contexts is always secret
* Wed Jun 13 2007 - choeger@open-xchange.com
 - Bugfix ID#7972 Fresh install - admin cannot login
 - Bugfix ID#7803 FQDN is replaced by a DHCP value after installation
* Tue Jun 12 2007 - manuel.kraft@open-xchange.com
 - Bugfix #7886:  	Translation of "Spam"
* Tue Jun 12 2007 - dennis.sieben@open-xchange.com
 - Bugfix #7816: User: Error when explicitly adding a new user to Group users
* Mon Jun 11 2007 - dennis.sieben@open-xchange.com
 - Bugfix #7855: (umin) changing password of user imap is no longer available for user
* Tue Jun 05 2007 - choeger@open-xchange.com
 - Implemented context admin authentication caching
* Wed May 30 2007 - thorben.betten@open-xchange.com
 - Bugfix #7615: Setting equal size to VARCHAR field 'fname' in both tables
   'oxfolder_tree' and 'del_oxfolder_tree'
* Fri May 25 2007 - choeger@open-xchange.com
  - Bugfix ID#7568 alias can't be set without the need to set PRIMARY_MAIL or EMAIL1
* Mon May 21 2007 - choeger@open-xchange.com
  - Bugfix ID#7340 user create -> can add aliases which are not acceptable
  - Bugfix ID#7342 can create a group with invalid email address
* Fri May 18 2007 - manuel.kraft@open-xchange.com
  -  Bugfix ID#7345 After update, login as admin not possible
* Mon May 14 2007 - manuel.kraft@open-xchange.com
  -  Fixed check for invalid locate data in users language in user.Change()
* Fri May 11 2007 - manuel.kraft@open-xchange.com
  -  Bugfix ID#7186 Fixed property in foldercache.properties that a user can edit his own personal data via groupware interface!
  -	 Fixed invalid alias creation when client sends an alias array with "" data entries.
