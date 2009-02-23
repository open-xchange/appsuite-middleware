
# norootforbuild
%define		configfiles	configfiles.list

Name:           open-xchange-server
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-conversion open-xchange-configread open-xchange-monitoring open-xchange-cache open-xchange-xml open-xchange-dataretention
%if 0%{?suse_version}
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
Version:	6.8.1
Release:	0
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Server Bundle
Requires:       open-xchange-global open-xchange-configread open-xchange-global open-xchange-conversion open-xchange-monitoring open-xchange-management open-xchange-cache open-xchange-xml open-xchange-dataretention
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
Requires:  java-1_5_0-ibm >= 1.5.0_sr9
Requires:  update-alternatives
%endif
%if %{?suse_version} >= 1100
Requires:  java-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
Requires:  java-1_5_0-sun
%endif
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
Requires:  java-1.6.0-openjdk
%endif
%if %{?fedora_version} <= 8
Requires:  java-icedtea
%endif
%endif
%if 0%{?rhel_version}
Requires:  java-1.5.0-sun
%endif
#

%package -n	open-xchange
Group:          Applications/Productivity
Summary:	Open-Xchange server scripts and configuration
Prereq:		/usr/sbin/useradd
Requires:	open-xchange-authentication open-xchange-charset open-xchange-conversion-engine open-xchange-conversion-servlet open-xchange-contactcollector open-xchange-i18n open-xchange-mailstore open-xchange-jcharset open-xchange-push-udp open-xchange-server open-xchange-sessiond open-xchange-smtp open-xchange-spamhandler, mysql >= 5.0.0


%description -n open-xchange
Open-Xchange server scripts and configuration

Authors:
--------
    Open-Xchange

%description
The Open-Xchange Server Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
mkdir -p %{buildroot}/sbin
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb installJars
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb installExceptJars installConfig

mkdir -p %{buildroot}/var/log/open-xchange

# generate list of config files for config package
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc/groupware -maxdepth 1 -type f \
	-not -name oxfunctions.sh \
	-printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}

ln -sf ../etc/init.d/open-xchange-groupware %{buildroot}/sbin/rcopen-xchange-groupware

%clean
%{__rm} -rf %{buildroot}


%pre -n open-xchange
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%post -n open-xchange


if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # -----------------------------------------------------------------------
   # bugfix id#12517
   pfile=/opt/open-xchange/etc/groupware/cache.ccf
   if ! ox_exists_property jcs.region.OXFolderCache.elementattributes.IsLateral $pfile; then
      ox_set_property jcs.region.OXFolderCache.elementattributes.IsLateral false $pfile
   else
      oldval=$(ox_read_property jcs.region.OXFolderCache.elementattributes.IsLateral $pfile)
      if [ "$oldval" != "false" ]; then
	  ox_set_property jcs.region.OXFolderCache.elementattributes.IsLateral false $pfile
      fi
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12290
   pfile=/opt/open-xchange/etc/groupware/ajp.properties
   if ! ox_exists_property AJP_LOG_FORWARD_REQUEST $pfile; then
     ox_set_property AJP_LOG_FORWARD_REQUEST FALSE $pfile
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12291
   pfile=/opt/open-xchange/etc/groupware/configdb.properties
   if ! ox_exists_property writeOnly $pfile; then
     ox_set_property writeOnly false $pfile
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12292
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.imapTemporaryDown $pfile; then
     ox_set_property com.openexchange.imap.imapTemporaryDown 10000 $pfile
   fi
   for prop in imapsPort smtpsPort; do
     if ox_exists_property $prop $pfile; then
       ox_remove_property $prop $pfile
     fi
   done
   if ! ox_exists_property com.openexchange.imap.spamHandler $pfile; then
     ox_set_property com.openexchange.imap.spamHandler DefaultSpamHandler $pfile
   fi

   # -----------------------------------------------------------------------
   # bugfix id#12296
   pfile=/opt/open-xchange/etc/groupware/system.properties
   if ox_exists_property CACHECCF $pfile; then
     ox_remove_property CACHECCF $pfile
   fi

   # we're updating from pre sp5
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ajp.properties
   if ! ox_exists_property AJP_JSESSIONID_TTL $pfile; then
      ox_set_property AJP_JSESSIONID_TTL 86400000 $pfile
   fi

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/participant.properties
   if ! ox_exists_property com.openexchange.participant.ShowWithoutEmail $pfile; then
	if ox_exists_property ShowWithoutEmail $pfile; then
	    oldval=$(ox_read_property ShowWithoutEmail $pfile)
	    ox_set_property com.openexchange.participant.ShowWithoutEmail $oldval $pfile
	    ox_remove_property ShowWithoutEmail $pfile
        else
	    ox_set_property com.openexchange.participant.ShowWithoutEmail true $pfile
	fi
   fi
   if ! ox_exists_property com.openexchange.participant.autoSearch $pfile; then
	ox_set_property com.openexchange.participant.autoSearch true $pfile
   fi
   if ! ox_exists_property com.openexchange.participant.MaximumNumberParticipants $pfile; then
	ox_set_property com.openexchange.participant.MaximumNumberParticipants 0 $pfile
   fi

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/system.properties
   for prop in InitWorker Participant SPELLCHECKCFG Contact; do
	if ox_exists_property $prop $pfile; then
	   ox_remove_property $prop $pfile
	fi
   done

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.MinimumSearchCharacters $pfile; then
      ox_set_property com.openexchange.MinimumSearchCharacters 0 $pfile
   fi

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/contact.properties
   if ! ox_exists_property com.openexchange.contact.mailAddressAutoSearch $pfile; then
	ox_set_property com.openexchange.contact.mailAddressAutoSearch true $pfile
   fi

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mail.properties
   if ! ox_exists_property com.openexchange.mail.loginSource $pfile; then
	ltype=$(ox_read_property com.openexchange.mail.loginType $pfile)
	credsrc=$(ox_read_property com.openexchange.mail.credSrc $pfile)
	if [ -n "$ltype" ] && [ -n "$credsrc" ]; then
	    if [ "$ltype" == "user" ] && [ "$credsrc" == "user.imapLogin" ]; then
	        ox_set_property com.openexchange.mail.loginSource "login" $pfile
		ox_set_property com.openexchange.mail.passwordSource "session" $pfile
		ox_set_property com.openexchange.mail.mailServerSource "user" $pfile
		ox_set_property com.openexchange.mail.transportServerSource "user" $pfile
	    elif [ "$ltype" == "user" ] && [ "$credsrc" == "session" ]; then
		ox_set_property com.openexchange.mail.loginSource "name" $pfile
		ox_set_property com.openexchange.mail.passwordSource "session" $pfile
		ox_set_property com.openexchange.mail.mailServerSource "user" $pfile
		ox_set_property com.openexchange.mail.transportServerSource "user" $pfile
	    elif [ "$ltype" == "global" ]; then
		ox_set_property com.openexchange.mail.loginSource "mail" $pfile
		ox_set_property com.openexchange.mail.passwordSource "global" $pfile
		ox_set_property com.openexchange.mail.mailServerSource "global" $pfile
		ox_set_property com.openexchange.mail.transportServerSource "global" $pfile
	    elif [ "$ltype" == "config" ]; then
		ox_set_property com.openexchange.mail.loginSource "mail" $pfile
		ox_set_property com.openexchange.mail.passwordSource "session" $pfile
		ox_set_property com.openexchange.mail.mailServerSource "global" $pfile
		ox_set_property com.openexchange.mail.transportServerSource "global" $pfile
	    fi
	  else
	      # defaults
	      ox_set_property com.openexchange.mail.loginSource "login" $pfile
	      ox_set_property com.openexchange.mail.passwordSource "session" $pfile
	      ox_set_property com.openexchange.mail.mailServerSource "user" $pfile
	      ox_set_property com.openexchange.mail.transportServerSource "user" $pfile
	  fi
	ox_remove_property com.openexchange.mail.loginType $pfile
	ox_remove_property com.openexchange.mail.credSrc $pfile
   fi

   # run checkconfigconsistency once
   /opt/open-xchange/sbin/checkconfigconsistency
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/bundles/*

%files -n open-xchange -f %{configfiles}
%defattr(-,root,root)
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/etc/groupware/osgi
%dir /opt/open-xchange/sbin
/sbin/*
/opt/open-xchange/etc/groupware/osgi/config.ini.template
/opt/open-xchange/sbin/*
%dir %attr(750,open-xchange,open-xchange) /var/log/open-xchange
/etc/init.d/open-xchange-groupware
%dir /opt/open-xchange/etc/groupware/servletmappings
%dir /opt/open-xchange/etc/groupware
/opt/open-xchange/etc/groupware/servletmappings/*
%changelog
* Wed Jan 21 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12985: Suppress notifications if only alarm setting is changed. 
* Wed Jan 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #11677: CLT control bundle tools work when JMX authentication is
   enabled
* Tue Jan 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #12954: Removing unnecessary CDATA tags from style elements
   produced by JTidy
 - Bugfix #12972: Fixed import of vCard with linked image URI
 - Bugfix #13002: Fixed through batch loading of referenced attachments
* Mon Jan 19 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12967: Set Due Date to 00:00 UTC if it is a "Date" and not a "DateTime".
 - Bugfix #12987: More lenient date parsing in CSV imports.
* Mon Jan 19 2009 - marcus.klein@open-xchange.com
 - Bugfix #12988: Improved exception message if E-Mail address is not RFC822 compliant.
 - Bugfix #13001: Fixed wrong grammar in exception message.
* Sun Jan 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #12981: Referencing found inline content by Content-Id value
* Fri Jan 16 2009 - francisco.laguna@open-xchange.com
 - Bugfix #11333: Fix SQL error when generating search string.
 - Bugfix #12790: Update version number first when removing current version.
 * Thu Jan 15 2009 - marcus.klein@open-xchange.com
 - Bugfix #12926: Task stays in delegators folder even if delegator removes himself from the participants list.
* Thu Jan 15 2009 - thorben.betten@open-xchange.com
 - Bugfix #12944: Fixed changing recurring pattern for infinite recurring
   appointments
 - Bugfix #12953: Fixed through more tolerant parsing of multipart parts
* Thu Jan 15 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12929: Don't use current time millis for generating timestamps
   in responses. Use the timestamp of the newest object in the response set
   instead.
* Wed Jan 14 2009 - marcus.klein@open-xchange.com
 - Bugfix #12239: Fixed writing of delete and change exceptions into iCal format.
* Wed Jan 14 2009 - thorben.betten@open-xchange.com
 - Bugfix #12935: Added wildcard support in IMAP search
 - Bugfix #12946: Fixed move/copy to a write-only IMAP folder
* Tue Jan 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #12922: Proper handling of empty sequence numbers when performing
   a FETCH command
* Tue Jan 13 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12925: Use task delete message when participant is removed from task.
* Mon Jan 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #12908: Fixed possible invalid sequence in FETCH command if
   non-existing UID is requested
* Mon Jan 12 2009 - francisco.laguna@open-xchange.com
 - Bugfix #10941: Added an interface test to guarantee correct server handling when removing start and end date from a task.
 - Bugfix #12904: Handle exceptions gracefully when checking permissions for links in calendar.
* Mon Jan 12 2009 - marcus.klein@open-xchange.com
 - Bugfix #12900: Improving CSV exporter and CSV parser to be compatible with each other.
 - Bugfix #12765: Suppressed warning about unknown image URL attribute for contacts.
* Sat Jan 10 2009 - thorben.betten@open-xchange.com
 - Bugfix #12902: Proper logging of expected InvalidStateException as debug
 - Bugfix #12909: Canceling setting reply headers if original mail is
   missing
* Fri Jan 09 2009 - thorben.betten@open-xchange.com
 - Bugfix #12894: Fixed deleting uploaded image(s) on auto-save action
 - Bugfix #12895: Using own routines to determine a mail part's filename
 - Bugfix #12898: Fixed iterator handling on concurrent map
 - Bugfix #12901: Allowing an InputStream when accessing a part's content
   which is considered to be a RFC822 message
* Fri Jan 09 2009 - francisco.laguna@open-xchange.com
 - Refactored the Calendar Folder Object to only use HashSets
 - Bugfix 12896: Expect UserException.Code.USER_NOT_FOUND to denote a regular resource when trying to load user.
 - Bugfix 10830: Save old console log
* Fri Jan 09 2009 - marcus.klein@open-xchange.com
 - Bugfix #7460: Server response now contains the timestamp of the confirmed appointment. 
* Wed Jan 07 2009 - thorben.betten@open-xchange.com
 - Bugfix #12869: Properly propagating change/delete exceptions to MS
   Outlook with respect to synchronizing user
 - Bugfix #12879: Validating specified folder name prior to performing a
   mail folder create/rename operation
 - Bugfix #12658: Added a recurring's termination information to series
   replacement
* Wed Jan 07 2009 - martin.herfurth@open-xchange.com
 - Bugfix #12842: Conflict handling for occurrences.
* Wed Jan 07 2009 - marcus.klein@open-xchange.com
 - Bugfix #12839: Remembering all source folders on moving tasks as dummy
   deleted entries to be able to send DELETEs on Outlook synchronization.
* Mon Jan 05 2009 - thorben.betten@open-xchange.com
 - Partial Bugfix #12839: Sending DELETE for moved appointments on Outlook
   synchronization
 - Partial Bugfix #12839: Sending DELETE for moved contacts on Outlook
   synchronization
* Tue Dec 30 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11124: Set "until" to null when switching from limited to
   unlimited series via webdav interface (which doesn't do incremental
   updates). 
* Sun Dec 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #12863: Proper unfolding of folded encoded-words as per RFC 2047
* Tue Dec 23 2008 - francisco.laguna@open-xchange.com
 - Bugfix 12862: Profiling and optimisation of free busy results.
* Mon Dec 22 2008 - francisco.laguna@open-xchange.com
 - Bugfix 12852: Extended virtual folder handling to new infostore structuring folders.
 - Bugfix 12502: Try coerceing everything to ints in ContactSetter.
* Sun Dec 21 2008 - thorben.betten@open-xchange.com
 - Bugfix 12838: Showing inline plain text attachments as downloadable
   attachment, too
* Fri Dec 19 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9765: More lenient in parsing floats in geo position for VCards.
 - Bugfix #9763: Fixed timezone parsing for VCards.
 - Bugfix #9815: Relax parsing of "Rev" in VCards, accept only days (without time) as well.
 - Bugfix #9766: Accept both URL and URI as prefix for image URIs in VCards.
* Fri Dec 19 2008 - marcus.klein@open-xchange.com
 - Bugfix #12829: Added check for possible null value.
* Thu Dec 18 2008 - marcus.klein@open-xchange.com
 - Bugfix #11311: Using a special format for float numbers to write them to the
   UI.
* Thu Dec 18 2008 - thorben.betten@open-xchange.com
 - Once again bugfix #12509: Change exception resides in same folder as
   parental recurring appointment
 - Bugfix #12737: Prepending only one empty line on reply/forward
 - Bugfix #12787: Fixed utility method to not set recurrence position when
   calculating first occurrence's start/end date
 - Bugfix #12786: Supporting (and now using) common prefix "$" for color
   labels' user flags
* Thu Dec 18 2008 - martin.herfurth@open-xchange.com
 - Bugfix #11703: Remove reminder does not cause conflict message.
* Thu Dec 18 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12790: Use loaded infoitem for permission check on detach.
* Wed Dec 17 2008 - marcus.klein@open-xchange.com
 - Bugfix #12768: Introduced a method to create database connections without
   timeouts.
* Wed Dec 17 2008 - thorben.betten@open-xchange.com
 - Bugfix #12720: Fixed synchronization of a full-update on a recurring
   appointment with MS Outlook
* Tue Dec 16 2008 - francisco.laguna@open-xchange.com
 - Bugfix 9464: Changed an error message.
* Tue Dec 16 2008 - thorben.betten@open-xchange.com
 - Bugfix #12759: Using "pre" tag on text2html conversion if tabs are
   contained in plain text to keep formatting
 - Bugfix #12678: No re-confirmation of appointments with minor changes
 - Bugfix #12754: Fixed dropped notification for master recurring
   appointment if a change exception is created
* Tue Dec 16 2008 - martin.herfurth@open-xchange.com
 - Bugfix #12730: Delete occurrence as participant.
* Mon Dec 15 2008 - marcus.klein@open-xchange.com
 - Bugfix #12569: Translated all strings for de_DE and fr_FR.
* Mon Dec 15 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9549: Changed error message for non-existing file for import.
* Mon Dec 15 2008 - thorben.betten@open-xchange.com
 - Bugfix #12744: More tolerant handling when expecting a certain item
   within a FETCH response
 - Bugfix #12738: "Empty folder" operation on trash folder (and its
   subfolders) deletes subfolders, too
* Fri Dec 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #12719: Fixed folder creation via WebDAV
 - Bugfix #12637: Notifications of recurrence exceptions show proper
   exception information
* Fri Dec 12 2008 - marcus.klein@open-xchange.com
 - Bugfix #12727: Added series occurrences to array returning requests for the
   task module.
* Thu Dec 11 2008 - marcus.klein@open-xchange.com
 - Node #1228: Added new switches for notifications to preferences tree.
* Tue Dec 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #12609: Discarding unavailable informations in notification message
* Tue Dec 09 2008 - marcus.klein@open-xchange.com
 - Bugfix #12716: Made server robust for illegal requested columns.
 - Bugfix #12414: Copying truncated information when new import exception is
   created.
* Tue Dec 09 2008 - francisco.laguna@open-xchange.com
 - Node #1228: Respect new switches in notification messages.
* Mon Dec 08 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11613: Allow empty files to be saved in infostore.
 - Bugfix #11399: Delete of locked infoitems will cause an error and not only a
   delete / edit conflict. 
* Mon Dec 08 2008 - thorben.betten@open-xchange.com
 - Bugfix #12679: Fixed notification handling for change exceptions with
   new participants
 - Bugfix #12715: Fixed wrong call sequence on JSON writer if CSV import
   operation indicates to hold warnings.
 - Bugfix #12700: Fixed correcting start/end date if full-time flag is set
   on appointment update operation
* Mon Dec 08 2008 - marcus.klein@open-xchange.com
 - Bugfix #11184: Fixed those issues for users, tasks and the preferences tree.
* Fri Dec 05 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12177: Responses for import contain an array named "warnings" with
   objects describing warnings that turned up during import.
 - Bugfix #12673: More user friendly error message when a non ical file was
   uploaded for parsing.
* Thu Dec 04 2008 - marcus.klein@open-xchange.com
 - Bugfix #12680: Foisted UTC time zone on iCal4J Date class.
 - Bugfix #11778: Changed to proper String handling instead of byte[] causing
   charset issues.
* Thu Dec 04 2008 - thorben.betten@open-xchange.com
 - Bugfix #12681: Fixed changing a recurring appointment to "ends never"
* Wed Dec 03 2008 - martin.herfurth@open-xchange.com
 - Bugfix #12432: No conflicts during change of a free appointment.
 - Bugfix #12644: Auto deletion of Appointments during deletion of user
   with additional resources in appointment.
* Wed Dec 03 2008 - thorben.betten@open-xchange.com
 - Bugfix #12662: Participant is able to "delete" whole series containing a
   change exception
 - Bugfix #12660: Fixed invalid server response on missing error message
* Wed Dec 03 2008 - marcus.klein@open-xchange.com
 - Added database update task to correct charset and collation on all tables and
   the database itself.
* Tue Dec 02 2008 - thorben.betten@open-xchange.com
 - Bugfix #7516: Included tags when searching for contacts
 - Bugfix #12634: Proper handling of possibly failed mail initialization
* Mon Dec 01 2008 - francisco.laguna@open-xchange.com
 - Added Update Task to reintroduce foreign key pointing from infostore_document
   to infostore.
* Fri Nov 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #11891: Added possibility to access object links from group
   appointments/tasks
 - Bugfix #12641: Added missing import for creating a SSL socket
* Thu Nov 27 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12618: Nicer names for infostore folders in webdav. 
* Wed Nov 26 2008 - marcus.klein@open-xchange.com
 - Bugfix #12614: Allowing delete of a change exception in a shared folder.
* Wed Nov 26 2008 francisco.laguna@open-xchange.com
 - Bugfix #12575: Report correct lock timeout.
 - Bugfix #12279: Change case of filename via webdav.
* Wed Nov 26 2008 - thorben.betten@open-xchange.com
 - Bugfix #12553: Fixed deleting a formerly created change exception
 - Bugfix #12577: Changed mail configuration possibilities to define source
   for login, password, mail server, and transport server
   CONFIG CHANGE:
   Modified mail configuration file 'mail.properties' by
   1. Adding the following properties:
    - com.openexchange.mail.loginSource
    - com.openexchange.mail.passwordSource
    - com.openexchange.mail.mailServerSource
    - com.openexchange.mail.transportServerSource
   2. Removing the following properties:
    - com.openexchange.mail.loginType
    - com.openexchange.mail.CredSrc
* Tue Nov 25 2008 - marcus.klein@open-xchange.com
 - Bugfix #12240: Added information about schema that is updated.
 - Added DATABASE UPDATE TASK CorrectIndexes that will drop useless indexes and
   create new helpful ones.
* Tue Nov 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #12571: Fixed calculation of yearly recurring appointment
   concerning every first-fourth, last "day" in month
* Mon Nov 24 2008 - francisco.laguna@open-xchange.com
  - Bug #5557: Test to verify that updates include personal folder ids in update
    event.
* Mon Nov 24 2008 - thorben.betten@open-xchange.com
 - Bugfix #12509: Change exception resides in same folder as parental
   recurring appointment
 - Bugfix #12490: Reset of confirmation information to initial status when
   creating a change exception
 - Bugfix #12601: Proper action=has operation for full-time appointments
 - Bugfix #12551: Changed permission sets to better meet groupware
   functionality
* Fri Nov 21 2008 - thorben.betten@open-xchange.com
 - Bugfix #12494: Fixed updating a change exception through MS Outlook
* Thu Nov 20 2008 - thorben.betten@open-xchange.com
 - Bugfix #12462: No password validation in groupware's password-change
   service since admin daemon (the actual provisioning interface) does not
   validate password, too
 - Bugfix #12413: End date of action=all query made exclusive
* Wed Nov 19 2008 - thorben.betten@open-xchange.com
 - Bugfix #12567: Fixed NPE when trying to access a non-existing contact
   image
* Wed Nov 19 2008 - marcus.klein@open-xchange.com
 - Bugfix #12565: Removed filling the links array with null values.
* Wed Nov 19 2008 - marcus.klein@open-xchange.com
 - Bugfix #12569: Respecting the charset while reading PO files.
 - Bugfix #12590: Storing until and occurrences as null if occurrences is sent
   as zero.
* Tue Nov 18 2008 - thorben.betten@open-xchange.com
 - Bugfix #12406: Removing remembered JSESSIONIDs after a configurable
   amount of time
   CONFIG CHANGE: Modified AJP configuration file 'ajp.properties' by
   adding the 'AJP_JSESSIONID_TTL' property
* Tue Nov 18 2008 - martin.herfurth@open-xchange.com
 - Bugfix #12264: Checking until field.
* Mon Nov 17 2008 - stefan.preuss@open-xchange.com
 - Bugfix #12558 : Mail quota values in the JSON object are not the ones
   delivered through the MAL interface
* Fri Nov 14 2008 - marcus.klein@open-xchange.com
 - Bugfix #12528: Keeping the recurrence string for appointment change
   exceptions. Added update task to copy missing recurrence strings from the
   series appointment.
* Thu Nov 13 2008 - thorben.betten@open-xchange.com
 - Bugfix #12517: Changed policy of folder cache to perform a
   remove-and-put cycle to ensure modified folder is invalidated in
   remote/lateral caches.
   CONFIG CHANGE: Modified cache configuration file
   'cache.ccf' in order to suppress lateral distributions of folder objects
* Thu Nov 13 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12377 : Copy links and attachments to recurrence exception
* Wed Nov 12 2008 - marcus.klein@open-xchange.com
 - Bugfix #12317: Appointments lasting an entire day start 00:00 UTC.
* Wed Nov 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #12165: Added a servlet for serving image requests without a session
   ID contained in request's URL parameters
* Tue Nov 11 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12282 : Set Security service in infostore factory.
* Mon Nov 10 2008 - marcus.klein@open-xchange.com
 - Changed database authentication bundle to use only context and user OSGi
   services instead of static interfaces.
 - Bugfix 12495: Setting recurrence date position if a change exception is
   created. Added update task to fix change exception without recurrence date
   position.
* Mon Nov 10 2008 - martin.herfurth@open-xchange.com
 - Bugfix #11463: Removing all change and delete exceptions after timeframe
   update on sequence master.
* Mon Nov 10 2008 - thorben.betten@open-xchange.com
 - Bugfix #12487: Fixed loss of session parameters on session migration
* Fri Nov 07 2008 - thorben.betten@open-xchange.com
 - Bugfix #12186: Catching thrown TokenMgrError on a lexical parsing error
 - Bugfix #12238: Enhanced sieve error by host name and port and user
   informations as well
* Fri Nov 07 2008 - marcus.klein@open-xchange.com
 - Bugfix #12241: Improved performance of update task
   ContactsRepairLinksAttachments.
 - Bugfix #11190: Implemented switching the series if recurrence days is set to
   0.
* Fri Nov 07 2008 - francisco.laguna@open-xchange.com
 - Added field number_of_versions (711) to infostore attributes for bug #12427
* Thu Nov 06 2008 - marcus.klein@open-xchange.com
 - Bugfix #12442: Setting modified_by attribute if series is updated.
* Thu Nov 06 2008 - thorben.betten@open-xchange.com
 - Bugfix #12460: Lowered log level of fallback to system upload quota to
   DEBUG
 - Bugfix #12416: Extended wording of notification for deleted calendar
   objects to hint to the possibility that receiver was removed from the
   list of participants
 - Bugfix #12452: Removing time information from tasks and full-time
   appointments
 - Bugfix #12242: Splitted large batch update statement into smaller pieces
   to not exceed database's max. time-out value
 - Bugfix #12138: Added group support to entity2ACL mapping
 - Bugfix #12390: Cleaning possibly invalid text prior to passing to a XML
   element/attribute
 - Bugfix #9589: Showing PGP signatures
* Wed Nov 05 2008 - thorben.betten@open-xchange.com
 - Bugfix #12426: Properly setting modified-by replacement to session
   user's display name
 - Bugfix #12449: Writing first occurrence's end time of a recurring
   appointment to notification message
 - Bugfix #12448: Proper check of calendar object's notification flag to
   not withhold notification messages by mistake AND added delete/change
   exceptions information to notification messages
 - Bugfix #12431: Removed direct link in notification message to external
   participant
* Wed Nov 05 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12459: Accept more than one file per language and parse names
   correctly.
* Wed Nov 05 2008 - marcus.klein@open-xchange.com
 - Removed UNION sql statement arising in MySQL slow logs because MySQL
   interprets this as a query not using indexes.
* Tue Nov 04 2008 - marcus.klein@open-xchange.com
 - Bugfix #12253: Removed additional session counter variable.
* Tue Nov 04 2008 - thorben.betten@open-xchange.com
 - Requirements 2579 and 2580: Ensured folder tree consistency
 - Bugfix #12455: Establishing a secure connection if IMAP server requires
   a SSL connection
* Tue Nov 04 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11148: Survive invalid recurrence pattern on load.
* Mon Nov 03 2008 - marcus.klein@open-xchange.com
 - Bugfix #12442: Added update task to remove duplicate recurrence date position
   from appointment change exceptions.
 - Bugfix #12444: Implementing correct check for empty email address in external
   participants.
* Mon Nov 03 2008 - thorben.betten@open-xchange.com
 - Bugfix #12445: Fixed possible NPE in MailFolderUtility
 - Bugfix #12441: Added wrapping try-catch block for timer safety reasons
* Fri Oct 31 2008 - marcus.klein@open-xchange.com
 - Bugfix #12387: Improved handling of exceptions in WebDAV super class.
 - Bugfix #12437: Corrected error code if invalid credentials are supplied.
 - Bugfix #12384: Servlets must not have fields.
* Fri Oct 31 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11305: Fixed batching of deletes.
* Thu Oct 30 2008 - thorben.betten@open-xchange.com
 - Bugfix #12420: Fixed forwarding of multiple mails
 - Bugfix #12385: Fastened traversal of (user) participants
* Thu Oct 30 2008 - marcus.klein@open-xchange.com
 - Bugfix #12428: Supporting InfoStore events in UDP push framework.
* Wed Oct 29 2008 - thorben.betten@open-xchange.com
 - Bugfix #12409: Sending proper fields back to GUI on edit-draft operation
 - Bugfix #11658: Checking given destination folder's fullname to be the
   default folder ID
 - Bugfix #12270: Fixed keeping attachment on copying a contact
 - Bugfix #12271: More robust parsing of messages with possible invalid
   header lines
* Wed Oct 29 2008 - martin.herfurth@open-xchange.com
 - Bugfix #11865: Deleting corrupted Appointments.
* Tue Oct 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #12393: Enhanced tree-consistency-check on a removed group
   permission
 - Bugfix #12256: Fixed equals() method in class 'ExternalUserParticipant'
 - Bugfix #12362: Splitted large number of contacts to query into blocks
* Mon Oct 27 2008 - choeger@open-xchange-com
 - Bugfix #12370 Wrong dependency in configjump package on rpm based distributions
   removed dependency to open-xchange-configjump
* Mon Oct 27 2008 - thorben.betten@open-xchange.com
 - Bugfix #12250: Changed (and fastened) parsing of PUT data on action=list
 - Bugfix #12347: No original HTML part appended as attachment on
   draft-edit
 - Bugfix #12333: Added property to HTML parser to work in relaxed mode;
   meaning it ignores parsing errors.
 - Bugfix #12300: Fixed request type counting for monitoring information
 - Bugfix #12297: Checking a task's start/due date on day-base
 - Bugfix #12249: Improved handling of exceptions internally created by
   JavaMail when receiving a "BYE" response code.
* Mon Oct 27 2008 - tobias.prinz@open-xchange.com
 - Bugfix #9367 for German Outlook: Added several other fields that had not
   been translated before. Missing translations for French and English.
 - Bugfix #11958: Added a hack that moves timezone information to the front
   of the file to ensure the library bug is circumvented.
* Mon Oct 27 2008 - marcus.klein@open-xchange.com
 - Bugfix #12325: Flushing the WebDAV/XML output after a useful part has been
   generated. This should prevent AJP connection timeouts.
* Fri Oct 24 2008 - thorben.betten@open-xchange.com
 - Bugfix #12371: Proper notification handling if participants replacement
   is empty
* Thu Oct 23 2008 - marcus.klein@open-xchange.com
 - Bugfix #12364: Replaced use of local variable with proxy method.
 - Bugfix #12372: Removing recurrence id from interface when appointment is
   created.
* Wed Oct 22 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12361: Be more lenient with locks.
* Wed Oct 22 2008 - marcus.klein@open-xchange-com
 - Implemented UDP push for new emails in INBOX.
* Wed Oct 22 2008 - choeger@open-xchange-com
 - Bugfix #12290: AJP_LOG_FORWARD_REQUEST parameter missing in
   ajp.properties after upgrade SP3 SP4
 - Bugfix #12291: Parameter writeOnly missing in groupware
   configdb.properties after upgrade SP3 -> SP4
 - Bugfix #12292: imap.properties not correctly updated after
   update SP3 -> SP4
 - Bugfix #12296: propertie CACHECCF not removed in groupware
   system.properties after upgrade SP3 -> SP4
 - Bugfix #12293: parameters from smtp.properties are to be found in
   mail.properties after upgrade SP3->SP4
 - Bugfix #12295: MonitorJMXPort and MonitorJMXBindAddress changed
   to JMXPort and JMXBindAddress from SP3 to SP4
* Tue Oct 21 2008 - thorben.betten@open-xchange.com
 - Bugfix #12357: Adding image attachments to an inline-forwarded mail if
   it its content-disposition is INLINE but specifies a file name
* Fri Oct 17 2008 - marcus.klein@open-xchange.com
 - Bugfix #12326: Removed recurrence type from series exceptions. Writing
   recurrence id and position to AJAX interface.
* Tue Oct 14 2008 - thorben.betten@open-xchange.com
 - Bugfix #12303: Throwing an I/O error if socket connection is broken on
   write/read attempt
 - Bugfix #12202: Fixed saving draft mails
* Mon Oct 13 2008 - marcus.klein@open-xchange.com
 - Bugfix #12099: Setting modified by when updating series if a virtual
   exception is created.
 - Bugfix #12254: Merged fix made in SP3 bugfix branch.
* Mon Oct 13 2008 - thorben.betten@open-xchange.com
 - Bugfix #12205: Parsing header Content-Length into a long instead of an
   integer
* Thu Oct 09 2008 - marcus.klein@open-xchange.com
 - Bugfix #12200: Corrected german translation.
* Fri Sep 26 2008 - thorben.betten@open-xchange.com
 - Bugfix #12231: Using default separator character from 'mail.properties'
   to configure folder path prefix equal to mailing system's separator
 - Bugfix #12212: Fixed moving a change exception of a recurring appointment
* Thu Sep 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #12166: Fixed max. end date calculation for yearly recurring
   appointment
 - Bugfix #12170: Applying possibly conflicting start/end to calculation of
   free-busy-results
* Wed Sep 24 2008 - marcus.klein@open-xchange.com
 - Bugfix #12224: First remove pool from pools data structure and then destroy it.
 - Bugfix #12211: Enclosed checking existing entry and following insert or
   update operation in a transaction.
* Fri Sep 19 2008 - dennis.sieben@open-xchange.com
 - Bugfix #12183: Fixed JSON creation and reading for sieve body rule.
* Fri Sep 19 2008 - thorben.betten@open-xchange.com
 - Bugfix #12194: Using current user time zone and language for notification to
   external participants instead of system settings.
* Thu Sep 18 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9845: Include payload data in 404 response to coax konqueror into sending data.
 - Bugfix #12167: Don't double decode '+' in webdav urls. The apache already decodes them once.
* Thu Sep 18 2008 - thorben.betten@open-xchange.com
 - Bugfix #12179: Fixed forwarding of messages without a text body
 - Bugfix #12181: Proper handling of message-removed exceptions and
   checking for possible null reference on put into message cache
* Wed Sep 17 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12171: Send ocurrences in list style requests when they are requested.
 - Bugfix #12173: Allow creating delete exceptions in a series by recurrence_date_position.
* Wed Sep 17 2008 - marcus.klein@open-xchange.com
 - Bugfix #11515: Disabling mail module if IMAP login fails.
 - Bugfix #12043: Improved exception message.
 - Bugfix #10759: Not deleting of session cookies on illegal requests.
* Wed Sep 17 2008 - thorben.betten@open-xchange.com
 - Bugfix #12169: Checking specified from address if covered by allowed
   user aliases
* Tue Sep 16 2008 - marcus.klein@open-xchange.com
 - Bugfix #12063: Not sending email notification for appointments and tasks
   ending in the past.
* Mon Sep 15 2008 - marcus.klein@open-xchange.com
 - Bugfix #12035: Copying not changed recurring values for recurring check on a
   task update.
 - Bugfix #12146: Iteration of occurrences of a series appointment is done after
   fetching all possible conflicts in the complete series time frame.
* Mon Sep 15 2008 - thorben.betten@open-xchange.com
 - Bugfix #12135: Sending an appropriate message to removed and added
   participants through an update operation on an appointment instead of
   the common "the appointment has been modified" message.
* Fri Sep 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #12137: Using new session ID generator based on "java.util.UUID"
   to also compute an unique ID for random token in a very fast way
 - Bugfix #12133: Sending notification to removed (external) participant(s)
 - Bugfix #12135: Sending an appropriate message to removed participants
   through an update operation on an appointment instead of the common
   "the appointment has been modified" message
* Fri Sep 12 2008 - marcus.klein@open-xchange.com
 - Bugfix #11848: Using correct collections in TaskIterator to not break ordering.
* Tue Sep 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #12027: Added JMX call to clear the sessions for a specific user
* Tue Sep 09 2008 - marcus.klein@open-xchange.com
 - Bugfix #12099: Prevented 0 in modifiedBy of series that gets a delete
   exception.
* Mon Sep 08 2008 - marcus.klein@open-xchange.com
 - Bugfix #11667: Using new event method for changed tasks and removed old ones.
 - Bugfix #9840: Throwing an exception if a daily recurring appointment with
   BYMONTH pattern is imported via iCal.
 - Bugfix #12124: Setting folder identifier of participant always on its private
   folder if the appointment is located in private or shared folder.
* Mon Sep 08 2008 - thorben.betten@open-xchange.com
 - Bugfix #10070: Proper conflict check for whole-day and non-whole-day
   appointments
 - Bugfix #12125: Fixed removing starting whitespace characters during
   html2text conversion
 - Added enhancement as specified in bug #11702
 - Bugfix #12116: Fixed calculation of recurring appointments without an
   until date set
 - Bugfix #12054: Checking for special group identifier "0" prior to
   performing update/delete operations
* Fri Sep 05 2008 - thorben.betten@open-xchange.com
 - Bugfix #12102: Invalidating context cache on every modifying context
   operation especially on disable/enable context.
 - Bugfix #12117: Checking if referenced draft message still exists on
   auto-safe draft operation
 - Bugfix #12118: Handling possible unavailable message text body on forward
 - Bugfix #12123: Restoring order if IMAP server always sorts fetch
   responses by sequence number in ascending order
* Fri Sep 05 2008 - marcus.klein@open-xchange.com
 - Bugfix #12114: Adding recurrence pattern to conflict checks for series
   appointments if a resource is added as participant.
* Thu Sep 04 2008 - thorben.betten@open-xchange.com
 - Bugfix #12108: Proper sequence range in FETCH command if folder is empty
 - Bugfix #12111: Indicating support of UIDPLUS prior to performing
   "UID EXPUNGE" command
 - Bugfix #12104: Fixed calculating wrong until date when syncing to
   Outlook
* Thu Sep 04 2008 - francisco.laguna@open-xchange.com
 - Send import warnings to the GUI.
 - Bugfix #11869 : Remove Attendees from private appointments and send a warning.
* Wed Sep 03 2008 - thorben.betten@open-xchange.com
 - Bugfix #11865: Checking for invalid changing of recurrence information
   on a change exception update
* Wed Sep 03 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11595: Reintroduced parameter for forcing use of write connections in DBPool.
 - Partial Fix #11399: Return ids and folders in an array of objects if entries can't be deleted.
 - Bugfix #12105: Send 'alarm' and 'notification' if so requested in response to list / updates and all requests.
* Tue Sep 02 2008 - marcus.klein@open-xchange.com
 - Bugfix #10859: Added a recurring pattern check before starting a recurring
   calculation.
 - Bugfix #11920: Setting end date of an imported appointment same as start date
   if DTEND and DURATION are missing.
* Tue Sep 02 2008 - thorben.betten@open-xchange.com
 - Bugfix #11266: Using full user login on credSrc=session
 - Bugfix #11695: Again fixed weekly recurrence calculation
 - Bugfix #12092: Fixed routine to determine recurring action on
   appointment update
 - Bugfix #12096: Closing resources quietly in FolderObjectIterator
* Mon Sep 01 2008 - thorben.betten@open-xchange.com
 - Bugfix #11302: Resolving an IMAP login to an user ID may return multiple
   IDs since multiple IMAP servers are allowed in one context.
 - Bugfix #4199: Checking for possible null reference prior to updating
   message cache
 - Bugfix #9607: Setting proper end date in notification mail to participant
* Mon Sep 01 2008 - dennis.sieben@open-xchange.com
 - Bugfix #12086: Changed endWith checks in SieveHandler to startWith
* Sun Aug 31 2008 - thorben.betten@open-xchange.com
 - Bugfix #12080: Fixed ordering of parameters in error message and bytes
   are converted to a human readable string
* Fri Aug 29 2008 - thorben.betten@open-xchange.com
 - Bugfix #12036: Logging a debug message only if a reminder could not be
   found for deletion on an appointment's participants update
 - Bugfix #11826: Writing proper value for "day_in_month" in JSON response
 - Bugfix #11702: Added possibility to define used host name part in
   generated links in a separate bundle
* Thu Aug 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #11849: Applying new local IP on redirect to pass future IP
   checks
 - Bugfix #12072: Proper calculation of possible conflicting resource(s) on
   inserting/updating a recurring appointment
 - Bugfix #11903: Allowing a context admin to login to mail system if
   permitted by property "com.openexchange.mail.adminMailLoginEnabled"
   located in file "mail.properties"
* Wed Aug 27 2008 - thorben.betten@open-xchange.com
 - Bugfix #11229: Proper handling of possible NaN error while parsing
   Outlook XML
 - Bugfix #10213: Setting "notify participants" flag to false in
   appointment objects on user deletion
* Tue Aug 26 2008 - thorben.betten@open-xchange.com
 - Bugfix #12045: Updating reminder's folder reference on appointment move
 - Bugfix #11181: Links now got deleted on folder deletion, too
 - Bugfix #11617: Handling a possible null reference on mail retrieval
* Mon Aug 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #12049: Applying proper login-info to newly created session on
   user login
* Wed Aug 20 2008 - thorben.betten@open-xchange.com
 - Bugfix #12011: Implemented a commit-mechanism that modifications sent by
   outlook will only be applied if XML could be completely parsed
 - Bugfix #10708: Adding properly base64-encoded image data to user's VCard
   attached to a mail
 - Bugfix #11998: Allowing to add group "All internal users" to an existing
   appointment
 - Bugfix #11984: Avoiding display of a-tag's href content in brackets if
   a-tag's content already represents a valid link
* Tue Aug 19 2008 - thorben.betten@open-xchange.com
 - Bugfix #12023: Properly handling empty Content-Id value
* Mon Aug 18 2008 - thorben.betten@open-xchange.com
 - Bugfix #11614: Written own quota parse routine which treats a missing
   parenthesis pair in IMAP QUOTA response as no resource restrictions
 - Bugfix #12001: Moved setting of "hardDelete" argument in "deleteFolder"
   and "clearFolder" routine to mail servlet interface implementation
 - Bugfix #12003: Applying proper content-type to mail object if reference
   to content is given
* Mon Aug 18 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11797: Check all fields for length constraints, even supposedly unlimited ones.
 - Bugfix #11803: Only return relevant appointments in freebusy result.
* Mon Aug 18 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11993: Now checking for a valid email address in redirect
 - Bugfix #11480: Copied session handling parts from groupware
 - Bugfix #11946: The property file and the properties are now checked right
   at the beginning
 - Bugfix #11989: Fixed grammar file
* Fri Aug 15 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11986: VAlarms require description.
 - Bugfix #11987: Chunk multiple VCalendars if needed.
 - Bugfix #11973: Whole Day appointments start at 00:00 UTC.
* Thu Aug 14 2008 - thorben.betten@open-xchange.com
 - Bugfix #11899: Fixed routine to remove user flags from a message prior
   to append that message to a folder which does not support user flags
 - Bugfix #11881: No multiple participants added to appointment which has
   the private flag set
 - Bugfix #11737: Fixed propagating display-name modification on common
   contact update
 - Bugfix #11912: Displaying those appointments at proper position in
   mini-calendar whose time zone offset exceeds the hour-of-day
 - Partial bugfix #11980: Properly delegating limit argument to search method on
   determining unread messages
* Thu Aug 14 2008 - francisco.laguna@open-xchange.com
 - Bugfix #3907 and #8527: Allow folded values.
 - Bugfix #11919: Allow date properties as DATE without saying so in a VALUE.
 - Bugfix #11968: Export whole day appointments with DTStart and DTEnd as DATEs.
* Wed Aug 13 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11963: Export VAlarms regardless of AlarmFlag.
* Wed Aug 13 2008 - marcus.klein@open-xchange.com
 - Bugfix #11969: Resolved class name conflict by adding package names.
 - Bugfix #11928: Added parsing of resource identifier on update request from
   URL.
 - Bugfix #10859: Prevent endless loops in recurring calculation. Removed all
   Thread.getStackTrace() in non-debug code. Reimplemented reloading of user
   objects to prevent too much thread blocking. 
* Wed Aug 13 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11949: "\n" as linebreaks are replaced by "\r\n"
* Wed Aug 13 2008 - thorben.betten@open-xchange.com
 - Bugfix #11888: Checking for a draft message by message's folder on
   draft-edit
* Tue Aug 12 2008 - francisco.laguna@open-xchange.com
 - Added count to infinity to recurrence calculation to prematurely terminate calculation of patterns, that are too
   complex.
 - Bugfix #11798: The short version:
   Don't ask. Just don't.
   The longer version: When saving a recurring appointment without setting the start and end dates those will be set
   to the first ocurrences start and end date, that in turn triggering an autoaccept in the name of the user. This
   doesn't work when updating only the delete exceptions, because no folder type is set. 
* Tue Aug 12 2008 - marcus.klein@open-xchange.com
 - Bugfix #11936: Disallowed delete of group 0 and 1 and update of group 0.
* Tue Aug 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #11872: Allowing "id" attribute in HTML/CSS filter
* Mon Aug 11 2008 - francisco.laguna@open-xchange.com
 - Bugfix #10401: Default to priority NORMAL for undefined priorities.
 - Bugfix #9827: Accept resources specified as attendees of cutype resource.
* Fri Aug 08 2008 - marcus.klein@open-xchange.com
 - Bugfix #11871: Replacing dates with dates of first occurrence for appointment
   series.
 - Bugfix #11612: (partial) Loading GUI plugin enabled if group or resource
   editing is allowed for a user.
* Thu Aug 07 2008 - marcus.klein@open-xchange.com
 - Bugfix #11659: Resolving group 0 does now work for tasks.
 - Bugfix #10852: Improved exception message if some task attribute is too long.
 - Bugfix #11280: Allowed open end time range in search for tasks.
 - Bugfix #11868: Implemented proper resource handling for iCal.
* Wed Aug 06 2008 - francisco.laguna@open-xchange.com
 - Partial Fix #11384: Fixed in Infostore
* Wed Aug 06 2008 - marcus.klein@open-xchange.com
 - Bugfix #11724: Now supporting whole day iCal events without DTEND and
   DURATION.
 - Bugfix #11736: Using standard group JSON writer for all requests.
 - Bugfix #11655: Unlimited series run until 99 years unto the future.
* Tue Aug 05 2008 - thorben.betten@open-xchange.com
 - Bugfix #11829: Validating freely writable recurrence information
* Mon Aug 04 2008 - thorben.betten@open-xchange.com
 - Bugfix #11827: Fixed setting proper end date if updating a recurrence
   appointment without until/occurrence setting
* Sun Aug 03 2008 - thorben.betten@open-xchange.com
 - Bugfix #11817: Fixed ordering of user's private default folders
   according to module panel
* Thu Jul 31 2008 - thorben.betten@open-xchange.com
 - Bugfix #11772: Fixed ordering of color flags in list view
 - Bugfix #11737: Fixed propagating changing of user's display name
* Wed Jul 30 2008 - thorben.betten@open-xchange.com
 - Bugfix #10111: No duplicate reminder for recurring appointment if a
   change exception has been created
 - Bugfix #11753: Loading real contact's current folder ID update request
* Wed Jul 30 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11764: If no output is created the last chars of the output must not be deleted
* Tue Jul 29 2008 - thorben.betten@open-xchange.com
 - Bugfix #11695: Proper calculation of a weekly recurring appointment
 - Bugfix #10313: No additional English text to exception message to obey
   i18n rules
 - Bugfix #11735: Added limit for recurrence's integer values for interval
   and occurrences
* Mon Jul 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #11654: Sending a DELETE for appointments in shared folders on
   which the private flag was set.
 - Bugfix #11690: Adding an entry to backup tables when deleting a change
   exception for proper Outlook synchronization
 - Bugfix #11719: Fixed calculation of daily recurring full-time
   appointment
* Fri Jul 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #11693: Fixed formatting simple quotes ('>') to colored
   blockquotes in plain-text messages
 - Bugfix #11699: Fixed removing another pretty-printer formatting on
   html2text conversion
 - Bugfix #11701: Added ending "END:VCALENDAR" on ICal export
* Wed Jul 23 2008 - marcus.klein@open-xchange.com
 - Bugfix #9591: Eliminating duplicate found tasks in search over all folder by
   a "GROUP BY" SQL statement.
* Wed Jul 23 2008 - thorben.betten@open-xchange.com
 - Bugfix #10306: Setting proper end date for recurring appointments with
   infinite occurrences
* Tue Jul 22 2008 - thorben.betten@open-xchange.com
 - Bugfix #10845: No conflict warning on appointment update if causing
   resource(s) were removed through update
* Fri Jul 18 2008 - thorben.betten@open-xchange.com
 - Bugfix #11370: Updating main recurring appointment's last-modified
   timestamp when creating a change exception
 - Bugfix #10998: Added checks to recurrence pattern building routine to
   ensure no invalid pattern finds its way into database
 - Partial bugfix #11384: Sending proper timestamp to GUI after
   contact/appointment update
* Fri Jul 18 2008 - marcus.klein@open-xchange.com
 - Bugfix #11650: Fixed wrong SQL query if a search for tasks is done in a
   shared folder or folder with "see only own objects" right.
 - Bugfix #11384: Returning the last modified timestamp if appointment/contact
   is created/modified.
 - Bugfix #11659: Identifier of group must not be written conditionally.
* Thu Jul 17 2008 - thorben.betten@open-xchange.com
 - Bugfix #11661: Fixed deletion of an appointment in which owner was
   removed as participant
 - Bugfix #11673: Checking for null reference when determining a user's
   mail/transport provider by URL string.
 - Bugfix #11671: Invoking "unsafe" user retrieval on user storage for
   being notified about a non-existing user.
 - Bugfix #11669: Check for null reference prior to composing a new 
   subject for a forward mail
 - Bugfix #11670: Checking unknown user configuration before checking mail
   access permission
 - Bugfix #11647: Sending proper error code (403 - FORBIDDEN) to Outlook on
   permission error
 - Partial bugfix #11184: Loading user's group IDs prior to fetching corresponding
   configuration from database
* Thu Jul 17 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11050: Fixed in Infostore, and Calendar.
 - Partial Fix Bug #11453: Detect update to alarm only and omit modification event.
* Thu Jul 17 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11672: NullPointerException in MailfilterAction.java
* Wed Jul 16 2008 - thorben.betten@open-xchange.com
 - Bugfix #10377: Deleting whole recurring appointment if all of its
   occurrences are marked as a delete exception
 - Bugfix #10748: Wrote ReminderDeleteInterface implementation for calendar
   module
* Wed Jul 16 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9950: Using modern event handling (OSGi EventAdmin) to send mails to both old and new participants.
 - Bugfix #11655: Fixed counting of weekenddays in monthly recurrences.
 - Bugfix #11521: When removing the last file switch the mimetype to none.
* Tue Jul 15 2008 - choeger@open-xchange.com
 - Bugfix #11642 RHEL5 Packages don't depend on Sun Java 1.5 and mysql-server
 Packages
* Mon Jul 14 2008 - thorben.betten@open-xchange.com
 - Bugfix #11623: Fixed renaming of folders on root level
 - Bugfix #11622: Fixed fetch of pre-sorted messages since fetch responses
   need not to be in the same order as requested sequence numbers
 - Bugfix #11607: Removing pretty-printer's formatting on html2text
   conversion
* Mon Jul 14 2008 - marcus.klein@open-xchange.com
 - Bugfix #11619: Fixed code problem if on updating task an external participant
   is added.
* Sat Jul 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #11617: Checking mail references prior to putting them into#
   message cache
* Fri Jul 11 2008 - thorben.betten@open-xchange.com
 - Bugfix #10663: Fixed calculating number of occurrences in a monthly
   recurring appointment
 - Bugfix #8516: Regarding time zone offset when calculating occurrences
 - Bugfix #9823: Fixed calculating occurrences of multi-day full-time
   appointment
* Fri Jul 11 2008 - marcus.klein@open-xchange.com
 - Bugfix #11606: Not removing modules from availableModules if they do not
   contain a module subvalue.
* Thu Jul 10 2008 - thorben.betten@open-xchange.com
 - Bugfix #11585: Removed warning in UDP push on appointment deletion
 - Bugfix #9930: Fixed calculation of duration of a single item in a
   recurring appointment
 - Bugfix #10113: Fixed update of a recurring appointment with an until
   date to an occurrence setting.
* Thu Jul 10 2008 - francisco.laguna@open-xchange.com
 - Partial fix for bug #11569: Fixed search with "%" in infostore module
 - Fix for bug #11597: Changed type of field07 to TEXT.
* Thu Jul 10 2008 - marcus.klein@open-xchange.com
 - Bugfix #11580: Made the fields estimated and actual duration optional in
   task WebDAV/XML writer.
 - Bugfix #10747: Implemented task last modified timestamp update for reminder.
 - Bugfix #11569: Fixed search with "%" in task module.
* Thu Jul 10 2008 - choeger@open-xchange.com
 - Bugfix ID#11596 Installation fails on SLES10 64Bit
* Wed Jul 09 2008 - francisco.laguna@open-xchange.com
 - Bugfix #10806: Skipping calculation of future appointments recurrences for conflicts in a series with resources.
 - UNDOING Bugfix #6927: After discussing this with PM, this was undone, to prevent some destructive behaviour with D&D.
 - Bugfix #10497: Corrected calculcation for nths workday in monthly series.
 - Partial Bugfix #11579: checked and corrected InfostoreWriter, checked AttachmentWriter.
* Wed Jul 09 2008 - thorben.betten@open-xchange.com
 - Partial bugfix #11579: Conditional writing to avoid writing default
   values of object fields that return primitive types.
* Wed Jul 09 2008 - marcus.klein@open-xchange.com
 - Bugfix #11318: Added detail exceptions if JSON parsing of a long fails.
 - Bugfix #9586: Fixed with fix for bug #11318.
 - Bugfix #9677: Default log level was changed to INFO.
 - Bugfix #9862: Provided a time zone for calculating recurrences.
 - Bugfix #10048: Increased size of task titles.
* Tue Jul 08 2008 - marcus.klein@open-xchange.com
 - Bugfix #11561: Added check if shared folder may be empty.
* Tue Jul 08 2008 - thorben.betten@open-xchange.com
 - Bugfix #9749: Setting image's content type according to "TYPE" parameter
   in VCard object
 - Partial fix for bug #11569: Fixed search with "%" in calendar module
 - Partial fix for bug #11569: Fixed search with "%" in contact module
 - Bugfix #11573: Proper parsing of field "imapServer" in user table
* Mon Jul 07 2008 - thorben.betten@open-xchange.com
 - Partially fixed bug #11474: Proper birthday when syncing contacts
* Fri Jul 04 2008 - thorben.betten@open-xchange.com
 - Preparations for Bugfix #11554: Proper logging of unexpected exceptions
 - Bugfix #11554:  Changed conversion of MailMessage objects to JavaMail
   Message objects
 - Bugfix #10949: Allowing multiple external participants whose email
   address' hash code is equal through re-computing their identifier
* Thu Jul 03 2008 - francisco.laguna@open-xchange.com
 - Bugfix #6927: Allow changing start_date of appointment series.
* Thu Jul 03 2008 - marcus.klein@open-xchange.com
 - Bugfix #11558: Fixed problem arised through Response object refactoring.
* Thu Jul 03 2008 - thorben.betten@open-xchange.com
 - Bugfix #11499: Fixed search for appointments in a shared folder
* Wed Jul 02 2008 - marcus.klein@open-xchange.com
 - Bugfix #11016: Renamed xml attribute for deleted groups and resources.
   Additionally sending old values for compatibility. This will be removed after
   some time.
* Wed Jul 02 2008 - thorben.betten@open-xchange.com
 - Bugfix #11528: Denying editing of system contact's primary email address
* Tue Jul 01 2008 - thorben.betten@open-xchange.com
 - Bugfix #10803: Immediate update of links on object modification (move,
   deletion, etc.)
 - Bugfix #11538: Added possibility to define login/password for
   authenticating connect to JMX agent
 - Bugfix #9746: More tolerant parsing of date/time values by allowing
   escaped colons
 - Bugfix #9768: Properly setting private flag if VCard's "CLASS" property
   is set to "CONFIDENTIAL" or "PRIVATE"
* Mon Jun 30 2008 - choeger@open-xchange.com
 - Bugfix #11527: packages providing the same functionality should conflict
   added conflicts for authentication and spamhandler bundles
* Mon Jun 30 2008 - thorben.betten@open-xchange.com
 - Bugfix #9987: Remembering a timed-out IMAP server as being temporary
   down on a failed connect attempt for a configurable amount of time and
   denying every request to affected IMAP for that time range.
 - Bugfix #9964: Applied support for different mail quota resources to JSON
   interface
 - Bugfix #10649: Added new property to limit number of concurrent sessions
   per user.
* Mon Jun 30 2008 - marcus.klein@open-xchange.com
 - Bugfix #10743: This is a special case of problem described in bug 11250 and
   it is fixed with fix for bug 11250.
 - Bugfix #11524: Polling java.util.concurrent.DelayQueue without timeout to
   workaround a bug in this class not fixed in currently IBM Java 5.0.7.
 - Bugfix #11423: Verified that all cache puts for contexts are located inside
   a lock.
* Mon Jun 30 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11534: mailfilter: ox set vacation rule generates sieve error
* Fri Jun 27 2008 - marcus.klein@open-xchange.com
 - Bugfix #7475: This bug has been fixed with the fix for bug #4778.
* Fri Jun 27 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11519: sieve filter could not be saved
* Fri Jun 27 2008 - thorben.betten@open-xchange.com
 - Bugfix #11515: Properly detecting null value as user's mail login and
   throwing an appropriate exception
* Thu Jun 26 2008 - marcus.klein@open-xchange.com
 - Bugfix #9774: Removed setting task series until date to MAX_VALUE. This may
   break recurrence calculation.
 - Bugfix #10222: Marked configjump.properties as configuration file to prevent
   overwriting an edited file.
 - Bugfix #11311: Writing long values as strings in JSON. This fixes problem with
   big long values.
 - Bugfix #11300: Added missing activator for generic ConfigJump.
* Thu Jun 26 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11298: Ask OXFolderAccess only if a folder is public / private. Ignore share state.
 - Bugfix #11237: Corrected control flow if management service is unavailable.
 - Bugfix #11465: Fallback to external participants eMail adress when the display name is not set.
 - Bugfix #11187 and #11467: Accept both an array of objects that must be deleted and a single object.
* Thu Jun 26 2008 - thorben.betten@open-xchange.com
 - Bugfix #11221: Checking array size prior to composing a SQL "IN (xxx)"
   string with StringCollection utility class which returns null if array
   parameter is empty.
 - Bugfix #11180: Deleting appointment reminders on user deletion
* Thu Jun 26 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11051: Ask OXFolderAccess only if a folder is public / private. Ignore share state.
* Wed Jun 25 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11307: Skip recurrence calculation when changing a series into a single appointment.
 - Bugfix #11333: Having a private calendar folder where the user could only see his own objects led to a SQL Error.
 - Bugfix #11349: If a conflicting appointment is in a shared folder, which is readable, provide the title in the
                   conflict.
 - Bugfix #4778 (sic!): Supply title in GUIs freebusy query.
* Wed Jun 25 2008 - marcus.klein@open-xchange.com
 - Bugfix #11195: Detecting duplicate task folder for user now correct when
   moving task.
* Tue Jun 24 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11399: Change delete and detach calls in inforstore to match the HTTP-API.
 - Bugfix #11424: Autoaccept for shared folder owner when an appointment is modified.
 - Bugfix #10154: Copy old participants when an appointment is modified in a shared folder.
 - Bigfix #11059: Check for read permissions when loading modified/deleted appointments.
* Tue Jun 24 2008 - marcus.klein@open-xchange.com
 - Bugfix #11403: Improved hashCode() and equals() method of participants.
* Tue Jun 24 2008 - thorben.betten@open-xchange.com
 - Bugfix #11481: Properly encapsulating pre-processor statements in
   comment and using single quote for quoting attribute value if attribute
   value contains quote character(s).
* Tue Jun 24 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11494: Can't create a vacation notice
* Mon Jun 23 2008 - thorben.betten@open-xchange.com
 - Bugfix #11104: Fixed mapping of exported contacts in German Outlook CSV
   file
 - Bugfix #10963: Always checking image size against property
   "max_image_size" (not only if "scale_images" is set to true)
 - Bugfix #11328: Also copying attachments and links on contact copy
* Mon Jun 23 2008 - dennis.sieben@open-xchange.com
 - Bugfix #11448: Wrong folder names in sieve scripts
* Mon Jun 23 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11316: Don't remove other participants.
* Fri Jun 20 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11305: [L3] - Deleting a user not possible
   The infostore batches deletes of more than 1000 documents to keep the statement size manageable.
* Fri Jun 20 2008 - marcus.klein@open-xchange.com
 - Bugfix #11397: Removed identifier from external participants in JSON.
 - Bugfix #11463: Improved exception message if the body for a search is missing.
 - Bugfix #11443: Invalidating cached contexts after database update.
* Fri Jun 20 2008 - thorben.betten@open-xchange.com
 - Bugfix #11909: Setting proper category TRUNCATED on a SQL data
   truncation error
 - Bugfix #11257: Fixed computing duration of a recurring appointment
   without altering Calendar object of "DTSTART" field
 - Bugfix #10951: Supporting multiple comma-separated parameter values in
   older VCard object
* Wed Jun 18 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #11250: [L3] List of day and month view is not sorted 
   correctly if there are serial appointments
* Tue Jun 17 2008 - ben.pahne@open-xchange.com
 - Bugfix #11274: Some contacts were not deletable 
 - Bugfix #11371: Not accessable links and attachments deleted
* Tue Jun 17 2008 - thorben.betten@open-xchange.com
 - Bugfix #10551: Extended calendar fields by missing constants used in
   error messages in importer-exporter module
* Fri Jun 13 2008 - thorben.betten@open-xchange.com
 - Bugfix #11417: Added position information to printf-formatted error
   messages
* Tue Jun 10 2008 - thorben.betten@open-xchange.com
 - Bugfix #11367: Sorting virtual owner folders appearing below
   "Shared Folder"
* Mon Jun 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #11352: Properly setting an attached VCard's disposition to
   'attachment'
* Fri Jun 06 2008 - thorben.betten@open-xchange.com
 - Bugfix #11156: Fixed SQL injection vulnerability through contact search
* Fri Jun 06 2008 - marcus.klein@open-xchange.com
 - Bugfix #11357: Sending lastModified and creationTime in contacts with correct
   timezone.
* Thu Jun 05 2008 - thorben.betten@open-xchange.com
 - Bugfix #11346: Fixed adding of direct links which got messed by tidy's
   pretty printer
* Thu Jun 05 2008 - marcus.klein@open-xchange.com
 - Bugfix #11348: Extended charset provider may be null.
* Thu Jun 05 2008 - choeger@open-xchange.com
 - Bugfix ID#11347: rpm packages for SLES10 and RHEL5 have broken dependencies
* Mon Jun 02 2008 - ben.pahne@open-xchange.com
 - Bugfix #9842: In global addressbook moved contacts not deleteable
* Mon Jun 02 2008 - marcus.klein@open-xchange.com
 - Bugfix #11324: Fixed special SettingStorage for administration daemon.
* Fri May 30 2008 - marcus.klein@open-xchange.com
 - Bugfix #11325: Preferences tree must be initialized for admin daemon.
* Wed May 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #11271: Proper handling when deleting a group to reassign
   affected group permissions to context's admin rather than to special
   group "all-groups-and-users"
* Tue May 27 2008 - thorben.betten@open-xchange.com
 - Bugfix #11299: Fixed downgrade actions on folder data
* Tue May 27 2008 - ben.pahne@open-xchange.com
 - Bugfix #10899: Changed contact sql column sizes with new updatetask
 - Bugfix #9282: Changed contact sql column sizes with new updatetask
* Wed May 21 2008 - thorben.betten@open-xchange.com
 - Bugfix #11292: Secure SMTP works with gmail
* Fri May 16 2008 - thorben.betten@open-xchange.com
 - Bugfix #11270: Reliable check for subscribed subfolders through a LSUB
   command instead of checking folder's attributes
* Thu May 08 2008 - thorben.betten@open-xchange.com
 - Bugfix #11256: Supporting non-ascii characters inside a parameterized
   header (Content-Type, Content-Disposition, etc.), although non-ascii,
   although non-ascii characters are not allowed as per RFC 2047.
* Thu May 08 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11269: When the current user is the only participant, conflicts must contain her.
* Wed May 07 2008 - thorben.betten@open-xchange.com
 - Bugfix #11244: Added logging of corresponding AJP forward request on an
   unexpected empty body request
* Mon May 05 2008 - marcus.klein@open-xchange.com
 - Bugfix #11242: Not throwing an exception if a read only preferences item is
   written.
* Mon May 05 2008 - francisco.laguna@open-xchange.com
 - Node 1077: Conflicts contains conflicting members
              Conflicts contain title if user has read access
* Wed Apr 30 2008 - thorben.betten@open-xchange.com
 - Bugfix #11235: Fixed possible StackOverflowError when parsing large HTML
   links or URLs occurring in a message content
 - Bugfix #11158: Fixed sorting messages by unread/read
* Tue Apr 29 2008 - choeger@open-xchange.com
 - Bugfix #11147: "Last modified from" should not be 0
   added new UpdateTask ContactsChangedFromUpdateTask
* Fri Apr 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #11206: Fixed creation of mail folder below a folder containing
   umlauts in its name
 - Bugfix #11175: Fixed encoding, quoting and escaping mailbox names
   according to RFC2060
* Thu Apr 24 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11202: Mimetypes default to application/octet-stream
* Wed Apr 23 2008 - thorben.betten@open-xchange.com
 - Bugfix #11175: Proper checking for invalid folder name on folder
   creation/rename
 - Bugfix #11193: Proper display of vcard-only messages
* Wed Apr 23 2008 - francisco.laguna@open-xchange.com
 - Bugfix #11148:
    Check recurrence pattern for validity on update.
    Recalculate entire recurrence pattern on update (if any recurrence data was changed).
* Tue Apr 22 2008 - thorben.betten@open-xchange.com
 - Bugfix #11169: Added support for strange cookie header which are not
   conform to RFC 2616
* Tue Apr 22 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #10825: [L3] Unclear object not found exceptions
* Tue Apr 22 2008 - thorben.betten@open-xchange.com
 - Bugfix #11139: The propagate method uses given connection if admin daemon.
* Mon Apr 21 2008 - marcus.klein@open-xchange.com
 - Bugfix #11173: Initialize folder component if server is in admin mode.
 - Bugfix #11174: Expect an OXFolder to be already deleted.
 - Bugfix #11176: Do not inform a removed task from the deleted table.
* Mon Apr 21 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #10881: HTTP API incompatibility: 
                  appointment request missing field ignore
* Tue Apr 15 2008 - thorben.betten@open-xchange.com
 - Bugfix #11142: Fixed NPE in cache bundle
* Tue Apr 15 2008 - francisco.laguna@open-xchange.com
 - Delete/Modify calendar data on user downgrade.
 - Delete/Modify infostore data on user downgrade.
* Fri Apr 11 2008 - thorben.betten@open-xchange.com
 - Bugfix #11138: Fixed mail folder creation
* Wed Apr 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #11105: Fixed non-tree-visible folders query
 - Bugfix 9914: Additional check if upload form's file name is encoded
* Mon Apr 07 2008 - francisco.laguna@open-xchange.com
 - Catch NumberFormatExceptions in Attachment Servlet, when a request does not include a properly formatted number
   (Bug #11074)
* Tue Apr 01 2008 - francisco.laguna@open-xchange.com
 - Consistency Tool migrated to JMX (Bug #11067)
 - Faster listing of all files in a LocalFileStorage (Bug #10079)
* Mon Mar 31 2008 - thorben.betten@open-xchange.com
 - Bugfix #11092: Fixed creation of mail folders
 - Bugfix #11088: Fixed sending read acknowledgment for unread messages
 - Bugfix #11096: Fixed display of nested messages' attachments
 - Bugfix #9759: Enforced repaint of shared/public folder on user/group
   deletion
* Thu Mar 20 2008 - thorben.betten@open-xchange.com
 - Bugfix #10969: Removed usages of com.openexchange.cache.CacheKey to
   avoid ClassCastException in a distributed setup
 - Bugfix #10886: Fixed NPE when searching with an empty pattern
* Thu Mar 20 2008 - marcus.klein@open-xchange.com
 - Bugfix #9447: This problem was fixed with fix for bug #10400.
* Mon Mar 17 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #10974: Clean up configuration files
* Mon Mar 17 2008 - marcus.klein@open-xchange.com
 - Bugfix #11064: Fixed a wrong constructor call.
 - Bugfix #11075: Fixed wrong build of sql search command if user has permission
   to read only own objects in a task folder.
 - Bugfix #9452: Omitting writing empty data object.
* Thu Mar 13 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #9871: [CONFIG] sessionContainerTimeout is not used
   The config parameter com.openexchange.session.sessionContainerTimeout
   is now used in the sessiond implementation
 - Bugfix #10372: end date of recurring appointment is wrong in 
                  search
 - Bugfix #10925: Context is missing in WebDAV/XML AppointmentWriter
* Thu Mar 13 2008 - thorben.betten@open-xchange.com
 - Partial bugfix #11044: Checking returned rfc822 data's/body's input
   stream for null reference prior to loading message's headers
* Wed Mar 12 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #11012: [L3] WebDAV interface doesn't send Free/Busy 
                       times as UTC
* Wed Mar 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #11027: Equal folder name response regardless of request method
* Tue Mar 11 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #10124: API does not return deleted items properly
 - Bugfix #10991: Unable to create tasks via WebDAV/XML if task 
                  with attachment already exists in the folder
* Thu Mar 06 2008 - thorben.betten@open-xchange.com
 - Bugfix #10976: No adding of user time zone's offset to a message's sent
   date
* Thu Feb 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #10979: Appropriate warn message on interrupted AJP listener on
   bundle stop
* Thu Feb 28 2008 - thorben.betten@open-xchange.com
 - Bugfix #10980: Performing a deeper connectivity check when mail/folder
   storage
* Tue Feb 26 2008 - francisco.laguna@open-xchange.com
 -  Bugfix #10962. Don't remove original if the copy is removed.
* Tue Feb 26 2008 - francisco.laguna@open-xchange.com
 - Bugfix #10968: Fix Infostore Search: Order of requested fields shouldn't matter.
* Mon Feb 25 2008 - thorben.betten@open-xchange.com
 - Bugfix #9910: Added mechanism to ensure folder data consistency after a
   user/group deletion operation
* Wed Feb 20 2008 - martin.kauss@open-xchange.com
 - Fixed bug #10717. If a participant was added to an appointment no
   notification mail was sent to this participant because the event
   object only contained only the original participants. This has been
   fixed.
* Fri Feb 15 2008 - martin.kauss@open-xchange.com
 - Fixed bug #10077. Deleting an existing exception by providing the
   exception date in the deleted_exception field does not delete the
   exception. This has been fixed.
* Thu Feb 14 2008 - marcus.klein@open-xchange.com
 - Bugfix #10400: Fixed bad handling of folder mapping when adding/removing
   participants.
 - Bugfix #9173: Removing .lock file in filestore if it is older than 100 times
   of timeout. This prevents stale .lock files.
* Thu Feb 14 2008 - martin.kauss@open-xchange.com
 - Fixed bug #10154,  If a user changes an appointment in a shared folder
   all participants except the owner of the shared folder are removed.
   This has been fixed.
 - Fixed the exception handling shown in bug #7141. An unexpected exception
   is shown instead of an object not found exception.
* Wed Feb 13 2008 - marcus.klein@open-xchange.com
 - Bugfix #9999: Changed loglevel of permission exceptions for tasks to level
   info.
* Tue Feb 12 2008 - thorben.betten@open-xchange.com
 - Bugfix #10926: No HTML validation on send with an empty HTML body
 - Bugfix #10924: No reference to possibly unknown class
   "sun.net.ConnectionResetException". Lookup by class name instead.
   Moreover affected routine is made safer to ensure a thrown messaging
   error finds its way to GUI.
* Tue Feb 12 2008 - marcus.klein@open-xchange.com
 - Bugfix #10237: Checking for not getable mail settings for some user.
* Mon Feb 11 2008 - martin.kauss@open-xchange.com
 - Fixed bug #10865. The time of the created exception was set to the
   recurring start/end time instead of the exception start/end time.
   Furthermore, the participant was only removed from the users list
   and not from the participant list. This has been fixed.
 - Fixed bug #10836. A user was able to setup a list request to see any
   object in any private folder. This has been fixed.
* Thu Feb 07 2008 - thorben.betten@open-xchange.com
 - Bugfix #10890: No copy into 'Sent' folder when sending raw message data
* Wed Feb 06 2008 - francisco.laguna@open-xchange.com
 - Attach iCal file to invitation emails to external participants.
* Wed Feb 06 2008 - thorben.betten@open-xchange.com
 - Bugfix #10902: Performing IMAP operation on a large number of messages
   in blocks to avoid the risk of an IMAP timeout
* Tue Feb 05 2008 - marcus.klein@open-xchange.com
 - Bugfix #10887: Removed context object from session interface to be able to
   update the server bundle without complete restart.
* Mon Feb 04 2008 - thorben.betten@open-xchange.com
 - Bugfix #10886: Fixed NPE when searching mails
* Fri Feb 01 2008 - thorben.betten@open-xchange.com
 - Bugfix #10880: Although version information should ensure that an update
   task only runs once, table existence is checked prior to its creation in
   spell check's update task
 - Bugfix #10890: Properly connecting mail connection object prior to
   sending notification
 - Bugfix #10893: Setting '\Seen' flag when copying a mail message into
   'Sent' folder
* Tue Jan 29 2008 - marcus.klein@open-xchange.com
 - Bugfix #10767: Locking for thread safety of cached object improved to prevent
   performance issues if load method for cached object is slow.
* Fri Jan 18 2008 - thorben.betten@open-xchange.com
 - Bugfix #9111: Throwing an error if user tries to share a private folder
   whose name is equal to another shared folder of the same user.
* Wed Jan 16 2008 - marcus.klein@open-xchange.com
 - Bugfix #8411: Fixed completely missing recurrence rule in WebDAV iCal
   interface. 
* Tue Jan 15 2008 - thorben.betten@open-xchange.com
 - Preparations for bugfix #9111: Check for duplicate names when sharing a
   folder
* Tue Jan 15 2008 - sebastian.kauss@open-xchange.com
 - Bugfix: #10760: Missing recurrence attributes in appointment requests
* Fri Jan 11 2008 - thorben.betten@open-xchange.com
 - Bugfix #10691: Increased IMAP timeout settings for both initial socket
   connect and socket I/O
 - Bugfix #10739: Added connect timeout and timeout for blocking operations
   to socket when detecting IMAP server
* Fri Jan 11 2008 - sebastian.kauss@open-xchange.com
 - Bugfix #6960: Missing recurrence_id when deleting a 
   synchronized exception
 - Bugfix: #9734: add number_of_attachments and number_of_links to 
   json array (calendar, contacts, tasks)
 - Bugfix: #9742: Whole-day appointment series with two participants in
   different timezones not shown in dayview
* Thu Jan 10 2008 - thorben.betten@open-xchange.com
 - Bugfix #10668: Decoding for newer mail-safe encoding as per RFC2231
 - Partial bugfix #10686: Overwriting JSESSIONID cookie if its ID refers to
   a non-existent or invalid HTTP session.
* Wed Jan 09 2008 - marcus.klein@open-xchange.com
 - Bugfix #10688: Schema should be unlocked if a SQLException occurs during
   locking.
* Wed Jan 09 2008 - francisco.laguna@open-xchange.com
 - Bug #10706: Don't lose the filename in an update to an infoitem via webdav.
* Wed Jan 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #10214: Using "Java port of Mozilla charset detector" to guess
   proper charset for uploaded files
* Tue Jan 08 2008 - francisco.laguna@open-xchange.com
 - Fix Exception handling in AjaxServlet#service. Wrap everything except ServletException in
   a ServletException (many thanks to Thorben).
 - Fixes for bugs #9109, #10051, #10044, #10052
* Mon Jan 07 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9064: [Update] Added update task to fix folder name collisions below any parent folder.
 - Bugfix #10403: Log exceptions based on the exceptions category.
 - Bugfix #9695: [Update] Allow longer URLs in Infostore.
* Thu Jan 03 2008 - francisco.laguna@open-xchange.com
 - Bugfix #10395: Don't recurse for webdav listing if permissions don't allow it.
* Wed Jan 02 2008 - francisco.laguna@open-xchange.com
 - Bugfix #9837: Allow forward slashes as part of WebDAV URLs
     [Conf] : add "AllowEncodedSlashes On" in apache2 configuration.
############### CONFIG CHANGE ##################
#                                              #
#   For this to work, this must be added to    #
#   the apache config:                         #
#                                              #
#   AllowEncodedSlashes On                     #
#                                              #
#   Otherwise the forward slashes are not      #
#   passed to our servlet container.           #
#                                              #
################################################
 - Bugfix #8676 : Requests for LockNullResources are supposed to return
   404 on most requests.
 - Bugfix #9903: Applied patch.
 - Bugfix #9904: Applied patch.
* Wed Dec 12 2007 - thorben.betten@open-xchange.com
 - Bugfix #10608: Allowing email addresses with pipe character "|" in
   personal part
* Mon Dec 10 2007 - thorben.betten@open-xchange.com
 - Bugfix #10393: MBox support
* Fri Dec 07 2007 - marcus.klein@open-xchange.com
 - Bugfix 10559: Checking the defaultSendAddress if a valid value is written.
* Wed Dec 05 2007 - thorben.betten@open-xchange.com
 - Bugfix #10503: Sending W3C conform html
 - Bugfix #10526: Text-only drafts are now kept as text-only and got no
   more converted to html
* Tue Dec 04 2007 - marcus.klein@open-xchange.com
 - Bugfix #10524: Removed a wrong logging.
* Thu Nov 29 2007 - thorben.betten@open-xchange.com
 - Bugfix #10460: AJP exception enhanced by a keep alive flag to indicate
   whether to close or keep established AJP connection. Thus any exception
   related to a broken socket has its flag set to false -> close connection.
* Tue Nov 27 2007 - marcus.klein@open-xchange.com
 - Bugfix #10276: Excluded in the end date range query the end date.
* Tue Nov 27 2007 - thorben.betten@open-xchange.com
 - Bugfix #9963: Avoiding infinite loops of broken pipe errors on a lost
   socket connection
* Wed Nov 21 2007 - thorben.betten@open-xchange.com
 - Bugfix #10033: Fixed 'edit-draft'
 - Bugfix #10100: Fixed path request on virtual shared folders
* Tue Nov 20 2007 - thorben.betten@open-xchange.com
 - Bugfix #10201: No second duplicate of a sent mail is copied to default
   sent folder
* Fri Nov 16 2007 - thorben.betten@open-xchange.com
 - Bugfix #10234: Using proper SQL to locate duplicate folders on update
* Wed Nov 14 2007 - thorben.betten@open-xchange.com
 - Bugfix #10167: Fixed imap-based sort
* Tue Nov 13 2007 - marcus.klein@open-xchange.com
 - Bugfix #10117: Fixed moving problems.
* Fri Nov 09 2007 - thorben.betten@open-xchange.com
 - Bugfix #10117: Checking for duplicates on folder creation/update/rename
 - Bugfix #10010: Decoding file attachment's file name when saving to
   infostore
* Thu Nov 08 2007 - thorben.betten@open-xchange.com
 - Bugfix #10053: Keeping original file data when attaching to mail
 - Bugfix #10121: Removed vulnerability for DOS attacks in AJP through
   keeping AJP connection alive on every exception
* Wed Nov 07 2007 - thorben.betten@open-xchange.com
 - Bugfix #9966: Proper error message on exceeded quota on mail server
* Wed Nov 07 2007 - manuel.kraft@open-xchange.com
  - Bugfix ID#10050 Database leftovers of deleted contexts
* Tue Nov 06 2007 - dennis.sieben@open-xchange.com
 - Bugfix #8919: [L3] 'checkconsistency' does not provide any usage information and
   does not work as well
* Wed Oct 31 2007 - ben.pahne@open-xchange.com
 - Bugfix #9996: Fixed replacing if special characters when converting
   plain text to html
 - Bugfix #9998: Catching runtime exception when converting TNEF read
   receipt or TNEF contact to common multipart object
* Mon Oct 29 2007 - thorben.betten@open-xchange.com
 - Bugfix #9957: Fixed error message on exceeded quota when placing a copy
   into sent folder during message transport
 - Looking for QUOTA resource 'STORAGE' and logging other unsupported QUOTA
   resources
 - Updating message cache when selected mail is marked as seen
* Mon Oct 29 2007 - marcus.klein@open-xchange.com
 - Bugfix #9869: Set default upload quota to infinity.
 - Bugfix #9826: Properly set charset in Content-Type for configuration jump.
* Mon Oct 29 2007 - ben.pahne@open-xchange.com
 - Fixed Bug 9975: Some connections didn't get closed during an error 
* Fri Oct 26 2007 - thorben.betten@open-xchange.com
 - Bugfix #9980: Proper check for table 'version'
 - Allowing an alternative way of specifying HTML inline images
 - Bugfix #9981: Checking for empty status message in HTTP response
* Thu Oct 25 2007 - thorben.betten@open-xchange.com
 - Bugfix #9899: Reconstructing item handlers if order of fetch items
   changes during processing of FETCH response
 - Bugfix #9939: Just sending END_RESPONSE package on ServletException to
   keep the socket alive
* Wed Oct 24 2007 - marcus.klein@open-xchange.com
 - Bugfix #9807: Removed throw of ServletException on a not catched Exception.
* Wed Oct 24 2007 - thorben.betten@open-xchange.com
 - Mail transport's dataobjects made more abstract
* Tue Oct 23 2007 - francisco.laguna@open-xchange.com
 - Bugfix #9865: Differentiate resources and users by trying to load them.
* Tue Oct 23 2007 - thorben.betten@open-xchange.com
 - Bugfix #9914: Providing a charset in new String() constructor
* Mon Oct 22 2007 - thorben.betten@open-xchange.com
 - Bugfix #9820: Fixed folder move operation if IMAP server does not
   support ACLs
 - Bugfix #9794: Moving folder (incl. its subfolder tree) to trash folder
   on folder deletion
 - Loading namespace folder only one time per user session
 - Bugfix #9852: Quoting replacement argument prior to invoking
   String.replaceFirst()
 - Bugfix #9922: Fetching ACL list in a safe manner when inserting or
   updating an IMAP folder. All ACL related actions are suppressed if ACLs
   cannot be obtained which is mostly the case in a missing ADMINISTER
   right due to a newer ACL extension.
* Mon Oct 22 2007 - francisco.laguna@open-xchange.com
 - Bugfix #9865: Less valiant logging for Notifications.
* Fri Oct 19 2007 - marcus.klein@open-xchange.com
 - Bugfix #9764: This issue is fixed with the fix for bug 9807.
* Thu Oct 18 2007 - marcus.klein@open-xchange.com
 - Bugfix #9800: Remaining entries inside del table after context delete
 - Bugfix #9581: Delete of all user contacts during the delete user process
   was not working if an unbound contacts occured.
 - Bugfix #9807: Catching all exceptions now in AJAX super servlet to prevent
   closed AJP sockets.
 - Bugfix #9804: Added mapping for charset x-unknown to US-ASCII.
 - Bugfix #9822: Surrounded all TimerTasks with catch statement to prevent
   dying OXTimer if an exception occurs.
* Thu Oct 18 2007 - martin.kauss@open-xchange.com
 - Fixed bug #9772. One argument in an object not found exception was
   missing. This has been fixed.
* Wed Oct 17 2007 - martin.kauss@open-xchange.com
 - Fixed bug #9808. An update task failed because of a wrong where
   clause. This has been fixed.
* Tue Oct 16 2007 - sebastian.kauss@open-xchange.com
 - Bugfix #9219: Reminder popups: Appointment reminder has wrong text
 - Bugfix #9416: Calendar: Concurrency issue with Reminder
                 confirmations - Bad error message
 - Bugfix #9492: Calendar: Reminder appears again and again
 - Bugfix #9514: Calendar: Reminder for recurring appointments shown
                 more than once for each occurence
* Mon Oct 15 2007 - martin.kauss@open-xchange.com
 - Fixed bug #6335. Some characters caused problems while building XML.
   Now we are throwing an error in the calendar if those characters are
   found.
* Fri Oct 12 2007 - marcus.klein@open-xchange.com
 - Bugfix #9790: Added delete event for contexts. Its listeners remove the file
   storage.
* Thu Oct 11 2007 - martin.kauss@open-xchange.com
 - Fixed bug #7471. If an existing exception is deleted with the
   update method and the field delete_exception then this exeption
   was just marked as deleted exception without a real delete.
   This has been fixed.
  - Removed unused imports
  - Fixed bug #9599. By adding a new users the confirm message was
    deleted for existing users. This has been fixed.
* Thu Oct 11 2007 - thorben.betten@open-xchange.com
 - Bugfix #9756: Fallback implementation if UID EXPUNGE is not supported by
   IMAP server
 - Bugfix #9787: Checking right table when looking for user's default
   infostore folder on context deletion
* Wed Oct 10 2007 - thorben.betten@open-xchange.com
 - Bugfix #9751: Fixed non-appearing shared mail folders
 - Bugfix #9750: Avoid annoying error logging whenever an ACL entity cannot
   be mapped to system user
 - Bugfix #9719: Returning proper instance of FolderObjectIterator
 - Bugfix #9733: More informative error message if no folder admin specified
* Mon Sep 17 2007 - marcus.klein@open-xchange.com
 - Bugfix #9475: Improved VCardTokenizer to use byte arrays instead of Byte
   lists.
* Mon Sep 17 2007 - martin.kauss@open-xchange.com
 - Fixed bug #9497. Recurring appointments with a start before 1970 caused a
   NullPointer by viewing the detail page of a single occurrence. This has been
   fixed.
* Mon Sep 17 2007 - thorben.betten@open-xchange.com
 - Bugfix #9664: Fixed check for IMAP server's user flag support
 - Bugfix #9699: Fixed update of IMAP folder's ACLs
* Fri Sep 14 2007 - ben.pahne@open-xchange.com
 - Fixed Bug #9466 - User was able to move his
   contact out of the global address book.
* Thu Sep 13 2007 - ben.pahne@open-xchange.com
 - Fixed Bug #6335 - Check for bad characters impletemented
* Thu Sep 13 2007 - marcus.klein@open-xchange.com
 - Bugfix #8935: Fixing once again another case breaking AJAX communication.
 - Bugfix #9445: Added handling for folders that are only visible.
* Wed Sep 12 2007 - francisco.laguna@open-xchange.com
 - Fix for Bug #6335, infostore. Strings are run against the validator.
 - Fix for Bug #6335, attachments. Strings are run against the validator.
 - Fix for Bug #9224: Allow import of empty contacts
 - Fix for Bug #9390: Calculate boundaries for webdav partial GET correctly
* Wed Sep 12 2007 - ben.pahne@open-xchange.com
 - Fixed Bug 9224: Display Name / File as not synchronized to Outlook
* Wed Sep 12 2007 - thorben.betten@open-xchange.com
 - Bugfix #9407: Using a thread-safe implementation of java.util.Map in
   IMAP connection watcher
* Wed Sep 12 2007 - marcus.klein@open-xchange.com
 - Bugfix #9409: Reimplemented bugfix for bug 8935 to prevent this problem.
* Tue Sep 11 2007 - marcus.klein@open-xchange.com
 - Bugfix #9341: Fixed returning null instead of throwing exception if context
   is not found.
 - Bugfix #9364: Using same mechanisms for modifying a search pattern for groups
   and resources.
 - Bugfix #8935: Removed replacing the string "null" with null.
 - Bugfix #9209: Illegal files for import now give all a message in the panel.
 - Bugfix #9384: Reloading task before updating last_modified to prevent
   exception on unsynchronized server times.
* Tue Sep 11 2007 - martin.kauss@open-xchange.com
 - Fixed bug #9137. It was not possible to move/edit an private appointment.
   This has been fixed.
 - Fixed bug #9191. Only the first reminder of a recurring appointment was
   shown after clicking on OK. This has been fixed.
* Mon Sep 10 2007 - francisco.lagunar@open-xchange.com
 - Bugfix #9256 : Don't write "null" in the subject line for notification
   mails if the title isn't set.
* Mon Sep 10 2007 - thorben.betten@open-xchange.com
 - Bugfix #9346: Changed mail component's code back to "MSG"
* Fri Sep 07 2007 - ben.pahne@open-xchange.com
 - Fixed bug #9255 - Typos in error message
 - Fixed bug #8456 - Changed an error message
* Fri Sep 07 2007 - thorben.betten@open-xchange.com
 - Bugfix #9229: Fixed parsing of html links starting with "news."
* Fri Sep 07 2007 - choeger@open-xchange.com
 - Bugfix #9235 Crypt implementation should be able to handle UTF-8
* Thu Sep 06 2007 - thorben.betten@open-xchange.com
 - Bugfix #6335: Checking for invalid characters in folders.
 - Bugfix #9299: Added fast IMAP fetch to avoid high time latencies when
   listing messages in large mail folders
############## PAY ATTENTION TO ME ! ################
 - CONFIG change: new property in file "imap.properties"
   called "imapFastFetch" which enabled/disables usage
   of fast fetch
#####################################################
* Thu Sep 06 2007 - martin.kauss@open-xchange.com
 - Fixed bugs #9205 and 9132
   9205: Move with participants from a public to a private folder failed 
   with an error message. This has been fixed.
   9132: If the original recurring appointment contains a resource an error
   occurred if an exception is created. This has been fixed.
* Wed Sep 05 2007 - marcus.klein@open-xchange.com
 - Bugfix #9252: Fixed checking access permission when getting a task.
 - Bugfix #6335: Checking for invalid characters in tasks.
 - Reopen #8699: Removed listening on localhost to get JMX working again.
* Tue Sep 04 2007 - francico.laguna@open-xchange.com
  - Fixed Bug #9204: Participant Notifications emails about tasks now sport
     the default DateFormat (not DateTimeFormat) for the users locale without
     timezone information.
  - Fixed Bug #9064: Added update task to fix name collision 
     in personal infostore folder names.
  - Fixed Bug #9112: Log filestore exception and provide simpler message
* Tue Sep 04 2007 - thorben.betten@open-xchange.com
 - Bugfix #8470: Applied provided patch
 - Bugfix #9231: Using Cyrus implementation as fallback for User2IMAP if no
   matching IMAP server implementation could be found and ACLs are not
   supported/turned off
* Mon Sep 03 2007 - ben.pahne@open-xchange.com
  - Fixed Bug 9154: Movings contacts not working
* Fri Aug 31 2007 - thorben.betten@open-xchange.com
 - Bugfix #7862/#9125: Adding library with additional charsets to Java VM
   to solve encoding problems with messages which uses java-foreign charset
   encodings
 - Bugfix #9169: Sorting messages by date fields (either sent date or
   received date) is no more mixed up with ascending/descending order
 - Bugfix #9161: Proper htm2text conversion
 - Bugfix #8699: JMX connector and its rmi data socket get now bound to the
   same address (if configured through server.properties' attribute
   "MonitorJMXBindAddress")
* Thu Aug 30 2007 - marcus.klein@open-xchange.com
 - Bugfix #9135: Fixed NullPointerException due to coding problem.
* Thu Aug 30 2007 - thorben.betten@open-xchange.com
 - Bugfix #7658: Fixed i18n call for read acknowledge text
 - Bugfix #9084: Fixed naming for folders
 - Bugfix #9163: Occuring blockquote tags in html content are no more
   colorized
* Thu Aug 30 2007 - francisco.laguna@open-xchange.com
 - Bugfix #9159: explicitely initializing infostore document metadat
   with filesize 0 for mail attachments.
* Thu Aug 30 2007 - marcus.klein@open-xchange.com
 - Bugfix #7276: Fixed handling for task loading to not miss permission checks.
* Tue Aug 28 2007 - marcus.klein@open-xchange.com
 - Bugfix #6502: Changed exception if a reminder is not found.
 - Removed deprecated methods from tasks.
* Tue Aug 28 2007 - ben.pahne@open-xchange.com
 - Fixed Bug #9110, Display name must be unique
 - Fixed Bug #8731, list request for a nonexistent object returns an
    empty array
 - Fixed Bug #9050, Moving private taged contacts into a public folder
* Tue Aug 28 2007 - francisco.laguna@open-xchange.com
 - Fixed Bug #9109, Support for DropBox scenario via WebDAV. A DropBox
   is a folder in which some user may only create objects
   (not read, update or delete). This works also via WebDAV Infostore now.
 - Contacts expect a display name so changed CSV Importer to supply one
   as needed.
* Mon Aug 27 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7732: Pass the error message from the Versit Converter on to
   the caller in the ImportResult.
 - Bugfix #7735: Support for RRULE last "whatever" of month "whatever"
* Fri Aug 24 2007 - martin.kauss@open-xchange.com
 - Fixed bug #9089. It was possible to set a private flag in a public folder.
   This has been fixed.
* Fri Aug 24 2007 - thorben.betten@open-xchange.com
 - Bugfix #6274: Using requested error message if IMAP login fails in any
   case
* Thu Aug 23 2007 - thorben.betten@open-xchange.com
 - Bugfix #8498: Checking for an empty content type when attaching
   (infostore) files to a (smtp) message
 - Bugfix #9069: Checking if destination folder is a subsequent folder on
   move operation
 - Bugfix #7928: User's Vcard is not attached twice anymore when sending a
   draft message
 - Bugfix #9059: Skipping leading quote character when comparing email
   addresses based on their personal part
 - Bugfix #7331: Additional check for an SMTP error's return code if init
   cause is an exceeded storage allocation
* Thu Aug 23 2007 - marcus.klein@open-xchange.com
 - Bugfix #8903: Added check for create permissions on destination folder if
   tasks are moved.
 - Bugfix #9013: Added the alarm attribute as possible list attribute.
 - Bugfix #9045: Fixed html page in TestServlet.
* Wed Aug 22 2007 - thorben.betten@open-xchange.com
 - Fixed checking for spam activation when checking default folders
* Tue Aug 21 2007 - francisco.laguna@open-xchange.com
 - Fixed bug #8971. User userId and not contactId for internal user participants 
   in ICAL import.
* Tue Aug 21 2007 - thorben.betten@open-xchange.com
 - Bugfix #8987: Fixed html2text conversion while keeping quotes
 - Bugfix #8988: Fixed NPE on non-matching servlet path
* Mon Aug 20 2007 - thorben.betten@open-xchange.com
 - Bugfix #8921: Fixed indexing of mail default folders dependent on spam
   activation
 - Bugfix #8937: Fixed typo by replacing all occurences of "instanciat"
   with "instantiat"
 - Fixed regex to detect uuencoded attachments inside a plaint text message
 - Fixed connection handling in:
   OXFolderAdminHelper.propagateUserModification()
* Mon Aug 20 2007 - martin.kauss@open-xchange.com
 - Fixed bug #8957. A search without a folder id returned -1 as folder instead
   of the real folder id. This has been fixed.
* Fri Aug 17 2007 - thorben.betten@open-xchange.com
 - More granular thread synchronization in folder and message caches
 - Added field "default_folder" to IMAP folders
 - Bugfix #8926: Avoiding setting IMAP's estimated file size when storing
   message attachments into infostore
* Fri Aug 17 2007 - marcus.klein@open-xchange.com
 - Bugfix #8934: Switched deleting user folder mapping and inserting admin
   folder mapping.
 - Bugfix #8936: Fixed SQL statement for deleting participants.
* Thu Aug 16 2007 - thorben.betten@open-xchange.com
 - Partial bugfix #8885: Avoiding translating IO errors to "instantiation
   failed" errors
 - Partial bugfix #8839: New method added to OXFolderAdminHelper class to
   propagate user modifications throughout folder module
 - Bugfix #8901: Sending correct update informations (action=updates) on
   public folder's permission modification
* Wed Aug 15 2007 - marcus.klein@open-xchange.com
 - Removed unused JNI SSL stuff.
 - Removed unused classes and methods.
* Wed Aug 15 2007 - thorben.betten@open-xchange.com
 - Special HTML entities moved to external properties file
 - Enhanced text2html conversion through including more HTML entities
 - Added check for duplicate infostore folder on user creation
 - Fixed usages of MailInterfaceMonitor.numActive
 - Removing unspecified initial ACLs on IMAP folder creation
 - Bugfix #8823: Allowing to look-up IMAP folder's ACLs if either READ or
   ADMINISTER right is granted to user
 - Bugfix #8900: Removed unnecessary informations from html error page
   template
############## PAY ATTENTION TO ME ! ################
 - CONFIG change: new property file "HTMLEntities.properties" which
   holds known HTML entities and their character mapping
#####################################################
* Wed Aug 15 2007 - tobias.prinz@open-xchange.com
 - Fixed bug in OldNPropertyDefinition causing NoSuchElementException.
   Preparation for bugfix #8844.
 - Bugfix #8844: Changed mailer to use v2.1 as format for attached
   VCards instead of v3.0, because Outlook 2003 has Problems with escaped
   commas.
* Wed Aug 15 2007 - ben.pahne@open-xchange.com
 - Bug Fix 8880: It was possible to move contacts into the global
   address book. Fixed this.
* Tue Aug 14 2007 - marcus.klein@open-xchange.com
 - Marked some methods for participants deprecated to prevent coding problems.
* Tue Aug 14 2007 - thorben.betten@open-xchange.com
 - More reliable sorting when requesting user's new messages
 - Removed some warnings from IDE
 - Fixed too early removal of temporary uploaded files if message transport
   fails
 - Removing message cache entries on delete
* Tue Aug 14 2007 - tobias.prinz@open-xchange.com
 - Bugfix #6825: Implemented new classes for translating fieldnames for
   Appointments/Calendar and Tasks. Now the users gets better hints what
   fields could not be imported.
* Tue Aug 14 2007 - martin.kauss@open-xchange.com
 - Fixed bug #8567. An exception of a recurring appointment could be moved
   into a different folder than the main object. This will now be avoided.
   In addition an exception can not have a different private state than the
   main object.
 - Fixed bug #8836. Updating a normal appointment and set the private flag
   failed. This has been fixed.
* Mon Aug 13 2007 - thorben.betten@open-xchange.com
 - Moved flag to store mail default folder have been checked to session due
   to new storage concept for UserSettingMail instances
 - Bugfix #8784: Mail folders with more messages than
   "imapMessageFetchLimit" defined in "imap.properties" can be sorted
   against "unread"
 - Using atomic datatypes from java.util.concurrent.atomic package for
   atomic access to monitoring variables instead of using own locking
   mechanism
 - MailWriter.getMailWriters() made static to reduce object instantiation
 - Additional methods for UserSettingMailStorage to remove single cached
   user's mail settings or whole cache
 - Bugfix #8767: Fixed new message display on portal side
 - Bugfix #8793: Starting html paragraph is skipped by html2text conversion
   to avoid double new line at the beginning
 - Reading html entities from resource:
   "com/sun/org/apache/xml/internal/serializer/HTMLEntities.properties"
* Mon Aug 13 2007 - tobias.prinz@open-xchange.com
 - Bugfix #8475: Opposed to the definition, UserParticipants are not 
   supposed to be identified by e-mail address, so I changed this in 
   OXContainerConverter.
* Mon Aug 13 2007 - martin.kauss@open-xchange.com
 - Bugfix #8505 and #8722. Reminder data still exist after an appointment was
   deleted. In addition, an update task exist to fix corrupt data in the 
   database from older version. INFO: A config file has been changed:
   updatetasks.cfg
 - Fixed bug #8741. If the first appointment of a sequence is an exception
   the GUI gets wrong start/end dates. This has been fixed.
* Mon Aug 13 2007 - sebastian.kauss@open-xchange.com
 - Fixed Bug #8785 - Only iCal imported fullday holiday appointments 
                     are shown on the start page. Added new ajax method 
                     newappointments for portal search.
 - Fixed Bug #8760 - Appointment conflicts were not in ajax response after
                     update request.
* Mon Aug 13 2007 - marcus.klein@open-xchange.com
 - Bugfix #8410: Removed duplicate write database connection.
* Fri Aug 10 2007 - thorben.betten@open-xchange.com
 - Forgot to delete temporary uploaded files manually after sending message
 - Bugfix #8701: Invoking own decoding method for MIME-encoded personal
   parts in internet addresses
 - Bugfix #8686: Closing first statement
 - Additional folder field for mail folder attribute "hasSubscribedSubfolders"
 - More reliable message attachment detection
 - Proper error codes in ParamContainer class
 - Removed unused methods from OCLPermission class and its subclasses
 - Ensure fully loaded folder object on delet or update
 - Bugfix #8743: Added message args to exception
* Fri Aug 10 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #8527: Versitparser now does not break if empty properties
   are in a VCard or ICal file.
* Thu Aug 09 2007 - francisco.laguna@open-xchange.com
 - Bugfix 8725: Remove depedency on user and userconfiguration for attachment and property cleander.
* Thu Aug 09 2007 - martin.kauss@open-xchange.com
 - Fixed some typos related to bug #7936.
* Thu Aug 09 2007 - thorben.betten@open-xchange.com
 - Bugfix 8726: Fixed detection of uuencoded body parts in a message
 - Enhanced file upload servlet by get request for uploaded files
 - New property in server.properties to define the max. idle time in millis
   for a temporary uploaded file
 - Enhanced mail by inline images referenced by locally stored files
############## PAY ATTENTION TO ME ! ################
 - CONFIG change: server.properties got an additonal
   property to define the max. idle time in millis
   for a temporary uploaded file
#####################################################
* Thu Aug 09 2007 - ben.pahne@open-xchange.com
- Fixed Bug 8728: Private Flag was not removed after a move into
  a public folder.
* Thu Aug 09 2007 - tobias.prinz@open-xchange.com
 - Bug 8653: Fixed counting of imports. Again.
 - Bug 8527: Fixed counting of imports.
* Thu Aug 09 2007 - marcus.klein@open-xchange.com
 - Added max idle timeout for uploads to configuration setting tree.
* Wed Aug 08 2007 - thorben.betten@open-xchange.com
 - Additional monitoring info in AJP monitor to count number of processed
   requests for throughput accounting purpose
 - Bugfix #8716: Fixed composing JSON response if multiple request element
   does not contain a timestamp value
* Wed Aug 08 2007 - tobias.prinz@open-xchange.com
 - Refactoring: JSONWriter replaced with OXJSONWriter for ImportServlet
 - Bug 8681: Checking for modules in Importers.
 - Bug 7936: Fixed typos in exception messages.
* Wed Aug 08 2007 - marcus.klein@open-xchange.com
 - Bugfix 8720: Added invalidation for context login information mappings.
 - Finished generic config jump.
* Tue Aug 07 2007 - francisco.laguna@open-xchange.com
 - Added license to classes.
 - Bugfix #8622: Provide overridability for url encoding in propfind responses.
* Tue Aug 07 2007 - thorben.betten@open-xchange.com
 - Fixed bug when requesting messages from several folders via PUT on mail
   servlet with action=list
 - Some changes to improve display of messages
 - Including inline images on reply/forward
* Mon Aug 06 2007 - thorben.betten@open-xchange.com
 - Fixed JSON error when deleting appointment through multiple servlet
 - Refactored UserConfiguration: UserSettingMail reference moved to
   SessionObject
 - UserSettingMailStorage class for typical storage operatinos (save, load
   & delete)
 - Added cache for user's mail settings (according to other existing storage
   classes)
 - Refactored UserSettingMail: SessionObject no more holds a reference to
   an instance of UserSettingMail rather than fetching a cached instance on
   SessionObject.getUserSettingMail() invokations.
 - Added error codes to DeletionFailedException (extends AbstractOXException)
* Mon Aug 06 2007 - martin.kauss@open-xchange.com
 - Documented calendar.properties options and parameters.   
 - Fixed bug #8510. Creating an recurring exception and setting a reminder
   for the exception exchanged the reminder from the recurring appointment
   and the exception. This has been fixed.
* Mon Aug 06 2007 - marcus.klein@open-xchange.com
 - Implemented generic config jump.
* Fri Aug 03 2007 - francisco.laguna@open-xchange.com
 - Fixed bug #8673. Added a new servletmapping to allow Vista Home Basic to connect
   on the first level. 
########### LOOK MA! NO HANDS! CONFIGCHANGE CONFIGCHANGE#############
in the servletmapping.properties we have a new entry:
infostore* Fri Aug 03 2007 - thorben.betten@open-xchange.com
 - Fixed forwarding of messages with inline images
 - Fastened message cache
 - Fixed: Invalidation of user configuration now affects session, too
* Thu Aug 02 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7936: Changed error messages for infostore and attachments. 
* Thu Aug 02 2007 - thorben.betten@open-xchange.com
 - Fixed multiple mail requests
* Thu Aug 02 2007 - marcus.klein@open-xchange.com
 - Bugfix #7936: Changed error messages for filestore, ajax and login.
 - Bugfix #7202: Fixed creating the next recurrence of tasks.
 - Bugfix #8301: Next Recurrence is only created if task state is changed to
   DONE.
 - Bugfix #7733: Changed error message for a task that has been changed by
   someone else.
 - Allowed creator to be participant of tasks.
* Thu Aug 02 2007 - sebastian.kauss@open-xchange.com
 - Fixed Bug #7393 - Folder 0 cannot be resolved
 - Fixed Bug #8618 - ICal import is possible if calendar module is disabled 
 - Fixed Bug #8635 - Use transaction to generate unique id for reminder
 - Fixed Bug #8545 - Reminder import use more database connections
 - Fixed Bug #7142 - Log permission exception as debug
* Thu Aug 02 2007 - martin.kauss@open-xchange.com
 - Fixed bug #6408. Reminders greater than 3 weeks did not work as expected
   because the calculation was done with an int instead of a long. Fixed.
* Wed Aug 01 2007 - thorben.betten@open-xchange.com
 - Changed folder's "getUpdatedFolders" query according to HTTP API spec to
   to query all folder GREATER THAN sent timestamp from GUI.
 - Enhanced com.openexchange.tools.stack.Stack interface to offer size()
   method
 - Fixed moving/copying of messages
 - Added module permission check in Multiple servlet's mail request
* Wed Aug 01 2007 - sebastian.kauss@open-xchange.com
 - Fixed Bug #8177 - Statement not closed if inserting reminder fails
 - Fixed Bug #6077 - Redesign of reminder exception handling 
 - Fixed Bug #6990 - Replace JSONException with OXJSONException
 - Fixed Bug #8182 - Deleted objects are not return in modified request
* Wed Aug 01 2007 - francisco.laguna@open-xchange.com
 - Fixed Bug #8630 - Check module permission for multiple module 
   (partial fix for infostore and attachments), note though, there is no
   module permission explicitely for attachments.
* Wed Aug 01 2007 - marcus.klein@open-xchange.com
 - Bugfix #8410: Changed SQL command for deleting task participants to not use
   if a value is within a set.
 - Bugfix #8253: Fixed NPE in checkConsistency tool.
* Tue Jul 31 2007 - thorben.betten@open-xchange.com
 - Multiple servlet changed to use JSON objects to create the multiple
   response insterad of using thousands of StringWriter instances
 - Changed SQL query to retrieve user's root folders to include user's
   configuration
* Tue Jul 31 2007 - francisco.laguna@open-xchange.com
 - Bugfix #8622: Properly URLEncode urls in response to propfind requests.
 - Improving logging
 - Bugfix #8643: Fixed response code for LOCK requests
 - Bugfix #8644: When infostore is disabled, WebDAV is also disabled
* Mon Jul 30 2007 - marcus.klein@open-xchange.com
 - Bugfix #7377. Fixed update of tasks to prevent loss of folder mappings.
 - Bugfix #8351. Server now runs with AggressiveHeap memory option.
 - Bugfix #8106. Startup order of JMX and ConfigDB fixed to get ConfigDB into
   monitoring.
* Mon Jul 30 2007 - thorben.betten@open-xchange.com
 - Bugfix #8525: Fixed missing personal encoding when sending a read
   acknowledgement
 - Bugfix #7936: Fixed typos in error messages
 - Bugfix #6274: Changed error message according to PM's suggestion
* Mon Jul 30 2007 - sebastian.kauss@open-xchange.com
 - Fixed Bug #4409 - Exception when opening a contact
* Mon Jul 30 2007 - ben.pahne@open-xchange.com
 - New Feature: Email Autocomplete
* Fri Jul 27 2007 ben.pahne@open-xchange.com
 - Fixed bug #7936, a lot of typos and text corrections
* Wed Jul 25 2007 ben.pahne@open-xchange.com
 - Bugfix 7368, changed a message status from error to info
 - Bugfix 7771, changed the SQL query for a linkage list
 - Bugfix 8417, changed the error message of a exception
 - Bugfix 8420, a connection handling problem
* Wed Jul 25 2007 - martin.kauss@open-xchange.com
 - Fixed bug #8482. If an appoinment with an enabled private flag was moved
   to a shared folder, no error message was shown. This has been fixed.
* Mon Jul 23 2007 - marcus.klein@open-xchange.com
 - Added config tree setting fastgui.
 - Bugfix #8427: Removing files if storing them fails.
* Mon Jul 23 2007 - martin.kauss@open-xchange.com
 - Fixed bug #8490. The wrong folder was send to the client if a shared folder
   was requested but the current user was also a participant in an appointment.
   This has been fixed.
* Mon Jul 23 2007 - marcus.klein@open-xchange.com
 - Improved SQL command when listing deleted tasks.
 - Removed Suns BASE64 en/decoder.
* Fri Jul 20 2007 - thorben.betten@open-xchange.com
 - Bugfix #8491: Allowing sharing of default folders (if owner still holds
   full rights on modified ACL list)
 - Fixed subfolder display on shared IMAP folders: A folder that was
   previously shared and subscribed but now does no more grant rights for
   affected user still occurs in user's subscription list (LSUB command).
   This is fixed now.
 - Added an own implementation of org.json.JSONWriter that uses real objects
   instead of a java.io.Writer
* Thu Jul 19 2007 - thorben.betten@open-xchange.com
 - Added mail folder clearing to mail servlet
 - Added invocation of FileItem.delete() on file upload to avoid double
   temporary files
 - Fastened reading of body content on PUT request in AJAXServlet.getBody()
 - Servlet mapping read from directory
* Thu Jul 19 2007 - francisco.laguna@open-xchange.com
 - Fixed build.xml to also copy subdirectories of the configuration.
* Wed Jul 18 2007 - thorben.betten@open-xchange.com
 - New design for custom IMAP commands
 - Moved imap-related packages to new package(s)
 - Bugfix #8472: Improved error message if a hidden subfolder is located
   underneath a folder that should be deleted
 - Bugfix #8498: Additional check when reading an infostore document's MIME
   type setting
 - Bugfix #8360: Removed senseless counter for working servlets in JMX's
   GeneralMonitor interface
* Tue Jul 17 2007 - francisco.laguna@open-xchange.com
 - Fix for bug #8478: Changed LogLevel.
* Tue Jul 17 2007 - marcus.klein@open-xchange.com
 - Bugfix #11337: Fixed reminder update SQL statement. Removing broken reminder.
* Fri Jul 13 2007 - thorben.betten@open-xchange.com
 - Slightly fastened AJP processing
 - Added file name extraction method to UploadEvent
 - Avoiding duplicate VCard attachment when saving draft message
* Fri Jul 13 2007 - francisco.laguna@open-xchange.com
 - Fix for bug #8392: Changed webdav servlet to expect ENCODED URIs. To ensure
   that they remain encoded the following option has to be set in the Apache configuration:
   JkOptions +ForwardURICompatUnparsed
############## PAY ATTENTION TO ME ! ################
 - CONFIG change: Option in apache configuration must be set:
JkOptions +ForwardURICompatUnparsed
#####################################################
* Fri Jul 13 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7470: Fixed and tested after all.
 - Bugfix #8411: Applied Thorben's patch and wrote test for it.
* Thu Jul 12 2007 - thorben.betten@open-xchange.com
 - Setting message's 'Organization' header surrounded by a try-catch clause
   in which a Throwable is caught thus sending a message is not aborted
   through a possible error while reading context admin's organization field
 - New method in USerConfiguration to detect if user is allowed to see portal
   page in GUI
* Thu Jul 12 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7732: An RRULE's COUNT property is now set as occurrence value
   for a CalendarObject.
* Wed Jul 11 2007 - thorben.betten@open-xchange.com
 - Added support for differing IMAP server's ACL handling plus
   auto-detection of IMAP server
 - Added possibility to map more machines to one host name when detecting
   User2IMAP impl
 - Fixed problem if IMAPProperties could not be properly initialized during
   session creation
* Tue Jul 10 2007 - thorben.betten@open-xchange.com
 - Bugfix #8316: Preventing the NPE occuring in first stack trace posted in
   bug
 - Additional bit in UserSettingMail to set the displayed content on message
   compose
 - Fastened parsing of query string inside AJP's forward request
 - New class IMAPServerInfo to read an IMAP server's greeting
 - Bugfix #7132: Fixed counting for open IMAP connections
* Tue Jul 10 2007 - marcus.klein@open-xchange.com
 - Bugfix #7140: Updated servlet API to newest version.
 - Bugfix #8351: Assigned 512MB maximum memory to groupware server.
* Tue Jul 10 2007 - martin.kauss@open-xchange.com
 - Fixed bug #8317. If a whole day appointment exist and a user
   creates an appointment around midnight which should conflict,
   depending of the timezone of the user, no conflict is resolved
   because of a wrong handling. This has been fixed.
* Tue Jul 10 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7949: Changed log-level for USER_INPUT exceptions 
   from warn() to debug().
* Mon Jul 09 2007 - marcus.klein@open-xchange.com
 - Bugfix #8337: Added proper content-type header to multiple response.
 - Bugfix #7047: Not working authentication is now logged as error.
 - Bugfix #7048: Exceptions in authentication mechanisms are not logged as
   "Invalid credentials" anymore.
 - Bugfix #7119: A not found context is not logged as error.
* Mon Jul 09 2007 - thorben.betten@open-xchange.com
 - Bugfix #8335: Remembering subscription status on folder rename for
   affected folder and its subfolders
 - Bugfix #8348: Proper exception handling on actionGetAttachment
* Mon Jul 09 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7710: Now the French Outlook value for the private field is
   recognized, too.
 - Bugfix #7949: Now using LOG.warn() instead of LOG.error() in case 
   of USER_INPUT exception.
 - Bugfix #8342: Searching for "target" attribute when parsing links
   inside html content of an email. If not present or its value is not
   equal to "_blank" it's going to be replaced.
* Fri Jul 06 2007 - thorben.betten@open-xchange.com
 - Fastened input stream reading from message parts
* Fri Jul 06 2007 - tobias.prinz@open-xchange.com
 - Bugfix #8325: Improved performance of VCardTokenizer. A lot.
* Fri Jul 06 2007 - marcus.klein@open-xchange.com
 - Bugfix #7046: Changed the error message.
* Thu Jul 05 2007 - martin.kauss@open-xchange.com
 - Added new feature that the conflict handling can be disabled.
   If the conflict flag is false, no conflict resolution or detection 
   is done, even if a resource is booked. This can lead into 
   overbooking of resources and this is not a bug but a feature.
 - Added new  feature that the free/busy handling can be disabled.
   If the free/busy flag is false, no free/busy results are returned, 
   even if the requested user or resource has appointments in
   the given timeframe. This is not a bug but a feature.
 - Fixed bug #8290. A multi span whole day account was 
   sometimes shown wrong in the mini calendar. This 
   could happen if the start of the appointment is before
   the start of the mini calendar. This has been fixed.
 - Fixed bug #7134. Weekly recurrence, occurrence and if the first occurrence
   is not in the first week after the start one occurrence is not calculated.
   This has been fixed.
 - Fixed bug #7878. The first recurring calculation result was never 
   provided by the free/busy interface. This has been fixed.
* Thu Jul 05 2007 - marcus.klein@open-xchange.com
  - Bugfix #7317: IMAPS now works with JavaMail 1.4.
  - Now the configuration setting tree adapts to user configuration of modules.
  - Bugfix #6692: Changed name of group containing all users to
    "All internal users".
* Thu Jul 05 2007 - thorben.betten@open-xchange.com
 - Fixed message headers 'X-Mailer' & 'Organization' settings when sending
   a message
 - Fixed folder handling on closure
 - Bugfix #8304: Displaying broken TNEF attachments at least as normal
   attachment
 - Fixed exception message formatting for folder exceptions
 - Added a watcher for established IMAP connections which keeps track of
   usage times and logs current state and using thread's stack trace.
   Moreover the watcher can be configured to close those IMAP connections
   which exceed the max. usage time.
   The watch is configured via property file 'imap.properties'
#################### ATTENTION ! #####################
 - CONFIG change: New properties in 'imap.properties' to configure
   watcher for IMAP connections:
   "watcherEnabled" to enable/disable watcher
   "watcherFrequency" to define watcher's frequency in milliseconds
   "watcherTime" to define the exceeding time
   "watcherShallClose" if watch is allowed to force closure of exceeded
   IMAP connections
#####################################################
* Thu Jul 05 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #8050: Now calling "ant -f build.xml all-i18n" on the
   server's buildfile will cause it to compare the German and the
   English .po/.pot files to check whether all texts have been 
   translated.
 - Fixed bug #7473 (importer): Trigger times in ICal are always 
   negative, alarmtimes in OX always positive. Just switching that now.
 - Fixed bug #7710 (importer): Now private flag is correctly read.
   This needed a ContactSwitcher able to translate several strings
   to a boolean value. Hopefully, the French translation is correct 
   also...
* Wed Jul 04 2007 - francisco.laguna@open-xchange.com
 - Fixed Bug #7870: Applied patch that corrects the HTTP status 
   codes for webdav MOVE and COPY requests.
 - Fixed Bug #8127: Applied patch that moves the property cleaning
   to the delete event handler for infoitems.
 - Fixed Bug #8111: Removed some unnecessary selects
 - Bug #7792: Logging HTTP Status 500 Exceptions as "Error".
* Wed Jul 04 2007 - thorben.betten@open-xchange.com
 - Invoking InternetAddress.toUnicodeString() when composing prefix line on
   message reply to get the decoded representation.
 - Bugfix #8280: Linebreak is performed after the whitespace character
 - Improved html2text conversion
 - Replaced usages of FolderString.ALL_GROUPS_AND_USERS with
   Groups.ZERO_DISPLAYNAME
 - Bugfix #8250: Fetching user's locale from user object instead from
   session. Thus the proper translation is used when changing user's
   language
 - Removing entry from UserConfigurationStorage on update/delete
* Wed Jul 04 2007 - martin.kauss@open-xchange.com
 - Fixed bug #7646. An conflict occured while updating n appointment
   with a resource if the endtime adjoins to another appointment start time and
   the same resource. This has been fixed.
 - Fixed bug #7064. Weekly and yearly recurring calculations, where
   the day was not in the first week, were calulated wrong.
   This has been fixed.
* Wed Jul 04 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #7632 (importer): OutlookCSVImporter uses cp1252 as
   standard encoding now, since this is the default charset on
   most Windows platforms and the default encoding for Outlook.
 - Bug #7470: Applied Viktor's patch. Now we are one bug further...
* Wed Jul 04 2007 - marcus.klein@open-xchange.com
  - Added module configuration to the GUI configuration interface.
* Tue Jul 03 2007 - thorben.betten@open-xchange.com
 - Bugfix #7848: Changed handling when creating a subfolder underneath a
   shared folder according to the suggestions posted in comment #11:
   Subfolder creation is no longer forbidden but owner of parental shared
   folder initially has full access (incl. folder admin) to subfolder. The
   creating user gets the permissions (excl. folder admin) as composed by
   GUI's permission composer dialog.
 - Bugfix #7862: Added occurences of UnsupportedEncodingExceptions to JMX
   monitoring interface as suggested by PM.
 - Bugfix #7950: Fixed broken reply if personal part of an email address
   contains quotable characters
 - Bugfix #7992: Added EventQueue to folder modification operations (create,
   modify & delete)
 - Bugfix #8018: INBOX folder is subscription status is checked along with
   default folder check. INBOX is subscribed if not yet done.
 - Bugfix #8107: Additional catch-clause when requesting folder's quota to
   handle the ParseException as unlimited quota
 - Bugfix #8133: Force folder unsubsription prior to its deletion
 - Avoiding OXFolderException being thrown when checking for for non-tree-
   visible public folders
 - Bugfix #8221: Proper decoding of address headers that do not contain a
   domain part.
* Tue Jul 03 2007 - martin.kauss@open-xchange.com
 - Fixed bug #6910. Moving a personal appointment into
   a shared sub folder did not moved the appointment.
   This has been fixed.
 - Fixed bug 7738. New config options exists in the filecalendar.properties,
   both are true by default.
   The first one (CHECK_AND_REMOVE_PAST_REMINDERS)
   checks if a reminder is in the past and removes the reminder from the object.
   To disable this option please use
   CHECK_AND_REMOVE_PAST_REMINDERS=FALSE
   The second one (CHECK_AND_AVOID_SOLO_REMINDER_TRIGGER_EVENTS)
   checks if only the reminder was changed. If this is true we avoid to trigger
   the event because this is a private field and no mail must be send and no
   client must be informed.
   To disable this option please use
   CHECK_AND_AVOID_SOLO_REMINDER_TRIGGER_EVENTS=FALSE
* Tue Jul 03 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #7718 (importer). Now the DESCRIPTION property of 
   VTODO is mapped to our Task.NOTE property.
 - Fixed bug #7719 (importer). Added special handling for TELEX 
   element which VCARD considers a mail object, but of course
   it does not pass the check for a proper mail adress (VCard is
   more tolerant than us).
 - Fixed bug #7703 (importer). Surprisingly, I could use the logic
   of WEEKLY occurrences for DAILY ones, too. A one line fix...
* Mon Jul 02 2007 - thorben.betten@open-xchange.com
 - Modularity: Added additional methods to UserConfiguration to check
   allowance for team view, free-busy, and conflict handling
 - Fastened expunging messages
 - Fixed exception message if user has no infostore access
 - Bugfix #7591: Removed exception message prefixed from folder exceptions
 - Partial Bugfix #7677: Setting for "imapSupportsACL" in file 'imap.proeprties'
   allows three values: true, false & auto
 - Bugfix #7845: NullPointer check before inserting new addresses
* Mon Jul 02 2007 - marcus.klein@open-xchange.com
  - Bugfix #7574: Now checking if objects exist before returning a cache proxy
    object.
  - Bugfix #8147: Autologin from wrong IP now returns an error and cookies are
    deleted.
* Fri Jun 29 2007 - thorben.betten@open-xchange.com
 - Added AJP watcher in accordance to database's watcher mechanism:
   If an AJP listener is in process for longer than x seconds its
   stack trace is going to logged.
   Affected Properties in ajp.properties:
   - AJP_WATCHER_ENABLED must be set to TRUE
   - AJP_WATCHER_MAX_RUNNING_TIME defines the amount of milliseconds
   	after which a stack trace is logged
 - Fastened message update (system flags, color label, spam/ham handling).
   Those operations are increased to max performance now.
* Thu Jun 28 2007 - marcus.klein@open-xchange.com
  - Bugfix #8007: Partly fix. Disabled possibility to create unbound folders
    through WebDAV XML interface. Replacing existing unbound folders in database
    with contact folders to prevent AJAX GUI errors.
* Thu Jun 28 2007 - martin.kauss@open-xchange.com
 - Removed enum from recurring API. The API can now be used with Java 1.4.
 - Fixed bug #8196. If the owner deleted his reminder in Outlook, a participant
   got a reminder at start time. This has been fixed.
 - Fixed bug #7734. If an appointment was moved to another time, the reminder
   was not re-calculated. This has been fixed.
 - Fixed bug #7273. The confirm state was not changed if an appointment was 
   moved to a different time. Now the confirm state is set to NONE.
* Wed Jun 27 2007 - martin.kauss@open-xchange.com
 - Refactored Recurring API. The API is now in a seperate package.
* Wed Jun 27 2007 - thorben.betten@open-xchange.com
 - Collecting multiple requests to store message flags which delivers a
   huge performance gain
 - Checking only subscribed subfolders when requesting subfolders flag
   in non-config folder tree
* Wed Jun 27 2007 - francisco.laguna@open-xchange.com
  - Add french messages to ox_languages.jar
* Tue Jun 26 2007 - thorben.betten@open-xchange.com
 - Modularity: Added invalidation routines to UserConfigurationStorage interface
 - Modularity: Added Cloneable interface to UserConfiguration thus cached
   implementation stores/returns cloned versions. Therefore altered
   instances fetched from cache do not change cached instance
 - Modularity: Added additional folder permission checks on folder creation/update:
   -> Sharing of a private folder is forbidden if user's config denies
      full shared folder access.
   -> Permissions defined for a folder are checked against entity's
      user config to ensure its applicability. Currently an exception
      is thrown but can be changed to any other handling.
* Mon Jun 25 2007 - thorben.betten@open-xchange.com
 - Modularity: Added UserConfigurationStorage to allow global access to
   every user's configuration settings
 - Modularity: Fixed some wrong permission checks in folder module
#################### ATTENTION ! #####################
 - CONFIG change: New property in 'system.properties' to define the
   name/alias of the implementing class of UserConfigurationStorage:
   UserConfigurationStorage
 - CONFIG change: New JCS region defined in 'cache.ccf' for
   UserConfigurationStorage which got its settings equal to existing
   user cache.
######################################################
* Thu Jun 21 2007 - marcus.klein@open-xchange.com
  - Bugfix #7670: Check in session check if the user is still enabled.
  - Bugfix #7901: Bad exception is catched when underlying user object is
    deleted.
* Wed Jun 20 2007 - marcus.klein@open-xchange.com
  - Bugfix #7679: Added possibility to bind groupware ports to a configurable
    hostname.
    CONFIG CHANGE: 2 additional options in ajp.properties and server.properties.
    Both have a fallback of "localhost".
  - Bugfix #7289 Forwarding cookies from existing session to umin login.
  - Added option for showing external participants without email address.
    CONFIG CHANGE: new file participant.properties and new path to this file in
    system.properties.
* Wed Jun 20 2007 - thorben.betten@open-xchange.com
 - Bugfix #7679: Extended to allow special value "* Wed Jun 20 2007 - martin.kauss@open-xchange.com
 - Fixed bug #7883. The reminder was set for all participants even if only
   one participant has set the reminder. This has been fixed.
 - Fixed an issue that while creating an recurring exception for whole day
   events the master recurring appointment has the same start/end time after
   calculation.
 - Fixed an whole day calculation issue for recurring events.
* Tue Jun 12 2007 - choeger@open-xchange.com
 - Bugfix ID#7859 OXEE: Groupware server must not be restarted while update
* Tue Jun 12 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7683 OXEE: Use fully qualified hostname in direct link generation.
* Fri Jun 08 2007 - thorben.betten@open-xchange.com
 - IMAP folder rights are now cached in user's session to decrease
   execution of IMAP command MYRIGHTS
* Wed Jun 06 2007 - marcus.klein@open-xchange.com
  - Bugfix #7757: Using user specific option in configuration for GUI.
* Wed Jun 06 2007 - thorben.betten@open-xchange.com
 - Bugfix #7825: Fixed communication problems if chunked transfer-encoding
   is used
 - Bugfix #7361: Fixed deletion of large number of mails
 - Fixed bug that no structural view is allowed on a shared folder even if
   a shared folder has a shared subfolder. All shared folders are displayed
   as a list.
* Tue Jun 05 2007 - marcus.klein@open-xchange.com
  - Added a method to database pooling API for changing the connection check
    time.
* Tue Jun 05 2007 - thorben.betten@open-xchange.com
 - Fixed formatting of forward/reply text
* Mon Jun 04 2007 - marcus.klein@open-xchange.com
  - Bugfix #7778: Checking client IP address to prevent session stealing.
* Mon Jun 04 2007 - thorben.betten@open-xchange.com
 - Processing of an upload moved to a public static method to make it
   available in admin servlets
* Fri Jun 01 2007 - marcus.klein@open-xchange.com
  - Bugfix #7730: Enabled IMAP ACLs for OX EE.
* Fri Jun 01 2007 - thorben.betten@open-xchange.com
 - Divided initialization of backend services into two parts: JMX & AJP.
   The first one is now going to be started before database initialization
   and the latter is started at the very end of initialization process.
 - Added proper logging mechanism when servlet intialization is executed
 - Bugfix #7214: Additional check to ensure folder owner always is a folder
   admin of a default folder.
 - Bugfix #7503: Unfortunately given writeable connection was not committed,
   now it is if it's set to auto-commit
 - Bugfix #7725: Changed error log to be a debug log only.
* Fri Jun 01 2007 - francisco.laguna@open-xchange.com
 - Bug 7683: Merged [hostname] resolution for direct link from bf_6_2 branch.
* Thu May 31 2007 - thorben.betten@open-xchange.com
 - Bugfix #7720: Tabs are removed from address header field on reply
 - Bugfix #7593: Removed check for INBOX folder on update
 - Bugfix #7480: Again optimized regex pattern for href parsing to ignore
   non-ascii characters
* Wed May 30 2007 - thorben.betten@open-xchange.com
 - Bugfix #7457: Solved many problem arising from granting only LOOKUP
   right to an user
 - Bugfix #7600: Checking initial mail folder rights on folder creation
   before applying new ACLs to it.
 - Bugfix 7603: Preventing the user from granting "create subfolder"
   permission on a shared folder for another user and denying the creation
   of a new folder underneath a shared folder
 - Bugfix #7677: Checking for imap.properties setting for "imapSupportsACL"
   when applying composed permissions to a mail folder
 - Bugfix #7652: ServletRequestWrapper returns null in getContentType()
   method and getCharacterEncoding() method if not set during request. This
   bugfix forces to search for every usage of these methods and to insert
   default value as defined through config:
   ServerConfig.getProperty(Property.DefaultEncoding)
 - Bugfix #7615: Added new UpdateTask to equal field size of VARCHAR
   column "fname" of table "del_oxfolder_tree" to the one located in table
   "oxfolder_tree"
* Tue May 29 2007 - francisco.laguna@open-xchange.com
 - Partial Fix 7557: Annotation Processor also checks class declarations
* Tue May 29 2007 - tobias.prinz@open-xchange.com
 - Partial fix #6825: Now handling all messages where data given is too
   long for our database. This makes part (3) of the bug report a lot better,
   yet it still needs translation (and possibly i18n).
* Fri May 25 2007 - tobias.prinz@open-xchange.com
 - Bugfix #6825 (part): OxContainerConverter did not discern between external
   and internal users, which lead to the rather unhelpful UNEXPECTED_EXCEPTION.
   This fixes the main problem with this bug report.
* Thu May 24 2007 - thorben.betten@open-xchange.com
 - Changed ServletConfigLoader to allow a global property file that applies
   to all servlets
 - Bugfix #7535: Fixed parsing of URLs in plain text messages
 - Bugfix #7526: Added property 'smtLocalhost' to property file
   'imap.properties' to specifiy the domain name that is going to be
   transmitted on SMTP's HELO/EHLO command
* Thu May 24 2007 - thorben.betten@open-xchange.com
 - Bugfix #7588: Avoiding NPE if content type is missing in infostore
   document that ought to be sent as attachment
 - Bugfix #7548 : Use objects parentFolderId if the user participant
   doesn't contain a user specific folderId.
* Thu May 24 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7538 : AJAXServlet.substitute used String.replaceAll which 
   removed some backslashes. This lead to unescaped quotation marks in
   error messages, which broke the JSON response. Fixed this for all
   response callbacks upon a HTTP-POST.
 - Bugfix #7552 : Outlook imports now handle dd.MM.yyyy format, too.
* Wed May 23 2007 - marcus.klein@open-xchange.com
  - Bugfix #7275: Changed error message once again.
* Wed May 23 2007 - tobias.prinz@open-xchange.com
 - Bugfix #7472: Added handling of CONFIDENTIAL flag: Entry flagged as such
   is not imported. Bug fixed.
* Wed May 23 2007 - thorben.betten@open-xchange.com
 - Bugfix #7280: Changes to apply to new tnef.jar v.1.3.1
 Changes made in bugfix branch:
 - Bugfix #7401: Improved template for Message Disposition Notification (MDN)
 - Bugfix #7480: Fixed parsing of href elements occuring inside img's src
   attribute
 - Bugfix #7288: Fixed empty message composer dialog if header "Subject" is
   missing in original message on reply
 - Bugfix #7260: Added missing header field 'Organization'
 - Bugfix #7323: Changed handling of ACL editing when multiple entities
   hold 'ADMINISTER' right on an IMAP folder.
 - Bugfix #7361: Avoiding too long argument(s) in IMAP request resulting
   from IMAPFolder.getMessagesByUIDs()
 - Bugfix #7331: Changed error message if sending fails cause message is
   too large
 - Bugfix #7362: Added parameter 'limit' to 'action=newmsgs' request from
   GUI as defined in HTTP spec
* Wed May 23 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7507: Resolve [hostname] to the hostname of the system in direct links
* Tue May 22 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7459: Add content-disposition headers for ordinary downloads also.
* Tue May 22 2007 - tobias.prinz@open-xchange.com
 - Added new error category "WARNING".
 - Bugfix #7109: Added server side solution using WARNING for partial inserts.
 - Bugfix #7472: Added handling of PRIVATE flag. Treating CONFIDENTIAL flag 
   as PRIVATE flag.
* Mon May 21 2007 - tobias.prinz@open-xchange.com
  - Bugfix: Loading the contact folder via CSV import did not work properly
    when folder was not cached already. Fixed this in CSVLibrary.java
* Mon May 21 2007 - thorben.betten@open-xchange.com
 - Enhancements for mobility OXtender: New servlet for special sync
   requests and new interface (and its implementation) to clear a folder's
   content in API package
* Fri May 18 2007 - marcus.klein@open-xchange.com
  - Bugfix #7374: Selecting deleted resources since a last sync time is fixed.
  - Bugfix #7380: Tasks with removed participants can now be deleted.
* Fri May 18 2007 - tobias.prinz@open-xchange.com
  - Bugfix #7386: Forgot to add exception to exception list when 
    encountering major parsing error in ICalImporter.
* Wed May 16 2007 - martin.kauss@open-xchange.com
 - An error message was rewritten like mentioned in bug #7292
 - Fixe bug #7281. The private folder id was used from the first participant
   when an appointment was updated. This has been fixed. 
* Wed May 16 2007 - marcus.klein@open-xchange.com
  - Bugfix #6902: Improved search pattern for group searching.
  - Bugfix #7275: Removed encapsulating of LoginException into SessionException.
  - Bugfix #7332: Fixed coding issue causing IllegalStateException.
* Wed May 16 2007 - tobias.prinz@open-xchange.com
  - Bugfix #7249: Validating e-mail before inserting e-mail 
    address from uploaded VCard...
  - Fixed response of ImportExportWriter in case of exception.
  - Added yet another translation possibility for ContactField: 
    Constants used in AJAX/Servlet part are now understood.
* Wed May 16 2007 - francisco.laguna@open-xchange.com
  - Bugfix #6104: Upload file size is checked against quota as set in config files and user_setting_mail table.
* Tue May 15 2007 - marcus.klein@open-xchange.com
  - Bugfix #6942: Now every new task will contain the attribute modified by.
  - Bugfix #7269: Now random timeout of session is done correctly.
* Tue May 15 2007 - tobias.prinz@open-xchange.com
  - Bugfix (reported by Suphi): Sending a Outlook CSV file as normal CSV 
    file caused ClassCastException because of different date formats. 
    Catching it now, refactored the CSV importers to make that easier.
  - Bugfix #7250 / #7107: Changed VCardTokenizer to work on ByteArrays
    instead of Strings to be independend of encoding.
  - Bugfix #7248: Added better testcase to make sure it is gone.
* Mon May 14 2007 - martin.kauss@open-xchange.com
 - Database changes (alter field length) to fix bug #6514. The mail address 
   can now contain 286 chars. 
 - Build in some configuration paramater to disable the fast pre fetch and to
   set the max. pre fetch block size.
   To disable the fast pre-fetch feature:
   CACHED_ITERATOR_FAST_FETCH=FALSE
   The default is TRUE
   To set the max. pre fetch size:
   MAX_PRE_FETCH=10
   The default is 20
* Mon May 14 2007 - marcus.klein@open-xchange.com
  - Bugfix #6302: Empty multiple request can't be sent by GUI. Added logging for
    some HTTP header in error case to be able to eliminate self build requests.
  - Bugfix #5629: Checking empty values before authenticating agains ldap to get
    invalid credentials if some login value is missing.
  - Bugfix #6303: Return a connection to the pool if setting the schema fails.
  - Bugfix #6345: Improved exception handling if JSON is malformed.
* Fri May 11 2007 - marcus.klein@open-xchange.com
  - Bugfix #7217: Removing first slash from href of webdav xml now works again
    for root folder.
* Fri May 11 2007 - thorben.betten@open-xchange.com
 - Added preparations to distinguish between imap login and user name when
   trying to determine the user ID on ACL operations
 - Bugfix #7107: More tolerant parsing of VCard's N element
 - Bugfix #7132 again: Hopefully counting now works correct
 - Remember user's groups when accessing through user configuration
* Fri May 11 2007 - tobias.prinz@open-xchange.com
 - Removed special chars and used Unicode \u??? style in ContactFieldMappers
 - Bugfix #7248 / #7107: Changed parsing of VCard files with less than five N 
   elements or 7 ADR elements in OXContainerConverter.
* Thu May 10 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #7105, part 2: Outlook CSV files may now be in English,
   German or French. I consider #7105 completely fixed now.
 - Added some new classes to help with translating fields of
   Contacts to Outlook names and vice versa.
* Wed May 09 2007 - marcus.klein@open-xchange.com
  - Bugfix #7121: Changed database authentication mechanism to prevent security
    holes.
  - Bugfix #6215: Split login information at last @ sign.
  - Bugfix #6602: Now an additional thread is used for prereading found tasks
    while request thread reads bunches of participants and reminders.
* Wed May 09 2007 - martin.kauss@open-xchange.com
 - Code clean up in some calendar classes. Avoided the use of the deprecated
   OXFolderTools.
 - Optimized calendar performance by reducing single SQL statements. Now many
   requests are bundled and executed by some requests. Additional the conflict
   resolution was optimized by reducing SQL queries and using an cached object
   instead.
* Wed May 09 2007 - thorben.betten@open-xchange.com
 - New method to clear folder's content
 - Bugfix #7153: Added a property to 'imap.properties' to define the
   character encoding that is going to be used on imap authentication
#################### ATTENTION ! #####################
 - CONFIG change: New property in 'imap.properties' to define the
   character encoding that is going to be used on imap authentication
######################################################
* Wed May 09 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #7106: Several conversions of Streams to strings which 
   did rely on UTF-8 as default encoding. Changed to explizit usage.
 - Fixed bug in OutlookImporter that made the importer not act on getting 
   an Outlook CSV file.
* Wed May 09 2007 - francisco.laguna@open-xchange.com
 - Added utility class for handling indexes in update tasks.
* Tue May 08 2007 - thorben.betten@open-xchange.com
 - Continue code review
* Tue May 08 2007 - francisco.laguna@open-xchange.com
   - Bugfix #7012: exists routine now also checks access permissions to filter links properly.
   - Bugfix #7160: corrected LocalFileStorage to provide all arguments to the exception.
   - Optimized SearchEngine for empty patterns, to avoid involving a BLOB field.
   - Added LOG.fatal to AbstractOXExceptionFactory when an error is detected.
* Tue May 08 2007 - sebastian.kauss@open-xchange.com
  - Fixed Bug #7083: Fixed a problem in the webdav xml interface 
    that the last modified attributes was not used in request for groups and resources.
  - Fixed Bug #7152: Fixed a problem that active sessions are not decremented correctly.
* Tue May 08 2007 - martin.kauss@open-xchange.com
 - Fixed bug #7126. Some monthly recurring appointmens were calculated wrong
   if the daylight saving time changed. This has been fixed.
 - Fixed bug #6960. The recurrence id was missing in the return object if a
   recurring exception was deleted. This has been fixed.
 - Enhanced logging if an attachment is requested via the webdav interface but
   the object does not exists. The bug nummer for this issue is 7141.
 - Fixed bug #7168. Now the status of the participant is automatically set to
   ACCEPT if an appointment is created.
* Mon May 07 2007 - francisco.laguna@open-xchange.org
  - Bugfix #7053. Changed query for locks. Doesn't load locks and counts them but checks it query matches. 
  - Bugfix #7131. Fixed NPE in HEAD query for folders.
  - Bugfix #6334. NPE due to autoboxing on partial GET for folders.
  - Removed some printStackTrace calls.
  - Bugfix #7143 Infostore and User Store are now considered virtual folders
  - Bugfix #7012: Corrected error messages when user has insufficient read permissions
      Changed filtering for links to check for permissions
* Mon May 07 2007 - thorben.betten@open-xchange.com
 - Nearly every log statement which uses a level less than 'error'
   surrounded with an if-statement checking log level
 - Bugfix #7132: Added a flag to monitor connection state and check it's
   value on closure for proper counting
 - Mighty code review
* Mon May 07 2007 - marcus.klein@open-xchange.com
  - Bugfix #6127: Fixed a small coding error causing the complete request to
    fail.
* Mon May 07 2007 - tobias.prinz@open-xchange.com
  - Bugfix #7105: Parser has become more tolerant to different amounts of 
    cells per line. Fixes the reported exception but not the basic problem 
    of Outlook imports.
  - Worked on response handling of ImportServlet. Should work now in any case.
* Mon May 07 2007 - martin.kauss@open-xchange.com
  - Fixed bug #6910. Moving an existing appointment from a private folder
    to a shared folder of another user was not visible anymore. Now the shared
    folder owner is added to as participant and the appointment is visible in
    both folders.
* Fri May 04 2007 - martin.kauss@open-xchange.com
 - Fixed bug #7016. Reminders for recurring appointments were not
   saved if the reminder was in the past. Now we check the end date
   and not the reminder date. If the end date is not in the past the
   reminder is created.
 - Fixed bug #6408. Reminders were not saved if the reminder was in the
   past even if the appointment starts in the future. Now the appointment
   end date is checked and not the reminder date. If the end date is in
   the past no reminder is stored.
 - Fixed bug #6535. Changing a recurring exception changed the confirm status
   of the user as well. This has been fixed.
* Fri May 04 2007 - thorben.betten@open-xchange.com
 - Reviewed class AJPv13ForwardRequest.java
 - Ignore-case-lookup of JSESSIONID in URL
 - Partial code review
 - Changes to approach fix of bug #7078
* Fri May 04 2007 - stefan.preuss@open-xchange.org
  - Bugfix #7112. Setting "Color quoted lines" has no effect.
* Fri May 04 2007 - tobias.prinz@open-xchange.com
 - Added check to ImportServlet for empty files. Did not solve
   bug #7089, but is useful anyway.
 - Fixed #7089, which was a never terminating while-loop in case
   of a broken file. Now the appropriate exception is caught inside
   the while-loop and the loop is stopped.
 - Added some more error messages to the importers to make it easier
   for the GUI team to know whether an import did work partially or 
   not at all.
* Thu May 03 2007 - martin.kauss@open-xchange.com
 - Fixed bug #6214. After moving an appointment into a subfolder a reminder
   was stored even if no reminder is requested. This has been fixed.
 - Fixed bug #6107. It was not possible to set flags to single appointment
   from recurrin appointments. This has been fixed.
* Thu May 03 2007 - thorben.betten@open-xchange.com
 - Slightly fastened AJP processing
 - Bugfix #7002: Color flags were sent back from cache entry on update.
   This is fixed.
 - Some changes to approach the fix for bug #6673
 - Simplified IMAP connection monitioring in class MailInterfaceImpl
* Wed May 02 2007 - thorben.betten@open-xchange.com
 - Bugfix #7008: Fixed comma-separation in multiple request
 - Bugfix #6994: Proper decoding a original message's subject on creating
   reply's display version
 - Bugfix #7007: Using java.text.Collator to sort strings locale-specific 
 - Bugfix #6971: Server crashes if backend services cannot be initialized
 - Added update task to add column 'passwordMech' to table 'user' if not
   present
 - Support of AJPv13 CPing request (reply quickly with a CPong)
* Wed May 02 2007 - francisco.laguna@open-xchange.com
 - Bugfix #7001 Remove all caching headers for downloads.
 - More robust JSONArray handling for multiple servlet (KUDOS to thorben)
* Wed May 02 2007 - tobias.prinz@open-xchange.com
 - Fixed potential follow up to #6962 by adding another check for
   unknown VCard format.
* Wed May 02 2007 - martin.kauss@open-xchange.com
 - Fixed bug #6400. An error message is thrown if a user moves an
   recurring exception into a different folder.
 - Fixed bug #6498. Updating a recurring appointment and changing
   the occurrence did not change the until date if no start and end
   date was submitted. This has been fixed.
* Mon Apr 30 2007 - tobias.prinz@open-xchange.com
 - Fixed bug #6962 by adding a tokenizer for VCard files, so every entry
   may have different versions and the file may come with any MIME type
   possible.
* Fri Apr 27 2007 - thorben.betten@open-xchange.com
 - Copy/move of messages made much more performant
 - Adding a warning if UploadQuoteChecker falls back to global server
   property 'MAX_UPLOAD_SIZE'
 - Error if source folder is equal to destination folder on message move
   operation
 - Fixed bug when moving all messages of a mail folder to another not yet
   selected mail folder and number of messages to move is low (< 10)
* Thu Apr 26 2007 ben.pahne@open-xchange.com
 - Fixed Bug 6688  for this branch: Contact loses display_name information when setting flag
* Thu Apr 26 2007 - thorben.betten@open-xchange.com
 - Again fixed encoding of message's subject: Using both MimeUtility's
   routine and own routine
 - Simplified class FolderWriter.java
 - Supporting public IMAP folders
 - Bugfix #6957: Fixed automatic linewrapping when sending plain text
   messages
* Thu Apr 26 2007 - tobias.prinz@open-xchange.com
 - Fixed ICALExporter to prevent something like bugs 6823, 6825
* Thu Apr 26 2007 - marcus.klein@open-xchange.com
  - Implemented Config Jump for new OXExpress user administration interface.
* Wed Apr 25 2007 - thorben.betten@open-xchange.com
 - Added new methods for re-using JSON objects into JSON API: reset() and
   parseJSONString(String str)
 - A lot of code review
 - Optimized folder-cache's write-lock usage on modifying action(s)
* Wed Apr 25 2007 ben.pahne@open-xchange.com
 - Fixed Bug #6743, Upload contact image: error messages incorrect
* Wed Apr 25 2007 francisco.laguna@open-xchange.com
 - Corrected some error messages
 - Fixed Bug #6842. Use setHeader to remove pragma header
 - Fixed Bug #6157. Don't format error messages in Exception class
* Tue Apr 24 2007 - thorben.betten@open-xchange.com
 - Checking both global system property for spam enablement and
   user-defined property for spam enablement when creating/checking
   standard mail folders for confirmed spam/ham
 - Bugfix #6872: Href declarations inside plain text messages are skipped
   on linebreak to maintain a proper link
 - Fastened line break algorithm for plain text messages
* Tue Apr 24 2007 - marcus.klein@open-xchange.com
  - Replaced spam button option in server properties with the one from imap
    properties.
  - Renamed exception category PROGRAMMING_ERROR to CODE_ERROR.
  - Bugfix #6579: During login everything but invalid credentials are logged as
    error.
  - Bugfix #6772: Build number now gets into server.
  - Bugfix #6286: This bug has been fixed with a merge from the bugfix branch.
* Tue Apr 24 2007 - francisco.laguna@open-xchange.com
  - Fixed Bug #6893 Omit X-OX-Reminder header on appointment and task delete
  - Fixed Bug #6618 Change server.pot strings.
  - Partially fixed Bug #5659 : Added timezone to dates.
  - Fixed the other half of Bug #5659
#################### ALARM ! ALARM ! #####################
  - CONFIG change: New property in 'notification.properties' to set web gui location (object_link)!
######################################################
* Mon Apr 23 2007 - sebastian-kauss@open-xchange.com
 - Fixed bug #6440: Prevent a ArrayOutOfBoundsException in CalendarWriter if
  a the user wasn't found in the participant array.
 - Fixed bug #6455: Log data truncation exception as debug log
 - Fixed bug #6486: Check if last modified is not null in update request
* Mon Apr 23 2007 - tobias.prinz@open-xchange.com
 - Fixed Bug #6823: removed chacking against mimetype(s...), used Format instead
* Mon Apr 23 2007 - thorben.betten@open-xchange.com
 - Fixed bug when an instance of MailFolderObject is constructed from an
   IMAPFolder object: getType() implicitely calls checkExists() which may
   throw a FolderNotFoundException.
 - Bugfix #6785: Portal view of mails behaves correct.
#################### ATTENTION ! #####################
  - CONFIG change: New property in 'imap.properties' to define socket I/O
    timeout value in milliseconds
######################################################
* Fri Apr 20 2007 - thorben.betten@open-xchange.com
 - Allowing to subscribe/unsubscribe default folders
* Fri Apr 20 2007 - ben.pahne@open-xchange.com
 - Fixed Bug #6779: Users can now edit their own information in the global
   address book
* Fri Apr 20 2007 - tobias.prinz@open-xchange.com
 - Changed behavior of ImporterServlet to be stricter: Now broken files
  are not parsed as far as possible. System simply throws exception.
* Thu Apr 19 2007 - thorben.betten@open-xchange.com
 - Added methods to update package (especially in class Updater) to check
   both if a schema is locked and is a schema needs to be updated
 - Added rudimentary implementation of spam handling
 - Spam handling completed
 - Setting a renamed folder to be subscribed
 - Global config option to enabled/disable spam functionaliy inside mail
   module
* Wed Apr 18 2007 - thorben.betten@open-xchange.com
 - Added folder's message count information (total, new, unread & deleted)
   to JSON message representation
 - Fixed bug when trying to update a folder with empty permissions
 - Added update tasks to extend table 'user_setting_mail' by necessary
   columns for spam handling
* Wed Apr 18 2007 - tobias.prinz@open-xchange.com
  - fixed import tests for iCal Importer, couldn't find bug in 
    export though. Reassigned to developer.
  - refactored CSV- and Outlook-CSV importers to make re-use easier. 
* Tue Apr 17 2007 - thorben.betten@open-xchange.com
 - Bugfix #6709: Correct email addresses filled on reply-all
 - Bugfix #6678: Log a warning if GUI tries to mark an already expunged
   message as /SEEN
 - Bugfic #6676: Checking string values in UserSettingMail before filling
   SQL INSERT statement
 - Bugfix #6677: Avoiding NPE on reply action if original message is not
   present anymore
* Tue Apr 17 2007 - marcus.klein@open-xchange.com
  - Bugfix #6724: Enabled POST method for login.
* Fri Apr 13 2007 - tobias.prinz@open-xchange.com
  - Fixes to make response of ImportServlet nicer to handle for GUI
* Fri Apr 13 2007 - tobias.prinz@open-xchange.com
  - Again working on the response of the ImportServlet,
    this time: error messages.
* Fri Apr 13 2007 - marcus.klein@open-xchange.com
  - Added option to the configuration tree for showing/hiding spam button.
* Fri Apr 13 2007 - thorben.betten@open-xchange.com
 - Bugfix #6687: Attachments do not get lost anymore when editing a draft
   message
* Thu Apr 12 2007 - thorben.betten@open-xchange.com
 - Bugfix #6668: Fixed NPE when creating
   'com.openexchange.groupware.contexts.Context' object from given context
   ID
* Thu Apr 12 2007 - tobias.prinz@open-xchange.com
 - Thorben fixed small calculation error in UploadEvent at my desktop, so
   I checked it in.
 - Changed response of ImportServlet from JSON to JavaScript callback as
   needed by GUI team. Fixed tests accordingly.
* Wed Apr 11 2007 - thorben.betten@open-xchange.com
 - Fixed problem during update process if multiple login requests occur
 - Indicating proper error message if a context is currently beeing updated
 - Fixed several problems in GeneralMonitor MBean
* Wed Apr 11 2007 - marcus.klein@open-xchange.com
  - Bugfix #6462: Task and appointment notifications can be turned off through
    configuration interface.
* Tue Apr 10 2007 - marcus.klein@open-xchange.com
  - Use all jars in lib directory for starting OX.
* Tue Apr 10 2007 - thorben.betten@open-xchange.com
 - Extended c.o.t.Collections class by a copy method that generates a deep
   copy of a serializable object
 - Using a faster implementation of ByteArrayOutputStream in
   OXServletOutputStream
 - Several fixes in update process
* Thu Apr 05 2007 - thorben.betten@open-xchange.com
 - Using UIDPLUS extension to append messages to a mail folder to
   immediately know assigned UIDs
 - Removed unnecessary event handler from folder cache
 - Initial classes for remote cache
* Wed Apr 04 2007 - thorben.betten@open-xchange.com
 - Fixed bug when accessing last message in a IMAP folder, in which the
   last message has been deleted in previous request.
 - Bugfix #5086: Indicating errors in a well-formatted JavaScript text on
   POST requests
 - Bugfix #6614: Format sequence number argument in IMAP command, cause an
   IMAP command must not exceed max. length of 16384 bytes.
* Wed Apr 04 2007 - sebastian.kauss@open-xchange.com
 - Fixed bug #6485: Fixed problem in appointment xml parser that
   delete exception are not parse correctly.
 - Fixed bug #6359: Add debug messages to find expired sessions
* Tue Apr 03 2007 - thorben.betten@open-xchange.com
 - Bugfix #6280: Added less strict parsing of header 'Content-Type'
 - Using same buffer size throughout all OutputStream instances
   (ServletOutputStream & SocketOutputStream)
* Mon Apr 02 2007 - thorben.betten@open-xchange.com
 - Enhanced mail display by an enriched2html conversion
 - Fixed bug #6592: Only entity's permissions are going to be deleted
   instead of all folder-associated permissions
 - Bugfix #6280: A single corrupt message does not affect whole fetch
   command
 - Bugfix #6561: Avoid NPE on getMergedPermission()
* Fri Mar 30 2007 - thorben.betten@open-xchange.com
 - Fixed bug #6585: Server enhanced by 'Automatic Database Update'
 - Fixed ClassCastException when creating a proxy for a context
 - Fixed missing display of messages whose content type is
   'multipart/alternative' and whose alternative version is different to
   MIME type 'text/html'
* Wed Mar 28 2007 - thorben.betten@open-xchange.com
 - Implemented method javax.servlet.ServletRequest.getScheme() in type
   c.o.t.servlet.ServletRequestWrapper
 - Fixed bug when handling message headers that do not occur in previous
   messages but appear in later ones
 - Implemented better initialization of servlet's ServletConfig and
   ServletContext references: now each servlet can hold its own config and
   context settings. Many thanks go out to Francisco!
 - Fixed bug #6567: Broken message's BODYSTRUCTURE is logged as WARNING
   instead of an ERROR cause it's related to a corrupt rfc822 message
   header
 - Fixed bug #6566: String constant moved from class MailStrings to avoid
   its translation.
 - Fixed bug #6466: Discarding cookies which cause an IllegalArgumentException
   when creating corresponding instance of javax.servlet.http.Cookie
* Mon Mar 26 2007 - thorben.betten@open-xchange.com
 - Fixed possible ArrayIndexOutOfBoundsException when requesting next UID
   on empty folder
* Fri Mar 23 2007 - thorben.betten@open-xchange.com
 - Fixed counting of established IMAP connections
 - Try to fix the bug related to fetch a newly created message with
   getMessageByUID() 
* Fri Mar 23 2007 - francisco.laguna@open-xchange.com
 - Merged marcus' bugfix for group search UnsupportedOperationException.
* Thu Mar 22 2007 - thorben.betten@open-xchange.com
 - External process 'sa-learn' is invoked in a separate thread to avoid
   possible server delay
 - Double break when quoting original message text on reply
* Thu Mar 22 2007 - francisco.laguna@open-xchange.com
 - Bugfix # 5485. ParticipantNotify tries to resolve a resource to a person
   if it can't be found. 
 - Bugfix # 6334. Error in size calculation for partial GET.   
 - Bugfix # 6315. Transfer lock ownership to mailadmin when a user is deleted
 - Throw delete events when removing infoitems during user delete to remove subordinate locks.
 - Event Handling to remove remaining locks if an item is deleted.
 - Bugfix #6524 Sort participants in Notification eMails.
* Wed Mar 21 2007 - thorben.betten@open-xchange.com
 - Fixed move of folders which only hold folders (\NoSelect flag set)
 - Improved rights check to avoid a possible exception if examined folder
   only holds folders (IMAP command 'MYRIGHTS' throws a MessagingException
   if it is invoked on a non-selectable folder)
 - Caching of HOLDS_MESSAGES information in DefaultImapConnection class to
   avoid unnecessary JavaMail checks
 - Ensure that a charset is set, when sending a message via MailObject
 - Fixed Bug #6499: Using proper file extension for vCard attached by
   webmailer
* Tue Mar 20 2007 - thorben.betten@open-xchange.com
 - Fixed bug when checking user flag support on non-existent or
   non-holding-messages mail folders
 - All JavaMail-specific properties configurable through new property file
   'javamail.properties'
 - Changed error handling of SocketException 'Broken Pipe' to log only as a
   warning, cause this kind of error is related to an aborted file download.
 - Expunge only applied to affected messages on move; whole folder expunge
   is performed no more
#################### ATTENTION ! #####################
  - CONFIG change: New property in 'system.properties' to link to a new
    property file
  - CONFIG change: New property file 'javamail.properties' that collects
    all known JavaMail properties
######################################################
* Mon Mar 19 2007 - thorben.betten@open-xchange.com
 - Fixed bug #5903: Added information to error, that its origin is
   temporary, if exception indicates a temporary error
 - Full ACL support for mail folders
 - Added spam functionaliyt on move/copy to/from spam folder
* Fri Mar 16 2007 - thorben.betten@open-xchange.com
 - Added possiblity to set/deliver ACLs of an IMAP folder
* Thu Mar 15 2007 - thorben.betten@open-xchange.com
 - New method to clean up UploadEvent
 - Changed value 'session.loginFromDB' to 'user.imapLogin'
 - Applied new class to map: User ID <-> IMAP Login
 - Added automatic UploadEvent clean up for registered listeners and manual
   for those classes which do not register but create an UploadEvent anyway
* Thu Mar 15 2007 - tobias.prinz@open-xchange.com
 - fixed bug when setting date in "ContactSwitcher" and related classes.
 - added Importer for CSV Outlook data (using Windows encoding).
 - added tests for CSV Outlook data import.
* Wed Mar 14 2007 - thorben.betten@open-xchange.com
 - FolderQueryCache only caches folder UIDs now; referenced objects are
   fetched from FolderObjectCache
 - Removed unnecessary parameters during folder deletion
 - New class to map an user ID to his IMAP login and vice versa
 - Extended abstract class 'UserStorage' by method "getUserIdByIMAPLogin()"
* Wed Mar 14 2007 - tobias.prinz@open-xchange.com
 - removed required parameter "type" from both importer and exporter
   interface, changed all implementors and tests.
* Wed Mar 14 2007 - marcus.klein@open-xchange.com
  - Added login information to user object.
* Tue Mar 13 2007 - choeger@open-xchange.com
  - Bugfix #6341 StartupScript (open-xchange-groupware) doesnt work good
* Tue Mar 13 2007 - thorben.betten@open-xchange.com
 - New Property to enable/disable setting of SMTP header 'ENVELPE-FROM'
   with user's primary email address
 - Fixed bug #6394: Sending JSON response on error
 - Fixed bug #6417: Preprocessing html content to display links
 #################### ATTENTION ! #####################
  - CONFIG change: New property in 'imap.properties' to enable/disable
   setting of SMTP header 'ENVELOPE-FROM'
#################################################
* Tue Mar 13 2007 - tobias.prinz@open-xchange.com
  - Split the infamous ImportExport servlet into two servlets, changed 
    parameter setup according to rest of the HTTP API, changed Format,
    changed tests accordingly. Changed AJAXServlet to handle uploads
    from servlets.
  - ###CHANGED SERVLETMAPPING.PROPERTIES!### to
    refer to two servlets now instead of one.
  - Changed CSVParser to list lines it could not parse instead from 
    breaking.
* Mon Mar 12 2007 - thorben.betten@open-xchange.com
 - Fixed bug #5840: Slow query splitted into smaller parts that faster
   release held connections
 - Fixed bug #6346: Added lock mechanism to support multiple threads
 - Enhanced error logging in AJPv13Server
 - Fixed bug #6374: Checking if internal date is not null
 - Ensured closing of IMAP connections
 - Avoid unnecessary IMAP connection in MailObject.java, only Transport is
   needed
 - Fixed bug #6244: Enhanced error logging on connect error
 #################### ATTENTION ! #####################
 - CONFIG change: New property in 'imap.properties' to enable/disable
   allowance of folder subsription status
#################################################
- Implemented respect of folder subsription status: GUI can ask for field
  314/"subsribed" to check/set folder's subsription status. 
* Mon Mar 12 2007 - marcus.klein@open-xchange.com
  - Bugfix #6127 Improved error message handling if session is not found.
* Fri Mar 09 2007 - thorben.betten@open-xchange.com
 - Fixed bug with multi-mime-encoded headers
 - Split SearchIterator functionality and OXFolder access methods into two
   classes; no need to further use OXFolderTools (which is completely messed
   up with all stuff)
 - Cache invalidation after user deletion
 - Split huge SQL query into smaller ones
 - Fixed bug with folder query caching
* Thu Mar 08 2007 - thorben.betten@open-xchange.com
 - Fixed bug #5903: Added user and context information if IMAP login fails
 - Setting FolderObject's cache element attributes to lateral=false on initial PUT
* Thu Mar 08 2007 - francisco.laguna@open-xchange.com
   - Fixed bug #6251: Quota inconsistencies on master-slave setup.
   - Fixed bug #6336: Added null-check to QuotaRequest.
* Thu Mar 08 2007 - tobias.prinz@open-xchange.com
  - Fixed problem with ModuleTypeTranslator: Changed from RuntimeException
    to ImportExportException
  - Extended enum for import/export formats to contain constant name: needed
    for Viktor's proposed changed to import/export servlet(s)
* Wed Mar 07 2007 - thorben.betten@open-xchange.com
 - Fixed bug (#6331) with (multi-)mime-encoded message headers
 - Fixed bug #6333: NPE check on forward message
 - Fixed bug #6280: Accept non-semicolon-separated parameters
   in Content-Type header
 - Fixed bug #6332: getFlags() is no more invoked on a message that is
   marked as '\DELETED'
* Tue Mar 06 2007 - thorben.betten@open-xchange.com
 - Fixed bug #6031: Better html2text conversion concerning paragraph
   elements "<p>"
 - Replaced "int foo = new Integer(str).intValue()" constructs with
   invocation of faster routine "int foo = Integer.parseInt(str)": Avoids
   unnecessary instantiation of Integer objects.
 - Changed permission for folder 'Global Address Book' formerly known as
   'Internal Users'
 - Fixed bug #6274: Changed error message as demanded: login to imap server
   failed, please try again later.
 - Removed unnecessary invocation of javax.mail.Message.saveChanges() method
 - Option to enable/disable possibility that an user may edit his own contact object
   in folder 'Global Address Book' aka 'Internal Users'
 - Applied code conventions & usage of new Java5.0 features to package
   'com.openexchange.tools.versit'
#################### ATTENTION ! #####################
 - CONFIG change: New property "ENABLE_INTERNAL_USER_EDIT" added to file
   "foldercache.properties"
#################################################
* Mon Mar 05 2007 - thorben.betten@open-xchange.com
 - Fixed bug #4928	OX Global Address Book vs. Internal Users
   Fixed through changes made in FolderStrings.java. Last thing to do is translation
 - Fixed bug #6201	com.openexchange.ajax.Mail NullPointerException
   Fixed through additional null-check in MailInterfaceImpl.java (line 1704)
 - Fixed bug #6048	Email: Emails without display name aren't sorted by sender (all views)
   Also checking for empty personal in MailComparator when comparing email addresses
 - Fixed bug #6229	Folder.actionGetUpdatedFolders Object was not returned
	Fixed through adding additional catch-clause when instantiating an instance of
	FolderObjectIterator, cause no finally-block can be used to close used resource
 - Fixed bug #6230	Logging when missing default Drafts folder in user mail settings
   Fixed in class MailInterfaceImpl.java (line 472 through 508): Falling back to default
   values defined in UserSettingMail
 - Fixed bug #6297: NPE check
* Tue Feb 27 2007 - tobias.prinz@open-xchange.com
  - Added a handy new sub package to allow for bulk entries of contacts,
    found in ...contacts.helpers. Might seem a bit strange to use, but
    Hibernate does it kinda he same way. You'll get used to it.
  - Added two basic CSV handling classes - one of them a parser. Can be found
    in ...importerexporter.csv - maybe useful for other projects.
  - Further Work on importers and exporters. Okay, we agreed not to mention 
    new development, but since I was already typing and even the 
    system.properties.in has been updated, which usually is worth an e-mail...
* Fri Feb 16 2007 - tobias.prinz@open-xchange.com
  - Work on importers and exporters.
  - Added a handy class to translate between constants used in Types.class
    and in FolderObject.class: ModuleTypeTranslator
* Thu Feb 15 2007 - tobias.prinz@open-xchange.com
  - Work on importers and exporters
* Wed Feb 14 2007 - tobias.prinz@open-xchange.com
  - First version of the import/export infrastructure that one day will enable
    us to import or export data in formats like iCAL, TNEF, vCard and CSV.
  - Later: Added SessionObject as additional param to check access rights properly.
* Tue Feb 13 2007 - marcus.klein@open-xchange.com
  - Initial Import
* Wed Jan 10 2007 - sebastian.kauss@open-xchange.com
 - Bugfix #6455: WebDAV Interface: Unexpected SQL Error!
