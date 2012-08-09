
Name:          open-xchange
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 6
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Source1:       open-xchange.init
Summary:       Open-Xchange Backend
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-authentication
Requires:      open-xchange-authorization
Requires:      open-xchange-mailstore
Requires:      open-xchange-httpservice
Requires:      open-xchange-smtp >= @OXVERSION@

%description
This package only contains the dependencies to install a working Open-Xchange 7 backend system.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

mkdir -p %{buildroot}/etc/init.d
mkdir -p %{buildroot}/sbin

install -m 755 %{SOURCE1} %{buildroot}/etc/init.d/open-xchange
ln -sf ../etc/init.d/open-xchange %{buildroot}/sbin/rcopen-xchange

mkdir -p %{buildroot}/var/log/open-xchange
mkdir -m 750 -p %{buildroot}/var/spool/open-xchange/uploads

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    ##
    ## start update from < 6.21
    ##
    GWCONFFILES="ajp.properties attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties imap.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties user.properties whitelist.properties"
    COCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
    for FILE in ${GWCONFFILES}; do
	ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
    done
    for FILE in ${COCONFFILES}; do
	ox_move_config_file /opt/open-xchange/etc/common /opt/open-xchange/etc $FILE
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
    ##
    ## end update from < 6.21
    ##
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir %attr(750,open-xchange,root) /var/log/open-xchange
%dir %attr(750,open-xchange,root) /var/spool/open-xchange/uploads
/etc/init.d/open-xchange
/sbin/rcopen-xchange

%changelog
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
* Wed Feb 01 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
