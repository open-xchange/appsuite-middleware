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
%define        ox_release 5
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
Provides:      open-xchange-cluster-discovery-mdns = %{version}
Obsoletes:     open-xchange-cluster-discovery-mdns <= %{version}
Provides:      open-xchange-cluster-discovery-static = %{version}
Obsoletes:     open-xchange-cluster-discovery-static <= %{version}

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
mkdir -p %{buildroot}/var/log/open-xchange
mkdir -p %{buildroot}/var/spool/open-xchange/uploads
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
     %{buildroot}/opt/open-xchange/importCSV \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(mail|configdb|server|filestorage)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

%pre
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    . /opt/open-xchange/lib/oxfunctions.sh

    # SoftwareChange_Request-1564
    VALUE="empty"
    if [ -e /opt/open-xchange/bundles/com.openexchange.cluster.discovery.mdns.jar ]; then
        VALUE="multicast"
    elif [ -e /opt/open-xchange/bundles/com.openexchange.cluster.discovery.static.jar ]; then
        VALUE="static"
    fi
    pfile=/opt/open-xchange/etc/hazelcast.properties
    if [ -e $pfile ] && ! ox_exists_property com.openexchange.hazelcast.network.join $pfile; then
        ox_set_property com.openexchange.hazelcast.network.join "$VALUE" $pfile
    fi
fi

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

# SoftwareChange_Request-1297
rm -f /opt/open-xchange/etc/sessioncache.ccf

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

# SoftwareChange_Request-1559
pfile=/opt/open-xchange/etc/mail.properties
VALUE=$(ox_read_property com.openexchange.mail.mailAccessCacheIdleSeconds $pfile)
if [ "$VALUE" == "7" ]; then
    ox_set_property com.openexchange.mail.mailAccessCacheIdleSeconds 4 $pfile
fi

# SoftwareChange_Request-1557
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.maxForwardCount $pfile; then
    ox_set_property com.openexchange.mail.maxForwardCount 8 $pfile
fi

# SoftwareChange_Request-1518
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.hideDetailsForDefaultAccount $pfile; then
    ox_set_property com.openexchange.mail.hideDetailsForDefaultAccount false $pfile
fi

# SoftwareChange_Request-1497
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.logging.enabled $pfile; then
    ox_set_property com.openexchange.hazelcast.logging.enabled true $pfile
fi

# SoftwareChange_Request-1492
pfile=/opt/open-xchange/etc/server.properties
for key in com.openexchange.json.poolEnabled com.openexchange.json.poolSize com.openexchange.json.poolCharArrayLength; do
    if ox_exists_property $key $pfile; then
       ox_remove_property $key $pfile
    fi
done

# SoftwareChange_Request-1483
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.servlet.maxRateTimeWindow $pfile; then
    ox_set_property com.openexchange.servlet.maxRateTimeWindow 300000 $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRate $pfile; then
    ox_set_property com.openexchange.servlet.maxRate 1500 $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRateLenientClients $pfile; then
    ox_set_property com.openexchange.servlet.maxRateLenientClients '"Open-Xchange .NET HTTP Client*", "Open-Xchange USM HTTP Client*", "Jakarta Commons-HttpClient*"' $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRateKeyPartProviders $pfile; then
    ox_set_property com.openexchange.servlet.maxRateKeyPartProviders '' $pfile
fi

# SoftwareChange_Request-1459
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.supportMsisdnAddresses $pfile; then
    ox_set_property com.openexchange.mail.supportMsisdnAddresses false $pfile
fi

# SoftwareChange_Request-1458
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.maxMailSize $pfile; then
    ox_set_property com.openexchange.mail.maxMailSize -1 $pfile
fi

# SoftwareChange_Request-1455
pfile=/opt/open-xchange/etc/sessiond.properties
if ! ox_exists_property com.openexchange.sessiond.asyncPutToSessionStorage $pfile; then
    ox_set_property com.openexchange.sessiond.asyncPutToSessionStorage false $pfile
fi

# SoftwareChange_Request-1448
ox_set_property com.openexchange.push.udp.pushEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.registerDistributionEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.eventDistributionEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.multicastEnabled false /opt/open-xchange/etc/push-udp.properties

# SoftwareChange_Request-1446
pfile=/opt/open-xchange/etc/server.properties
VALUE=$(ox_read_property MAX_UPLOAD_SIZE $pfile)
if [ "$VALUE" == "0" ]; then
    ox_set_property MAX_UPLOAD_SIZE 104857600 $pfile
fi
VALUE=$(ox_read_property com.openexchange.defaultMaxConcurrentAJAXRequests $pfile)
if [ "$VALUE" == "250" ]; then
    ox_set_property com.openexchange.defaultMaxConcurrentAJAXRequests 100 $pfile
fi
VALUE=$(ox_read_property com.openexchange.servlet.maxActiveSessions $pfile)
if [ "$VALUE" == "-1" ]; then
    ox_set_property com.openexchange.servlet.maxActiveSessions 250000 $pfile
fi
pfile=/opt/open-xchange/etc/sessiond.properties
VALUE=$(ox_read_property com.openexchange.sessiond.maxSession $pfile)
if [ "$VALUE" == "5000" ]; then
    ox_set_property com.openexchange.sessiond.maxSession 50000 $pfile
fi
VALUE=$(ox_read_property com.openexchange.sessiond.randomTokenTimeout $pfile)
if [ "$VALUE" == "1M" ]; then
    ox_set_property com.openexchange.sessiond.randomTokenTimeout 30000 $pfile
fi

# SoftwareChange_Request-1445
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.maxOperationTimeout $pfile; then
    ox_set_property com.openexchange.hazelcast.maxOperationTimeout 300000 $pfile
fi

# SoftwareChange_Request-1426
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.log.maxMessageLength $pfile; then
    ox_set_property com.openexchange.log.maxMessageLength -1 $pfile
fi

# SoftwareChange_Request-1365
pfile=/opt/open-xchange/etc/configdb.properties
if ! ox_exists_property com.openexchange.database.replicationMonitor $pfile; then
    ox_set_property com.openexchange.database.replicationMonitor true $pfile
fi

# SoftwareChange_Request-1389
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/foldercache.properties
if ! ox_exists_property com.openexchange.folderstorage.database.preferDisplayName $pfile; then
    ox_set_property com.openexchange.folderstorage.database.preferDisplayName false $pfile
fi

# SoftwareChange_Request-1335
pfile=/opt/open-xchange/etc/paths.perfMap
if ! grep "modules/mail/defaultaddress" $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
modules/mail/defaultaddress > io.ox/mail//defaultaddress
modules/mail/sendaddress > io.ox/mail//sendaddress
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1330
pfile=/opt/open-xchange/etc/mime.types
if ! grep docm $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
application/vnd.ms-word.document.macroEnabled.12 docm
application/vnd.ms-word.template dotm
application/vnd.openxmlformats-officedocument.wordprocessingml.template dotx
application/vnd.ms-powerpoint.presentation.macroEnabled.12 potm
application/vnd.openxmlformats-officedocument.presentationml.template potx
application/vnd.ms-excel.sheet.binary.macroEnabled.12 xlsb
application/vnd.ms-excel.sheet.macroEnabled.12 xlsm
application/vnd.openxmlformats-officedocument.spreadsheetml.sheet xlsx
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1324
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.enableIPv6Support $pfile; then
    ox_set_property com.openexchange.hazelcast.enableIPv6Support false $pfile
fi

# SoftwareChange_Request-1308
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.port $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.port 5701 $pfile
fi
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.portAutoIncrement $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.portAutoIncrement true $pfile
fi
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.outboundPortDefinitions $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.outboundPortDefinitions '' $pfile
fi

# SoftwareChange_Request-1307
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.server.considerXForwards $pfile; then
    ox_set_property com.openexchange.server.considerXForwards false $pfile
fi
if ! ox_exists_property com.openexchange.server.forHeader $pfile; then
    ox_set_property com.openexchange.server.forHeader X-Forwarded-For $pfile
fi
if ! ox_exists_property com.openexchange.server.knownProxies $pfile; then
    ox_set_property com.openexchange.server.knownProxies '' $pfile
fi

# SoftwareChange_Request-1296
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/cache.properties
if ! ox_exists_property com.openexchange.caching.jcs.eventInvalidation $pfile; then
    ox_set_property com.openexchange.caching.jcs.eventInvalidation true $pfile
fi

# SoftwareChange_Request-1302
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/user.properties
if ! ox_exists_property com.openexchange.user.maxClientCount $pfile; then
    ox_set_property com.openexchange.user.maxClientCount -1 $pfile
fi

# SoftwareChange_Request-1275
pfile=/opt/open-xchange/etc/server.properties
if grep -E "com.openexchange.log.propertyNames.*.ajp13." $pfile > /dev/null; then
   ptmp=${pfile}.$$
   sed -e 's;\.ajp13\.;.ajpv13.;g' $pfile > $ptmp
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1252
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/whitelist.properties
if ! grep -E '^html.tag.div.*bgcolor' $pfile > /dev/null; then
    oval=$(ox_read_property html.tag.div ${pfile})
    oval=${oval//\"/}
    ox_set_property html.tag.div \""${oval}bgcolor,"\" $pfile
fi

# SoftwareChange_Request-1247
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.enabled $pfile; then
    ox_set_property com.openexchange.hazelcast.enabled true $pfile
fi

# SoftwareChange_Request-1223
# SoftwareChange_Request-1237
# SoftwareChange_Request-1243
# SoftwareChange_Request-1245
# SoftwareChange_Request-1392
# SoftwareChange_Request-1468
# SoftwareChange_Request-1498
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/ox-scriptconf.sh
jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
jopts=${jopts//\"/}
nopts=$jopts
# -----------------------------------------------------------------------
permval=$(echo $nopts | sed 's;^.*MaxPermSize=\([0-9]*\).*$;\1;')
if [ $permval -lt 256 ]; then
    nopts=$(echo $nopts | sed "s;\(^.*MaxPermSize=\)[0-9]*\(.*$\);\1256\2;")
fi
# -----------------------------------------------------------------------
for opt in "-XX:+DisableExplicitGC" "-server" "-Djava.awt.headless=true" \
    "-XX:+UseConcMarkSweepGC" "-XX:+UseParNewGC" "-XX:CMSInitiatingOccupancyFraction=" \
    "-XX:+UseCMSInitiatingOccupancyOnly" "-XX:NewRatio=" "-XX:+UseTLAB" \
    "-XX:-OmitStackTraceInFastThrow"; do
    if ! echo $nopts | grep -- $opt > /dev/null; then
        if [ "$opt" = "-XX:CMSInitiatingOccupancyFraction=" ]; then
            opt="-XX:CMSInitiatingOccupancyFraction=75"
        elif [ "$opt" = "-XX:NewRatio=" ]; then
            opt="-XX:NewRatio=3"
        fi
        if [ "$opt" == "-XX:+UseConcMarkSweepGC" -o "$opt" == "-XX:+UseParNewGC" -o "$opt" == "-XX:CMSInitiatingOccupancyFraction=75" -o "$opt" == "-XX:+UseCMSInitiatingOccupancyOnly" ]; then
            if ! echo $nopts | grep -- "-XX:+UseParallelGC" > /dev/null && ! echo $nopts | grep -- "-XX:+UseParallelOldGC" > /dev/null; then
                nopts="$nopts $opt"
            fi
        else
            nopts="$nopts $opt"
        fi
    fi
done
# -----------------------------------------------------------------------
for opt in "-XX:+UnlockExperimentalVMOptions" "-XX:+UseG1GC" "-XX:+CMSClassUnloadingEnabled"; do
    if echo $nopts | grep -- $opt > /dev/null; then
        nopts=$(echo $nopts | sed "s;$opt;;")
    fi
done
if [ "$jopts" != "$nopts" ]; then
   ox_set_property JAVA_XTRAOPTS \""$nopts"\" $pfile
fi

# SoftwareChange_Request-1141
pfile=/opt/open-xchange/etc/mime.types
if ! grep font-woff $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
application/font-woff woff
text/cache-manifest appcache
text/javascript js
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi
if grep -E "application\/javascript.*js" $pfile > /dev/null; then
   ptmp=${pfile}.$$
   grep -vE "^application\/.*javascript" $pfile > $ptmp
   cat<<EOF >> $ptmp
application/javascript
application/x-javascript
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1214
# SoftwareChange_Request-1429
# SoftwareChange_Request-1467
pfile=/opt/open-xchange/etc/file-logging.properties
for opt in org.apache.cxf.level com.openexchange.soap.cxf.logger.level org.jaudiotagger.level \
    com.gargoylesoftware.htmlunit.level; do
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

# SoftwareChange_Request-1220
# obsoletes SoftwareChange_Request-1068
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/ox-scriptconf.sh
jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
jopts=${jopts//\"/}
if echo $jopts | grep "osgi.compatibility.bootdelegation" > /dev/null; then
    jopts=$(echo $jopts | sed 's;-Dosgi.compatibility.bootdelegation=true;-Dosgi.compatibility.bootdelegation=false;')
    ox_set_property JAVA_XTRAOPTS \""$jopts"\" $pfile
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
if ox_exists_property contactldap.configuration.path $pfile; then
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
if ! ox_exists_property com.openexchange.import.mapper.path $pfile; then
    ox_set_property com.openexchange.import.mapper.path /opt/open-xchange/importCSV $pfile
fi
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.JavaMailProperties $pfile || grep -E '^com.openexchange.mail.JavaMailProperties.*/' $pfile >/dev/null; then
    ox_set_property com.openexchange.mail.JavaMailProperties javamail.properties $pfile
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

# SoftwareChange_Request-1529
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.server.fullPrimaryKeySupport $pfile; then
    ox_set_property com.openexchange.server.fullPrimaryKeySupport false $pfile
fi

# SoftwareChange_Request-1540
pfile=/opt/open-xchange/etc/permissions.properties
if ! grep "com.openexchange.capability.boring" >/dev/null $pfile; then
    echo -e "\n# Mark this installation as boring, i.e. disable an easter egg\n" >> $pfile
    echo "# com.openexchange.capability.boring=true" >> $pfile
fi

# SoftwareChange_Request-1556
pfile=/opt/open-xchange/etc/excludedupdatetasks.properties
if ! grep "com.openexchange.groupware.tasks.database.TasksModifyCostColumnTask" >/dev/null $pfile; then
    echo -e "\n# v7.4.0 update tasks start here\n" >> $pfile
    echo "# Changes the columns actual_costs and target_costs for tasks from float to NUMERIC(12, 2)" >> $pfile
    echo "!com.openexchange.groupware.tasks.database.TasksModifyCostColumnTask" >> $pfile
fi

# SoftwareChange_Request-1564
[ -e /opt/open-xchange/etc/cluster.properties ] && VALUE=$(ox_read_property com.openexchange.cluster.name /opt/open-xchange/etc/cluster.properties)
TOVALUE=$(ox_read_property com.openexchange.hazelcast.group.name /opt/open-xchange/etc/hazelcast.properties)
if [ -n "$VALUE" -a -z "$TOVALUE" ]; then
    ox_set_property com.openexchange.hazelcast.group.name "$VALUE" /opt/open-xchange/etc/hazelcast.properties
fi
rm -f /opt/open-xchange/etc/cluster.properties
[ -e /opt/open-xchange/etc/static-cluster-discovery.properties ] && VALUE=$(ox_read_property com.openexchange.cluster.discovery.static.nodes /opt/open-xchange/etc/static-cluster-discovery.properties)
TOVALUE=$(ox_read_property com.openexchange.hazelcast.network.join.static.nodes /opt/open-xchange/etc/hazelcast.properties)
if [ -n "$VALUE" -a -z "$TOVALUE" ]; then
    ox_set_property com.openexchange.hazelcast.network.join.static.nodes "$VALUE" /opt/open-xchange/etc/hazelcast.properties
fi
pfile=/opt/open-xchange/etc/hazelcast.properties
OLDNAMES=( com.openexchange.hazelcast.interfaces com.openexchange.hazelcast.mergeFirstRunDelay com.openexchange.hazelcast.mergeRunDelay com.openexchange.hazelcast.networkConfig.port com.openexchange.hazelcast.networkConfig.portAutoIncrement com.openexchange.hazelcast.networkConfig.outboundPortDefinitions com.openexchange.hazelcast.enableIPv6Support )
NEWNAMES=( com.openexchange.hazelcast.network.interfaces com.openexchange.hazelcast.merge.firstRunDelay com.openexchange.hazelcast.merge.runDelay com.openexchange.hazelcast.network.port com.openexchange.hazelcast.network.portAutoIncrement com.openexchange.hazelcast.network.outboundPortDefinitions com.openexchange.hazelcast.network.enableIPv6Support )
DEFAULTS=( 127.0.0.1 120s 120s 5701 true "" false )
for I in $(seq 1 ${#OLDNAMES[@]}); do
    OLDNAME=${OLDNAMES[$I-1]}
    NEWNAME=${NEWNAMES[$I-1]}
    VALUE=$(ox_read_property $OLDNAME $pfile)
    if ox_exists_property $OLDNAME $pfile; then
        ox_remove_property $OLDNAME $pfile
    fi
    if [ -z "$VALUE" ]; then
        VALUE="${DEFAULTS[$I-1]}"
    fi
    if ! ox_exists_property $NEWNAME $pfile; then
        ox_set_property $NEWNAME "$VALUE" $pfile
    fi
done
NEWPROPS=( com.openexchange.hazelcast.jmxDetailed com.openexchange.hazelcast.network.join.multicast.group com.openexchange.hazelcast.network.join.multicast.port com.openexchange.hazelcast.group.password com.openexchange.hazelcast.memcache.enabled com.openexchange.hazelcast.rest.enabled com.openexchange.hazelcast.socket.bindAny )
DEFAULTS=( false 224.2.2.3 54327 'wtV6$VQk8#+3ds!a' false false false )
for I in $(seq 1 ${#NEWPROPS[@]}); do
    NEWPROP=${NEWPROPS[$I-1]}
    DEFAULT=${DEFAULTS[$I-1]}
    if ! ox_exists_property $NEWPROP $pfile; then
        ox_set_property $NEWPROP "$DEFAULT" $pfile
    fi
done

PROTECT="configdb.properties mail.properties management.properties oauth-provider.properties secret.properties secrets sessiond.properties"
for FILE in $PROTECT
do
    ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
done
ox_update_permissions "/opt/open-xchange/etc/ox-scriptconf.sh" root:root 644
ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
ox_update_permissions "/var/spool/open-xchange/uploads" open-xchange:root 750
ox_update_permissions "/var/log/open-xchange" open-xchange:root 750
exit 0

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
%dir %attr(750, open-xchange, root) /var/log/open-xchange
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/uploads
%doc docs/
%doc com.openexchange.server/doc/examples
%doc com.openexchange.server/ChangeLog
%config(noreplace) /opt/open-xchange/etc/contextSets/index.yml
%config(noreplace) /opt/open-xchange/etc/requestwatcher.properties
%config(noreplace) /opt/open-xchange/etc/preview.properties
%config(noreplace) /opt/open-xchange/etc/quota.properties
%config(noreplace) /opt/open-xchange/etc/contextSets/*

%changelog
* Tue Aug 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.0
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-21
* Tue Aug 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-02
* Fri Aug 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.0
* Fri Jul 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-26
* Wed Jul 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-02
* Wed Jul 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Release candidate for 7.2.2 release
* Tue Jun 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-05
* Mon Jun 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-21
* Fri Jun 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Mon Jun 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue May 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-08
* Fri May 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-17
* Sun Apr 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-25
* Mon Apr 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.1 release
* Thu Apr 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-30
* Wed Apr 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09
* Mon Apr 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.2.0
* Mon Mar 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-18
* Fri Mar 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-10
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-12-27
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for EDP drop #6
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
Third release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for next EDP drop
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
