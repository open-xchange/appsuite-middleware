
Name:          open-xchange
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 13
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Source1:       open-xchange.init
Summary:       The Open-Xchange backend
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-authentication
Requires:      open-xchange-authorization
Requires:      open-xchange-mailstore
Requires:      open-xchange-httpservice
Requires:      open-xchange-theme-default
Requires:      open-xchange-smtp >= @OXVERSION@

%description
This package provides the dependencies to install a working Open-Xchange backend system. By installing this package a minimal backend is
installed. Additionally this package provides the init script for starting the backend on system boot.

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
. /opt/open-xchange/lib/oxfunctions.sh
GWCONFFILES="ajp.properties attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties imap.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties user.properties whitelist.properties folder-reserved-names"
COCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
for FILE in ${GWCONFFILES}; do
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
done
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

if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # SoftwareChange_Request-1142
    pfile=/opt/open-xchange/etc/imap.properties
    if ! ox_exists_property com.openexchange.imap.umlautFilterThreshold $pfile; then
        ox_set_property com.openexchange.imap.umlautFilterThreshold 50 $pfile
    fi

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
* Wed Aug 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-03
* Tue Jun 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
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
