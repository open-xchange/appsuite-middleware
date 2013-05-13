%define	       configfiles     configfiles.list

Name:          open-xchange-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-log4j
BuildRequires: open-xchange-xerces
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 19
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/            
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The essential core of an Open-Xchange backend
Requires:      open-xchange-osgi >= @OXVERSION@
Requires:      open-xchange-xerces
Provides:      open-xchange-cache = %{version}
Obsoletes:     open-xchange-cache <= %{version}
Provides:      open-xchange-calendar = %{version}
Obsoletes:     open-xchange-calendar <= %{version}
Provides:      open-xchange-charset = %{version}
Obsoletes:     open-xchange-charset <= %{version}
Provides:      open-xchange-common = %{version}
Obsoletes:     open-xchange-common <= %{version}
Provides:      open-xchange-config-cascade = %{version}
Obsoletes:     open-xchange-config-cascade <= %{version}
Provides:      open-xchange-config-cascade-context = %{version}
Obsoletes:     open-xchange-config-cascade-context <= %{version}
Provides:      open-xchange-config-cascade-user = %{version}
Obsoletes:     open-xchange-config-cascade-user <= %{version}
Provides:      open-xchange-configread = %{version}
Obsoletes:     open-xchange-configread <= %{version}
Provides:      open-xchange-contactcollector = %{version}
Obsoletes:     open-xchange-contactcollector <= %{version}
Provides:      open-xchange-control = %{version}
Obsoletes:     open-xchange-control <= %{version}
Provides:      open-xchange-conversion = %{version}
Obsoletes:     open-xchange-conversion <= %{version}
Provides:      open-xchange-conversion-engine = %{version}
Obsoletes:     open-xchange-conversion-engine <= %{version}
Provides:      open-xchange-conversion-servlet = %{version}
Obsoletes:     open-xchange-conversion-servlet <= %{version}
Provides:      open-xchange-crypto = %{version}
Obsoletes:     open-xchange-crypto <= %{version}
Provides:      open-xchange-data-conversion-ical4j = %{version}
Obsoletes:     open-xchange-data-conversion-ical4j <= %{version}
Provides:      open-xchange-dataretention = %{version}
Obsoletes:     open-xchange-dataretention <= %{version}
Provides:      open-xchange-genconf = %{version}
Obsoletes:     open-xchange-genconf <= %{version}
Provides:      open-xchange-genconf-mysql = %{version}
Obsoletes:     open-xchange-genconf-mysql <= %{version}
Provides:      open-xchange-file-storage = %{version}
Obsoletes:     open-xchange-file-storage <= %{version}
Provides:      open-xchange-file-storage-composition = %{version}
Obsoletes:     open-xchange-file-storage-composition <= %{version}
Provides:      open-xchange-file-storage-config = %{version}
Obsoletes:     open-xchange-file-storage-config <= %{version}
Provides:      open-xchange-file-storage-generic = %{version}
Obsoletes:     open-xchange-file-storage-generic <= %{version}
Provides:      open-xchange-file-storage-infostore = %{version}
Obsoletes:     open-xchange-file-storage-infostore <= %{version}
Provides:      open-xchange-file-storage-json = %{version}
Obsoletes:     open-xchange-file-storage-json <= %{version}
Provides:      open-xchange-folder-json = %{version}
Obsoletes:     open-xchange-folder-json <= %{version}
Provides:      open-xchange-frontend-uwa = %{version}
Obsoletes:     open-xchange-frontend-uwa <= %{version}
Provides:      open-xchange-frontend-uwa-json = %{version}
Obsoletes:     open-xchange-frontend-uwa-json <= %{version}
Provides:      open-xchange-global = %{version}
Obsoletes:     open-xchange-global <= %{version}
Provides:      open-xchange-html = %{version}
Obsoletes:     open-xchange-html <= %{version}
Provides:      open-xchange-i18n = %{version}
Obsoletes:     open-xchange-i18n <= %{version}
Provides:      open-xchange-itip-json = %{version}
Obsoletes:     open-xchange-itip-json <= %{version}
Provides:      open-xchange-jcharset = %{version}
Obsoletes:     open-xchange-jcharset <= %{version}
Provides:      open-xchange-logging = %{version}
Obsoletes:     open-xchange-logging <= %{version}
Provides:      open-xchange-management = %{version}
Obsoletes:     open-xchange-management <= %{version}
Provides:      open-xchange-modules-json = %{version}
Obsoletes:     open-xchange-modules-json <= %{version}
Provides:      open-xchange-modules-model = %{version}
Obsoletes:     open-xchange-modules-model <= %{version}
Provides:      open-xchange-modules-storage = %{version}
Obsoletes:     open-xchange-modules-storage <= %{version}
Provides:      open-xchange-monitoring = %{version}
Obsoletes:     open-xchange-monitoring <= %{version}
Provides:      open-xchange-proxy = %{version}
Obsoletes:     open-xchange-proxy <= %{version}
Provides:      open-xchange-proxy-servlet = %{version}
Obsoletes:     open-xchange-proxy-servlet <= %{version}
Provides:      open-xchange-publish-basic = %{version}
Obsoletes:     open-xchange-publish-basic <= %{version}
Provides:      open-xchange-publish-infostore-online = %{version}
Obsoletes:     open-xchange-publish-infostore-online <= %{version}
Provides:      open-xchange-push = %{version}
Obsoletes:     open-xchange-push <= %{version}
Provides:      open-xchange-push-udp = %{version}
Obsoletes:     open-xchange-push-udp <= %{version}
Provides:      open-xchange-secret = %{version}
Obsoletes:     open-xchange-secret <= %{version}
Provides:      open-xchange-secret-recovery = %{version}
Obsoletes:     open-xchange-secret-recovery <= %{version}
Provides:      open-xchange-secret-recovery-json = %{version}
Obsoletes:     open-xchange-secret-recovery-json <= %{version}
Provides:      open-xchange-secret-recovery-mail = %{version}
Obsoletes:     open-xchange-secret-recovery-mail <= %{version}
Provides:      open-xchange-server = %{version}
Obsoletes:     open-xchange-server <= %{version}
Provides:      open-xchange-sessiond = %{version}
Obsoletes:     open-xchange-sessiond <= %{version}
Provides:      open-xchange-settings-extensions = %{version}
Obsoletes:     open-xchange-settings-extensions <= %{version}
Provides:      open-xchange-sql = %{version}
Obsoletes:     open-xchange-sql <= %{version}
Provides:      open-xchange-templating = %{version}
Obsoletes:     open-xchange-templating <= %{version}
Provides:      open-xchange-templating-base = %{version}
Obsoletes:     open-xchange-templating-base <= %{version}
Provides:      open-xchange-threadpool = %{version}
Obsoletes:     open-xchange-threadpool <= %{version}
Provides:      open-xchange-tx = %{version}
Obsoletes:     open-xchange-tx <= %{version}
Provides:      open-xchange-user-json = %{version}
Obsoletes:     open-xchange-user-json <= %{version}
Provides:      open-xchange-xml = %{version}
Obsoletes:     open-xchange-xml <= %{version}
Provides:      open-xchange-passwordchange-servlet = %{version}
Obsoletes:     open-xchange-passwordchange-servlet <= %{version}
Provides:      open-xchange-file-storage-webdav = %{version}
Obsoletes:     open-xchange-file-storage-webdav <= %{version}

%description
This package installs all essential bundles that are necessary to get a working backend installation. This are the bundles for the main
modules of Open-Xchange: Mail, Calendar, Contacts, Tasks and InfoStore. Additionally the following functionalities are installed with this
package:
* the main caching system using the Java Caching System (JCS)
* the config cascade allowing administrators to selectively override configuration parameters on context and user level
* the contact collector storing every contact of read or written emails in a special collected contacts folder
* the conversion engine converting vCard or iCal email attachments to contacts or appointments
* the import and export module to import or export complete contact or appointment folders
* the iMIP implementation to handle invitations with participants through emails
* auto configuration for external email accounts
* encrypted storing of passwords for integrated social accounts
* and a lot more

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
     %{buildroot}/opt/open-xchange/importCSV \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(mail|configdb|server|filestorage)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}


%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_move_config_file /opt/open-xchange/etc/common /opt/open-xchange/etc i18n.properties
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc push.properties push-udp.properties
CONFFILES="management.properties templating.properties mail-push.properties filestorage.properties folderjson.properties messaging.properties publications.properties secret.properties secrets threadpool.properties settings/themes.properties settings/ui.properties meta/ui.yml attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties user.properties whitelist.properties folder-reserved-names"
for FILE in $CONFFILES; do
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
done
COCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
for FILE in ${COCONFFILES}; do
    ox_move_config_file /opt/open-xchange/etc/common /opt/open-xchange/etc $FILE
done

# SoftwareChange_Request-1094
rm -f /opt/open-xchange/etc/groupware/mailjsoncache.properties
# SoftwareChange_Request-1091
rm -f /opt/open-xchange/etc/groupware/TidyConfiguration.properties
rm -f /opt/open-xchange/etc/groupware/TidyMessages.properties

pfile=/opt/open-xchange/etc/ox-scriptconf.sh
if grep COMMONPROPERTIESDIR $pfile >/dev/null; then
    ox_remove_property COMMONPROPERTIESDIR $pfile
    # without original values, we're lost...
    if [ -e ${pfile}.rpmnew ]; then
       CHECKPROPS="LIBPATH PROPERTIESDIR LOGGINGPROPERTIES OSGIPATH"
       grep JAVA_OXCMD_OPTS $pfile > /dev/null || CHECKPROPS="$CHECKPROPS JAVA_OXCMD_OPTS" && true
       for prop in $CHECKPROPS; do
           oval=$(ox_read_property $prop ${pfile}.rpmnew)
           if [ -n "$oval" ]; then
          ox_set_property $prop "$oval" $pfile
           fi
       done
    fi
fi

# SoftwareChange_Request-1214
pfile=/opt/open-xchange/etc/file-logging.properties
for opt in org.apache.cxf.level com.openexchange.soap.cxf.logger.level; do
    if ! ox_exists_property $opt $pfile; then
       ox_set_property $opt WARNING $pfile
    fi
done

# SoftwareChange_Request-1184
pfile=/opt/open-xchange/etc/file-logging.properties
if ! ox_exists_property com.hazelcast.level $pfile; then
   ox_set_property com.hazelcast.level SEVERE $pfile
fi

# SoftwareChange_Request-1212
pfile=/opt/open-xchange/etc/foldercache.properties
if ! ox_exists_property com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore $pfile; then
    ox_set_property com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore true $pfile
fi

# SoftwareChange_Request-1196
pfile=/opt/open-xchange/etc/import.properties
if ! ox_exists_property com.openexchange.import.ical.limit $pfile; then
    ox_set_property com.openexchange.import.ical.limit 10000 $pfile
fi

# SoftwareChange_Request-1068
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/ox-scriptconf.sh
jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
jopts=${jopts//\"/}
if ! echo $jopts | grep "osgi.compatibility.bootdelegation" > /dev/null; then
    ox_set_property JAVA_XTRAOPTS \""$jopts -Dosgi.compatibility.bootdelegation=true"\" $pfile
fi

# SoftwareChange_Request-1135
pfile=/opt/open-xchange/etc/contact.properties
for key in scale_images scale_image_width scale_image_height; do
    if ox_exists_property $key $pfile; then
       ox_remove_property $key $pfile
    fi
done

# SoftwareChange_Request-1124
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.ajax.response.includeStackTraceOnError $pfile; then
    ox_set_property com.openexchange.ajax.response.includeStackTraceOnError false $pfile
fi

# SoftwareChange_Request-1117
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.webdav.disabled $pfile; then
    ox_set_property com.openexchange.webdav.disabled false $pfile
fi

# SoftwareChange_Request-1105
pfile=/opt/open-xchange/etc/cache.ccf
ptmp=${pfile}.$$
if grep -E "^jcs.region.OXIMAPConCache" $pfile > /dev/null; then
    grep -vE "^jcs.region.OXIMAPConCache" $pfile > $ptmp
    if [ -s $ptmp ]; then
        cp $ptmp $pfile
    fi
    rm -f $ptmp
fi
# SoftwareChange_Request-1028
pfile=/opt/open-xchange/etc/contact.properties
if ! ox_exists_property com.openexchange.carddav.tree $pfile; then
    ox_set_property com.openexchange.carddav.tree "0" $pfile
fi
if ! ox_exists_property com.openexchange.carddav.combinedRequestTimeout $pfile; then
    ox_set_property com.openexchange.carddav.combinedRequestTimeout "20000" $pfile
fi
if ! ox_exists_property com.openexchange.carddav.exposedCollections $pfile; then
    ox_set_property com.openexchange.carddav.exposedCollections "0" $pfile
fi
# SoftwareChange_Request-1091
pfile=/opt/open-xchange/etc/contact.properties
if ! ox_exists_property contactldap.configuration.path $pfile; then
    ox_remove_property contactldap.configuration.path $pfile
fi

# SoftwareChange_Request-1101
pfile=/opt/open-xchange/etc/configdb.properties
if ox_exists_property writeOnly $pfile; then
    ox_remove_property writeOnly $pfile
fi
# SoftwareChange_Request-1091
if ox_exists_property useSeparateWrite $pfile; then
    ox_remove_property useSeparateWrite $pfile
fi

# SoftwareChange_Request-1091
pfile=/opt/open-xchange/etc/system.properties
for prop in Calendar Infostore Attachment Notification ServletMappingDir CONFIGPATH \
    AJPPROPERTIES IMPORTEREXPORTER LDAPPROPERTIES EVENTPROPERTIES PUSHPROPERTIES \
    UPDATETASKSCFG HTMLEntities MailCacheConfig TidyMessages TidyConfiguration Whitelist; do
    if ox_exists_property $prop $pfile; then
       ox_remove_property $prop $pfile
    fi
done
if grep -E '^com.openexchange.caching.configfile.*/' $pfile >/dev/null; then
    ox_set_property com.openexchange.caching.configfile cache.ccf $pfile
fi
if ox_exists_property MimeTypeFile $pfile; then
    ox_set_property MimeTypeFileName mime.types $pfile
    ox_remove_property MimeTypeFile $pfile
fi
pfile=/opt/open-xchange/etc/import.properties
if ox_exists_property com.openexchange.import.mapper.path $pfile; then
    ox_remove_property com.openexchange.import.mapper.path $pfile
fi
pfile=/opt/open-xchange/etc/mail.properties
if ox_exists_property com.openexchange.mail.JavaMailProperties $pfile; then
    ox_remove_property com.openexchange.mail.JavaMailProperties $pfile
fi
pfile=/opt/open-xchange/etc/sessiond.properties
if ox_exists_property com.openexchange.sessiond.sessionCacheConfig $pfile; then
    ox_remove_property com.openexchange.sessiond.sessionCacheConfig $pfile
fi

# SoftwareChange_Request-1024
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.IPMaskV4 $pfile; then
    ox_set_property com.openexchange.IPMaskV4 "" $pfile
fi
if ! ox_exists_property com.openexchange.IPMaskV6 $pfile; then
    ox_set_property com.openexchange.IPMaskV6 "" $pfile
fi

# SoftwareChange_Request-1027
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.dispatcher.prefix $pfile; then
    ox_set_property com.openexchange.dispatcher.prefix "/ajax/" $pfile
fi


# SoftwareChange_Request-1167
pfile=/opt/open-xchange/etc/contact.properties
if ! ox_exists_property "com.openexchange.contact.scaleVCardImages" $pfile; then
   ox_set_property "com.openexchange.contact.scaleVCardImages" "200x200" $pfile
fi

# SoftwareChange_Request-1148
pfile=/opt/open-xchange/etc/whitelist.properties
if ! ox_exists_property "html.style.word-break" $pfile; then
   ox_set_property "html.style.word-break" '"break-all"' $pfile
fi
if ! ox_exists_property "html.style.word-wrap" $pfile; then
   ox_set_property "html.style.word-wrap" '"break-word"' $pfile
fi

# SoftwareChange_Request-1125
pfile=/opt/open-xchange/etc/contactcollector.properties
if ! ox_exists_property com.openexchange.contactcollector.folder.deleteDenied $pfile; then
   ox_set_property com.openexchange.contactcollector.folder.deleteDenied false $pfile
fi

PROTECT="configdb.properties mail.properties management.properties oauth-provider.properties secret.properties secrets sessiond.properties"
ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
for FILE in $PROTECT
do
    ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
done
ox_update_permissions "/opt/open-xchange/etc/ox-scriptconf.sh" root:root 644

%clean
%{__rm} -rf %{buildroot}


%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/i18n/
%dir /opt/open-xchange/importCSV/
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/oxfunctions.sh
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/osgi/config.ini.template
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/templates/
/opt/open-xchange/templates/*
%doc docs/
%doc com.openexchange.server/doc/examples
%doc com.openexchange.server/ChangeLog

%changelog
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Wed Apr 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-18
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-10
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-12-27
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Mon Nov 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Thu Nov 08 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Tue Nov 06 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Mon Nov 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Fri Nov 02 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Tue Oct 30 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-29
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Mon Apr 16 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
* Wed Apr 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #0
* Mon Oct 17 2011 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
