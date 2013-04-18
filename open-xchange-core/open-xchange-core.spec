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
%define        ox_release 16
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
CONFFILES="management.properties templating.properties mail-push.properties filestorage.properties folderjson.properties messaging.properties publications.properties secret.properties secrets threadpool.properties settings/themes.properties settings/ui.properties meta/ui.yml"
for FILE in $CONFFILES; do
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
done
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    ox_update_permissions "/var/log/open-xchange" open-xchange:root 750
    ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
    PROTECT="configdb.properties mail.properties management.properties oauth-provider.properties secret.properties secrets sessiond.properties"
    for FILE in $PROTECT
    do
        ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
    done
    ox_update_permissions "/opt/open-xchange/etc/ox-scriptconf.sh" root:root 644
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
%doc docs/
%doc com.openexchange.server/doc/examples
%doc com.openexchange.server/ChangeLog

%changelog
* Thu Apr 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
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
* Mon Nov 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Thu Nov 08 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Mon Nov 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Tue Oct 30 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-29
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
