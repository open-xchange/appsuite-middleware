
Name:          open-xchange
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
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
    COMMONCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
    for FILE in ${COMMONCONFFILES}; do
	if [ -e /opt/open-xchange/etc/common/${FILE} ]; then
	    mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
	    mv /opt/open-xchange/etc/common/${FILE} /opt/open-xchange/etc/${FILE}
	fi
    done

    GWCONFFILES="ajp.properties attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties imap.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties user.properties whitelist.properties"
    for FILE in ${GWCONFFILES}; do
	if [ -e /opt/open-xchange/etc/groupware/${FILE} ]; then
	    mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
	    mv /opt/open-xchange/etc/groupware/${FILE} /opt/open-xchange/etc/${FILE}
	fi
    done

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
