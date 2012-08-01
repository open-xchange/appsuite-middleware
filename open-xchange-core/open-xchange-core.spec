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
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/            
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The main bundles for the Open-Xchange collaboration suite
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


%description
The main bundles for the Open-Xchange collaboration suite.

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
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    ##
    ## start update from < 6.21
    ##
    GWCONFFILES="filestorage.properties folderjson.properties mail-push.properties messaging.properties publications.properties push.properties secret.properties secrets threadpool.properties meta/ui.yml settings/themes.properties settings/ui.properties"
    COCONFFILES="i18n.properties"
    for FILE in ${GWCONFFILES}; do
        if [ -e /opt/open-xchange/etc/groupware/${FILE} ]; then
            mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
            mv /opt/open-xchange/etc/groupware/${FILE} /opt/open-xchange/etc/${FILE}
        fi
    done
    for FILE in ${COCONFFILES}; do
        if [ -e /opt/open-xchange/etc/common/${FILE} ]; then
            mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
            mv /opt/open-xchange/etc/common/${FILE} /opt/open-xchange/etc/${FILE}
        fi
    done

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
    # -----------------------------------------------------------------------
    rm -f /opt/open-xchange/etc/groupware/TidyConfiguration.properties
    rm -f /opt/open-xchange/etc/groupware/TidyMessages.properties
    pfile=/opt/open-xchange/etc/configdb.properties
    ox_remove_property useSeparateWrite $pfile
    pfile=/opt/open-xchange/etc/contact.properties
    ox_remove_property contactldap.configuration.path $pfile
    pfile=/opt/open-xchange/etc/import.properties
    ox_remove_property com.openexchange.import.mapper.path $pfile
    pfile=/opt/open-xchange/etc/mail.properties
    ox_remove_property com.openexchange.mail.JavaMailProperties $pfile
    pfile=/opt/open-xchange/etc/sessiond.properties
    ox_remove_property com.openexchange.sessiond.sessionCacheConfig $pfile
    pfile=/opt/open-xchange/etc/system.properties
    ox_remove_property Calendar $pfile
    ox_remove_property Infostore $pfile
    ox_remove_property Attachment $pfile
    ox_remove_property Notification $pfile
    ox_remove_property ServletMappingDir $pfile
    ox_remove_property CONFIGPATH $pfile
    ox_remove_property AJPPROPERTIES $pfile
    ox_remove_property IMPORTEREXPORTER $pfile
    ox_remove_property LDAPPROPERTIES $pfile
    ox_remove_property EVENTPROPERTIES $pfile
    ox_remove_property PUSHPROPERTIES $pfile
    ox_remove_property UPDATETASKSCFG $pfile
    ox_remove_property HTMLEntities $pfile
    ox_remove_property MailCacheConfig $pfile
    ox_remove_property TidyMessages $pfile
    ox_remove_property TidyConfiguration $pfile
    ox_remove_property Whitelist $pfile
    if grep -E '^com.openexchange.caching.configfile.*/' $pfile >/dev/null; then
        ox_set_property com.openexchange.caching.configfile cache.ccf $pfile
    fi
    if ox_exists_property MimeTypeFile $pfile; then
        ox_set_property MimeTypeFileName mime.types $pfile
        ox_remove_property MimeTypeFile $pfile
    fi
    # SoftwareChange_Request-1094
    # -----------------------------------------------------------------------
    rm -f /opt/open-xchange/etc/groupware/mailjsoncache.properties

    # SoftwareChange_Request-1101
    pfile=/opt/open-xchange/etc/configdb.properties
    if ox_exists_property writeOnly $pfile; then
        ox_remove_property writeOnly $pfile
    fi
    ##
    ## end update from < 6.21
    ##
    ox_update_permissions "/var/log/open-xchange" open-xchange:root 750
    ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
    ox_update_permissions "/opt/open-xchange/etc/mail.properties" root:open-xchange 640
    ox_update_permissions "/opt/open-xchange/etc/configdb.properties" root:open-xchange 640
    ox_update_permissions "/opt/open-xchange/etc/server.properties" root:open-xchange 640
    ox_update_permissions "/opt/open-xchange/etc/filestorage.properties" root:open-xchange 640
fi

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

%changelog
* Tue Apr 17 2012 Sonja Krause-Harder  <sonja.krause-harder@open-xchange.com>
Internal release build for EDP drop #1
