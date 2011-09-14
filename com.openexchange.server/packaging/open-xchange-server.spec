
# norootforbuild
%define		configfiles	configfiles.list

Name:           open-xchange-server
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant open-xchange-common >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-conversion >= @OXVERSION@ open-xchange-monitoring >= @OXVERSION@ open-xchange-secret >= @OXVERSION@ open-xchange-cache >= @OXVERSION@ open-xchange-xml >= @OXVERSION@ open-xchange-dataretention >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-publish >= @OXVERSION@ open-xchange-push >= @OXVERSION@ open-xchange-messaging >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-html >= @OXVERSION@ open-xchange-file-storage >= @OXVERSION@ open-xchange-tx >= @OXVERSION@ open-xchange-file-storage-composition >= @OXVERSION@ open-xchange-crypto >= @OXVERSION@
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
Version:	@OXVERSION@
%define		ox_release 20
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Server Bundle
Requires:       open-xchange-common >= @OXVERSION@ open-xchange-configread >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-conversion >= @OXVERSION@ open-xchange-monitoring >= @OXVERSION@ open-xchange-secret >= @OXVERSION@  open-xchange-management >= @OXVERSION@ open-xchange-cache >= @OXVERSION@ open-xchange-xml >= @OXVERSION@ open-xchange-dataretention >= @OXVERSION@ open-xchange-threadpool >= @OXVERSION@ open-xchange-publish >= @OXVERSION@ open-xchange-push >= @OXVERSION@ open-xchange-messaging >= @OXVERSION@ open-xchange-genconf >= @OXVERSION@ open-xchange-html >= @OXVERSION@ open-xchange-file-storage >= @OXVERSION@ open-xchange-tx >= @OXVERSION@ open-xchange-crypto >= @OXVERSION@
%if 0%{?suse_version} && 0%{?sles_version} < 11
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
%if 0%{?sles_version} >= 11
# SLES11
Requires:  java-1_6_0-ibm
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
# RHEL5 removed sun-java5, but some might still use it, so just depend on sun-java
Requires:  java-sun
%endif
#

%package -n	open-xchange
Group:          Applications/Productivity
Summary:	Open-Xchange server scripts and configuration
Requires:	open-xchange-authentication >= @OXVERSION@ open-xchange-charset >= @OXVERSION@ open-xchange-conversion-engine >= @OXVERSION@ open-xchange-conversion-servlet >= @OXVERSION@ open-xchange-contactcollector >= @OXVERSION@ open-xchange-i18n >= @OXVERSION@ open-xchange-mailstore >= @OXVERSION@ open-xchange-jcharset >= @OXVERSION@ open-xchange-push-udp >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-calendar >= @OXVERSION@ open-xchange-sessiond >= @OXVERSION@ open-xchange-smtp >= @OXVERSION@ open-xchange-spamhandler >= @OXVERSION@ open-xchange-user-json >= @OXVERSION@ open-xchange-settings-extensions >= @OXVERSION@ open-xchange-theme-default >= @OXVERSION@ open-xchange-folder-json >= @OXVERSION@ open-xchange-proxy-servlet >= @OXVERSION@ open-xchange-secret-recovery-json >= @OXVERSION@ open-xchange-secret-recovery-mail >= @OXVERSION@ open-xchange-tx >= @OXVERSION@ open-xchange-file-storage-json >= @OXVERSION@ open-xchange-file-storage-infostore >= @OXVERSION@ open-xchange-file-storage-config >= @OXVERSION@ open-xchange-authorization >= @OXVERSION@ open-xchange-logging >= @OXVERSION@ open-xchange-httpservice >= @OXVERSION@ open-xchange-config-cascade-context >= @OXVERSION@ open-xchange-config-cascade-user >= @OXVERSION@ open-xchange-modules-json >= @OXVERSION@ open-xchange-modules-model >= @OXVERSION@ open-xchange-modules-storage >= @OXVERSION@ open-xchange-frontend-uwa >= @OXVERSION@ open-xchange-frontend-uwa-json >= @OXVERSION@ open-xchange-publish-infostore-online >= @OXVERSION@
%if 0%{?suse_version}
Requires: mysql-client >= 5.0.0
%endif
%if 0%{?rhel_version}
Requires: mysql >= 5.0.0
# for the correct operation of the init scripts
Requires:  redhat-lsb
%endif
%if 0%{?centos_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif

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

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb installJars
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb installExceptJars installConfig

mkdir -p %{buildroot}/var/log/open-xchange
mkdir -m 750 -p %{buildroot}/var/spool/open-xchange/uploads

# generate list of config files for config package
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc/groupware \
     %{buildroot}/opt/open-xchange/etc/groupware/servletmappings \
     %{buildroot}/opt/open-xchange/etc/common \
	-maxdepth 1 -type f \
	-not -name oxfunctions.sh \
	-printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(mail|configdb|server)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

ln -sf ../etc/init.d/open-xchange-groupware %{buildroot}/sbin/rcopen-xchange-groupware

%clean
%{__rm} -rf %{buildroot}


%post -n open-xchange


if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # prevent bash from expanding, see bug 13316
   GLOBIGNORE='*'

   # SoftwareChange_Request-798
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/login.properties
   if ! ox_exists_property com.openexchange.ajax.login.insecure $pfile; then
       ox_set_property com.openexchange.ajax.login.insecure "false" $pfile
   fi

   # SoftwareChange_Request-797
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.servlet.echoHeaderName $pfile; then
       ox_set_property com.openexchange.servlet.echoHeaderName "X-Echo-Header" $pfile
   fi

   # SoftwareChange_Request-791
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/common/excludedupdatetasks.properties
   if ! grep "com.openexchange.groupware.update.tasks.CorrectOrganizerInAppointments" >/dev/null $pfile; then
       echo "# Corrects the organizer in appointments. When exporting iCal and importing it again the organizer gets value 'null' instead of SQL NULL" >> $pfile
       echo "# This task corrects this." >> $pfile
       echo "!com.openexchange.groupware.update.tasks.CorrectOrganizerInAppointments" >> $pfile
   fi
 
   # SoftwareChange_Request-788
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/system.properties
   if ! ox_exists_property com.openexchange.config.cascade.scopes $pfile; then
       ox_set_property com.openexchange.config.cascade.scopes "user, context, contextSets, server" $pfile
   fi

   # SoftwareChange_Request-784
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.maxNumExternalConnections $pfile; then
       ox_set_property com.openexchange.imap.maxNumExternalConnections "imap.gmail.com:2,imap.googlemail.com:2" $pfile
   fi

   # SoftwareChange_Request-766
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.maxNumExternalConnections $pfile; then
       ox_set_property com.openexchange.imap.maxNumExternalConnections '""' $pfile
   fi
 
   # SoftwareChange_Request-774
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/cache.ccf
   val=0$(ox_read_property jcs.region.Context.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 10000 ]; then
       ox_set_property jcs.region.Context.cacheattributes.MaxObjects 10000 $pfile
   fi

   # SoftwareChange_Request-755
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.notifyRecent $pfile; then
       ox_set_property com.openexchange.imap.notifyRecent "false" $pfile
   fi
   if ! ox_exists_property com.openexchange.imap.notifyFrequencySeconds $pfile; then
       ox_set_property com.openexchange.imap.notifyFrequencySeconds "300" $pfile
   fi
   if ! ox_exists_property com.openexchange.imap.notifyFullNames $pfile; then
       ox_set_property com.openexchange.imap.notifyFullNames "INBOX" $pfile
   fi

   # SoftwareChange_Request-754
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/whitelist.properties
   for tag in html.tag.sub html.tag.sup; do
       if ! ox_exists_property $tag $pfile; then
           ox_set_property $tag '""' $pfile
       fi
   done

   # SoftwareChange_Request-748
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/common/excludedupdatetasks.properties
   if ! grep -E "^com.openexchange.groupware.update.tasks.DeleteOldYahooSubscriptions" >/dev/null $pfile; then
      echo "# Remove crawler-style yahoo subscriptions. ENABLE THIS IF YOU WANT TO USE open-xchange-subscribe-yahoo. DISABLE IT OTHERWISE!" >> $pfile
      echo "com.openexchange.groupware.update.tasks.DeleteOldYahooSubscriptions" >> $pfile
   fi

   # SoftwareChange_Request-711
   # obsoleted by SoftwareChange_Request-766
   # -----------------------------------------------------------------------
   #pfile=/opt/open-xchange/etc/groupware/imap.properties
   #if ! ox_exists_property com.openexchange.imap.maxNumExternalConnections $pfile; then
   #   ox_set_property com.openexchange.imap.maxNumExternalConnections "imap.googlemail.com:4," $pfile
   #fi

   # SoftwareChange_Request-705
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ox_exists_property com.openexchange.cookie.forceHTTPS $pfile; then
      oval=$(ox_read_property com.openexchange.cookie.forceHTTPS $pfile)
      ox_remove_property com.openexchange.cookie.forceHTTPS $pfile
   fi
   if ! ox_exists_property com.openexchange.forceHTTPS $pfile; then
      if [ -n "$oval" ]; then
	  val=$oval
      else
	  val=false
      fi
      ox_set_property com.openexchange.forceHTTPS $val $pfile
   fi

   # SoftwareChange_Request-647 (obsoleted by 705)
   # -----------------------------------------------------------------------
   #pfile=/opt/open-xchange/etc/groupware/server.properties
   #if ! ox_exists_property com.openexchange.cookie.forceHTTPS $pfile; then
   #   ox_set_property com.openexchange.cookie.forceHTTPS false $pfile
   #fi

   # SoftwareChange_Request-618
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.propagateHostNames $pfile; then
      ox_set_property com.openexchange.imap.propagateHostNames '' $pfile
   fi

   # SoftwareChange_Request-614
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mail.properties
   if ! ox_exists_property com.openexchange.mail.hidePOP3StorageFolders $pfile; then
      ox_set_property com.openexchange.mail.hidePOP3StorageFolders false $pfile
   fi

   # SoftwareChange_Request-602
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/servletmappings/servletmapping.properties
   ptmp=${pfile}.$$
   if grep -E "^/ajax/login" $pfile > /dev/null; then
      grep -vE "^/ajax/login" $pfile > $ptmp
      if [ -s $ptmp ]; then
          cp $ptmp $pfile
      fi
      rm -f $ptmp
   fi 

   # SoftwareChange_Request-568
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ox_exists_property FileStorageImpl $pfile; then
      ox_remove_property FileStorageImpl $pfile
   fi

   # SoftwareChange_Request-570
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.cookie.hash $pfile; then
      ox_set_property com.openexchange.cookie.hash calculate $pfile
   fi

   # SoftwareChange_Request-565
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/whitelist.properties
   if ! ox_exists_property html.tag.i $pfile; then
      ox_set_property html.tag.i '""' $pfile
   fi

   # SoftwareChange_Request-561
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mail.properties
   if ox_exists_property com.openexchange.mail.maxNumOfConnections $pfile; then
      ox_remove_property com.openexchange.mail.maxNumOfConnections $pfile
   fi

   # SoftwareChange_Request-537
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/sessiond.properties
   if ! ox_exists_property com.openexchange.sessiond.encryptionKey $pfile; then
      ox_set_property com.openexchange.sessiond.encryptionKey "auw948cz,spdfgibcsp9e8ri+<#qawcghgifzign7c6gnrns9oysoeivn" $pfile
   fi

   # SoftwareChange_Request-532
   # -----------------------------------------------------------------------
   smtpc=/opt/open-xchange/etc/groupware/smtp.properties
   mailc=/opt/open-xchange/etc/groupware/mail.properties
   oval=0
   if ox_exists_property com.openexchange.smtp.smtpRateLimit $smtpc; then
      oval=$(ox_read_property com.openexchange.smtp.smtpRateLimit $smtpc)
      ox_remove_property com.openexchange.smtp.smtpRateLimit $smtpc
   fi
   if ! ox_exists_property com.openexchange.mail.rateLimit $mailc; then
      ox_set_property com.openexchange.mail.rateLimit $oval $mailc
   fi
   if ! ox_exists_property com.openexchange.mail.rateLimitPrimaryOnly $mailc; then
      ox_set_property com.openexchange.mail.rateLimitPrimaryOnly true $mailc
   fi
   if ! ox_exists_property com.openexchange.mail.maxToCcBcc $mailc; then
      ox_set_property com.openexchange.mail.maxToCcBcc 0 $mailc
   fi

   # SoftwareChange_Request-519
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/file-logging.properties
   if ! ox_exists_property com.openexchange.login.internal.LoginPerformer.level $pfile; then
      ox_set_property com.openexchange.login.internal.LoginPerformer.level INFO $pfile
   fi
   if ! ox_exists_property com.openexchange.sessiond.impl.SessionHandler.level $pfile; then
      ox_set_property com.openexchange.sessiond.impl.SessionHandler.level INFO $pfile
   fi

   # SoftwareChange_Request-515
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.cookie.httpOnly $pfile; then
      ox_set_property com.openexchange.cookie.httpOnly true $pfile
   fi

   # SoftwareChange_Request-511 / Bugfix #17523
   # -----------------------------------------------------------------------
   if [ -e /opt/open-xchange/etc/groupware/excludedupdatetasks.properties ]; then
      if ! cmp /opt/open-xchange/etc/groupware/excludedupdatetasks.properties /opt/open-xchange/etc/common/excludedupdatetasks.properties >/dev/null; then
	  mv /opt/open-xchange/etc/groupware/excludedupdatetasks.properties /opt/open-xchange/etc/common/excludedupdatetasks.properties
      else
	  rm -f /opt/open-xchange/etc/groupware/excludedupdatetasks.properties
      fi
   fi

   # SoftwareChange_Request-505
   # -----------------------------------------------------------------------
   sessionc=/opt/open-xchange/etc/groupware/sessiond.properties
   serverc=/opt/open-xchange/etc/groupware/server.properties
   ajpc=/opt/open-xchange/etc/groupware/ajp.properties
   oval=1W
   if ox_exists_property com.openexchange.sessiond.cookie.ttl $sessionc; then
      oval=$(ox_read_property com.openexchange.sessiond.cookie.ttl $sessionc)
      ox_remove_property com.openexchange.sessiond.cookie.ttl $sessionc
   fi
   if ! ox_exists_property com.openexchange.cookie.ttl $serverc; then
      ox_set_property com.openexchange.cookie.ttl $oval $serverc
   fi
   if ox_exists_property AJP_JSESSIONID_TTL $ajpc; then
      ox_remove_property AJP_JSESSIONID_TTL $ajpc
   fi

   # SoftwareChange_Request-490
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/servletmappings/servletmapping.properties
   ptmp=${pfile}.$$
   if grep -E "^/ajax/infostore" $pfile > /dev/null; then
      grep -vE "^/ajax/infostore" $pfile > $ptmp
      if [ -s $ptmp ]; then
	  cp $ptmp $pfile
      fi
      rm -f $ptmp
   fi

   # SoftwareChange_Request-499
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ox-scriptconf.sh
   jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
   jopts=${jopts//\"/}
   if ! echo $jopts | grep "sun.net.inetaddr.ttl" > /dev/null; then
      ox_set_property JAVA_XTRAOPTS \""$jopts -Dsun.net.inetaddr.ttl=3600 -Dnetworkaddress.cache.ttl=3600 -Dnetworkaddress.cache.negative.ttl=10"\" $pfile
   fi

   # SoftwareChange_Request-498
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mime.types
   if ! grep "openxmlformats-officedocument" >/dev/null $pfile; then
      echo "application/vnd.openxmlformats-officedocument.wordprocessingml.document docx" >> $pfile
   fi

   # SoftwareChange_Request-479
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/smtp.properties
   if ! ox_exists_property com.openexchange.smtp.smtpRateLimit $pfile; then
      ox_set_property com.openexchange.smtp.smtpRateLimit 0 $pfile
   fi

   # SoftwareChange_Request-473
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/sessiond.properties
   for prop in com.openexchange.sessiond.isServerSocketEnabled com.openexchange.sessiond.isServerObjectStreamSocketEnabled com.openexchange.sessiond.serverPort com.openexchange.sessiond.serverObjectStreamPort com.openexchange.sessiond.isTcpClientSocketEnabled com.openexchange.sessiond.serverBindAddress com.openexchange.sessiond.isDoubleLoginPermitted com.openexchange.sessiond.sessionAuthUser com.openexchange.sessiond.isSecureSocketConnectionEnabled com.openexchange.sessiond.caFile com.openexchange.sessiond.certFile com.openexchange.sessiond.keyFile com.openexchange.sessiond.sessionContainerTimeout com.openexchange.sessiond.numberOfSessionContainers; do
      if ox_exists_property $prop $pfile; then
	  ox_remove_property $prop $pfile
      fi
   done
   if ! ox_exists_property com.openexchange.sessiond.randomTokenTimeout $pfile; then
      ox_set_property com.openexchange.sessiond.randomTokenTimeout 1M $pfile
   fi
   if ! ox_exists_property com.openexchange.sessiond.sessionLongLifeTime $pfile; then
      ox_set_property com.openexchange.sessiond.sessionLongLifeTime 1W $pfile
   fi
   pfile=/opt/open-xchange/etc/groupware/system.properties
   if ox_exists_property SESSIONDPROPERTIES $pfile; then
      ox_remove_property SESSIONDPROPERTIES $pfile
   fi

   # SoftwareChange_Request-378
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/imap.properties
   if ! ox_exists_property com.openexchange.imap.propagateClientIPAddress $pfile; then
      ox_set_property com.openexchange.imap.propagateClientIPAddress false $pfile
   fi

   # SoftwareChange_Request-371
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ox-scriptconf.sh
   jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
   jopts=${jopts//\"/}
   if ! echo $jopts | grep "MaxPermSize" > /dev/null; then
      ox_set_property JAVA_XTRAOPTS \""$jopts -XX:MaxPermSize=128M"\" $pfile
   fi

   # SoftwareChange_Request-354
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mail.properties
   if ! ox_exists_property com.openexchange.mail.addClientIPAddress $pfile; then
      ox_set_property com.openexchange.mail.addClientIPAddress false $pfile
   fi

   # SoftwareChange_Request-334
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/sessiond.properties
   if ! ox_exists_property com.openexchange.sessiond.autologin $pfile; then
      ox_set_property com.openexchange.sessiond.autologin false $pfile
   fi
   # obsoleted by SoftwareChange_Request-505
   # if ! ox_exists_property com.openexchange.sessiond.cookie.ttl $pfile; then
   #   ox_set_property com.openexchange.sessiond.cookie.ttl 1W $pfile
   # fi

   # SoftwareChange_Request-341
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/contact.properties
   if ! ox_exists_property com.openexchange.contacts.allFoldersForAutoComplete $pfile; then
      ox_set_property com.openexchange.contacts.allFoldersForAutoComplete true $pfile
   fi

   # SoftwareChange_Request-308
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/whitelist.properties
   for prop in html.tag.dd html.tag.dt; do
      if ! ox_exists_property $prop $pfile; then
	  ox_set_property $prop '""' $pfile
      fi
   done

   # SoftwareChange_Request-294
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mailcache.ccf
   ptmp=${pfile}.$$
   if grep -E "^jcs.default" $pfile > /dev/null; then
      grep -vE "^jcs.default" $pfile > $ptmp
      if [ -s $ptmp ]; then
	  cp $ptmp $pfile
      fi
      rm -f $ptmp
   fi

   # SoftwareChange_Request-293
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/notification.properties
   if ! ox_exists_property com.openexchange.notification.fromSource $pfile; then
       ox_set_property com.openexchange.notification.fromSource "primaryMail" $pfile
   fi

   # SoftwareChange_Request-285
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.UIWebPath $pfile; then
      ox_set_property com.openexchange.UIWebPath "/ox6/index.html" $pfile
   fi

   # SoftwareChange_Request-266
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/user.properties
   if ! ox_exists_property com.openexchange.folder.tree $pfile; then
      ox_set_property com.openexchange.folder.tree 0 $pfile
   fi

   # Property to disable iCal attachment for iMIP mail messages to internal users.
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/notification.properties
   if ! ox_exists_property imipForInternalUser $pfile; then
      ox_set_property imipForInternalUser false $pfile
   fi

   # SoftwareChange_Request-194
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/cache.ccf
   val=0$(ox_read_property jcs.region.User.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 40000 ]; then
      ox_set_property jcs.region.User.cacheattributes.MaxObjects 40000 $pfile
   fi
   val=0$(ox_read_property jcs.region.UserConfiguration.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 20000 ]; then
      ox_set_property jcs.region.UserConfiguration.cacheattributes.MaxObjects 20000 $pfile
   fi
   val=0$(ox_read_property jcs.region.UserSettingMail.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 20000 ]; then
      ox_set_property jcs.region.UserSettingMail.cacheattributes.MaxObjects 20000 $pfile
   fi
   val=0$(ox_read_property jcs.region.OXDBPoolCache.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 20000 ]; then
      ox_set_property jcs.region.OXDBPoolCache.cacheattributes.MaxObjects 20000 $pfile
   fi
   val=0$(ox_read_property jcs.region.MailAccount.cacheattributes.MaxObjects $pfile)
   if [ $val -lt 100000 ]; then
      ox_set_property jcs.region.MailAccount.cacheattributes.MaxObjects 100000 $pfile
   fi

   # SoftwareChange_Request-131
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property com.openexchange.IPCheck $pfile; then
      ox_set_property com.openexchange.IPCheck true $pfile
   fi

   # SoftwareChange_Request-124
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/cache.ccf
   grep jcs.region.GlobalFolderCache $pfile >/dev/null || {
cat<<EOF >> $pfile
# Pre-defined cache regions for global folder objects.
jcs.region.GlobalFolderCache=LTCP
jcs.region.GlobalFolderCache.cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes
jcs.region.GlobalFolderCache.cacheattributes.MaxObjects=10000000
jcs.region.GlobalFolderCache.cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache
jcs.region.GlobalFolderCache.cacheattributes.UseMemoryShrinker=true
# Disable MaxMemoryIdleTimeSeconds cause some entries can be eternal
# Shrinker removal works as follows:
# 1. Check 'Eternal', 'MaxLifeSeconds' AND 'IdleTime' for element-attribute-caused removal
# 2. Check 'MaxMemoryIdleTime' for cache-attribute-caused removal
jcs.region.GlobalFolderCache.cacheattributes.MaxMemoryIdleTimeSeconds=180
jcs.region.GlobalFolderCache.cacheattributes.ShrinkerIntervalSeconds=60
jcs.region.GlobalFolderCache.elementattributes=org.apache.jcs.engine.ElementAttributes
jcs.region.GlobalFolderCache.elementattributes.IsEternal=false
jcs.region.GlobalFolderCache.elementattributes.MaxLifeSeconds=300
jcs.region.GlobalFolderCache.elementattributes.IdleTime=180
jcs.region.GlobalFolderCache.elementattributes.IsSpool=false
jcs.region.GlobalFolderCache.elementattributes.IsRemote=false
jcs.region.GlobalFolderCache.elementattributes.IsLateral=false
EOF
}
   grep jcs.region.UserFolderCache $pfile >/dev/null || {
cat<<EOF >> $pfile
# Pre-defined cache regions for user-sensitive folder objects.
jcs.region.UserFolderCache=LTCP
jcs.region.UserFolderCache.cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes
jcs.region.UserFolderCache.cacheattributes.MaxObjects=10000000
jcs.region.UserFolderCache.cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache
jcs.region.UserFolderCache.cacheattributes.UseMemoryShrinker=true
# Disable MaxMemoryIdleTimeSeconds cause some entries can be eternal
# Shrinker removal works as follows:
# 1. Check 'Eternal', 'MaxLifeSeconds' AND 'IdleTime' for element-attribute-caused removal
# 2. Check 'MaxMemoryIdleTime' for cache-attribute-caused removal
jcs.region.UserFolderCache.cacheattributes.MaxMemoryIdleTimeSeconds=180
jcs.region.UserFolderCache.cacheattributes.ShrinkerIntervalSeconds=60
jcs.region.UserFolderCache.elementattributes=org.apache.jcs.engine.ElementAttributes
jcs.region.UserFolderCache.elementattributes.IsEternal=false
jcs.region.UserFolderCache.elementattributes.MaxLifeSeconds=300
jcs.region.UserFolderCache.elementattributes.IdleTime=180
jcs.region.UserFolderCache.elementattributes.IsSpool=false
jcs.region.UserFolderCache.elementattributes.IsRemote=false
jcs.region.UserFolderCache.elementattributes.IsLateral=false
EOF
}
  grep jcs.region.MailAccount $pfile >/dev/null || {
cat<<EOF >> $pfile
# Pre-defined cache region for mail account
jcs.region.MailAccount=LTCP
jcs.region.MailAccount.cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes
jcs.region.MailAccount.cacheattributes.MaxObjects=1000
jcs.region.MailAccount.cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache
jcs.region.MailAccount.cacheattributes.UseMemoryShrinker=true
jcs.region.MailAccount.cacheattributes.MaxMemoryIdleTimeSeconds=180
jcs.region.MailAccount.cacheattributes.ShrinkerIntervalSeconds=60
jcs.region.MailAccount.cacheattributes.MaxSpoolPerRun=500
jcs.region.MailAccount.elementattributes=org.apache.jcs.engine.ElementAttributes
jcs.region.MailAccount.elementattributes.IsEternal=false
jcs.region.MailAccount.elementattributes.MaxLifeSeconds=300
jcs.region.MailAccount.elementattributes.IdleTime=180
jcs.region.MailAccount.elementattributes.IsSpool=false
jcs.region.MailAccount.elementattributes.IsRemote=false
jcs.region.MailAccount.elementattributes.IsLateral=false
EOF
}

   # SoftwareChange_Request-125
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/server.properties
   if ! ox_exists_property PUBLISH_REVOKE $pfile; then
      ox_set_property PUBLISH_REVOKE "" $pfile
   fi

   # SoftwareChange_Request-109 / SoftwareChange_Request-104
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/calendar.properties
   if ! ox_exists_property com.openexchange.calendar.undefinedstatusconflict $pfile; then
      ox_set_property com.openexchange.calendar.undefinedstatusconflict true $pfile
   fi
   if ! ox_exists_property com.openexchange.calendar.seriesconflictlimit $pfile; then
      ox_set_property com.openexchange.calendar.seriesconflictlimit true $pfile
   fi

   # SoftwareChange_Request-84
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/TidyConfiguration.properties
   if ox_exists_property clean $pfile; then
      ox_remove_property clean $pfile
   fi

   # SoftwareChange_Request-70
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/system.properties
   if ox_exists_property configDB $pfile; then
      ox_remove_property configDB $pfile
   fi

   # SoftwareChange_Request-62 / Bugfix #13477
   # -----------------------------------------------------------------------
   if [ -e /opt/open-xchange/etc/groupware/foldercache.properties ]; then
      if ! cmp /opt/open-xchange/etc/groupware/foldercache.properties /opt/open-xchange/etc/common/foldercache.properties >/dev/null; then
	  mv /opt/open-xchange/etc/groupware/foldercache.properties /opt/open-xchange/etc/common/foldercache.properties
      else
	  rm -f /opt/open-xchange/etc/groupware/foldercache.properties
      fi
   fi

   # SoftwareChange_Request-55
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/mail.properties
   if ! ox_exists_property com.openexchange.mail.mailAccessCacheShrinkerSeconds $pfile; then
      ox_set_property com.openexchange.mail.mailAccessCacheShrinkerSeconds 3 $pfile
   fi
   if ! ox_exists_property com.openexchange.mail.mailAccessCacheIdleSeconds $pfile; then
      ox_set_property com.openexchange.mail.mailAccessCacheIdleSeconds 7 $pfile
   fi

   # bugfix id#12859
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/ox-scriptconf.sh
   if ! ox_exists_property UMASK $pfile; then
      ox_set_property UMASK 066 $pfile
   fi
   # bugfix id#13928
   # -----------------------------------------------------------------------
   if ! ox_exists_property COMMONPROPERTIESDIR $pfile; then
      ox_set_property COMMONPROPERTIESDIR /opt/open-xchange/etc/common $pfile
   fi

   # bugfix id#13313
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/whitelist.properties
   if ! ox_exists_property html.tag.base $pfile; then
      ox_set_property html.tag.base \"",href\"" $pfile
   fi

   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/groupware/contact.properties
   if ! ox_exists_property com.openexchange.contact.singleFolderSearch $pfile; then
      ox_set_property com.openexchange.contact.singleFolderSearch false $pfile
   fi

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

   # obsoleted by SoftwareChange_Request-505
   # we're updating from pre sp5
   # -----------------------------------------------------------------------
   # pfile=/opt/open-xchange/etc/groupware/ajp.properties
   # if ! ox_exists_property AJP_JSESSIONID_TTL $pfile; then
   #    ox_set_property AJP_JSESSIONID_TTL 86400000 $pfile
   # fi

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

   ox_update_permissions "/opt/open-xchange/etc/groupware/mail.properties" root:open-xchange 640
   ox_update_permissions "/opt/open-xchange/etc/groupware/configdb.properties" root:open-xchange 640
   ox_update_permissions "/opt/open-xchange/etc/groupware/server.properties" root:open-xchange 640
   ox_update_permissions "/var/spool/open-xchange/uploads" open-xchange:root 750

   # run checkconfigconsistency once
   /opt/open-xchange/sbin/checkconfigconsistency
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/importCSV
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/bundles/*
%config(noreplace) /opt/open-xchange/importCSV/*

%files -n open-xchange -f %{configfiles}
%defattr(-,root,root)
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/etc/groupware/osgi
%dir /opt/open-xchange/sbin
/sbin/*
/opt/open-xchange/etc/groupware/osgi/config.ini.template
/opt/open-xchange/sbin/*
%dir %attr(750,open-xchange,open-xchange) /var/log/open-xchange
%dir %attr(750,open-xchange,root) /var/spool/open-xchange/uploads
/etc/init.d/open-xchange-groupware
%dir /opt/open-xchange/etc/groupware/servletmappings
%dir /opt/open-xchange/etc/groupware
/opt/open-xchange/etc/groupware/servletmappings/*
%doc doc/examples

%changelog
* Fri Jul 22 2011 - choeger@open-xchange.com
 - Bugfix #19921: package open-xchange is missing a dependency on open-xchange-publish-infostore-online
* Fri Jul 22 2011 - thorben.betten@open-xchange.com
 - Fix bug 19923: Using timeout
 - Fix bug 19924: Using timeout
* Thu Jul 21 2011 - tobias.prinz@open-xchange.com
 - Bugfix #19915: A mis-matching byte order mark (BOM) does not throw the parser off anymore.
* Thu Jul 21 2011 - marcus.klein@open-xchange.com
 - Bugfix #19890: No more NullPointerExceptions in UDP push bundle if socket initialization fails.
 - Bugfix #19880: Partial fix. Decoupling the clean up task of the database pooling from other subsystems to avoid blocking situations.
* Thu Jul 21 2011 - thorben.betten@open-xchange.com
 - Fix for bug #19920: Using proper tree identifier
 - Bugfix #19910: Multiple IMAP-IDLE listeners for certain clients
 - Bugfix #19880: Logging problem is fixed, too
* Wed Jul 20 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19547: Resolve groups in update cycle, retain all resources of an appointment, declare only primary mail address as private address.
* Wed Jul 20 2011 - thorben.betten@open-xchange.com
 - Fix for bug 19688: Introduced white-list property to specify clients which are allowed to receive a notification about a new mail (mail-push)
* Wed Jul 20 2011 - martin.herfurth@open-xchange.com
 - Bugfix #19109: Moving a sequence destroys the appointment.
* Tue Jul 19 2011 - thorben.betten@open-xchange.com
 - Bugfix #19776: Applying proper subscription status to newly created mail default folders
 - Bugfix #19824: Token-based access to mail attachments
 - Bugfix #19875: Updated GMX export URL in corresponding .yml files
* Tue Jul 19 2011 - marcus.klein@open-xchange.com
 - Bugfix #19818: Setting secure flag on cookies even if autologin is not enabled.
* Tue Jul 19 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19484: Only patch appointments once for use in CalDAV interface
* Mon Jul 18 2011 - thorben.betten@open-xchange.com
 - Bugfix #19728: Configured max. connection restriction for GMail by default
 - Bugfix #19722: Additional check for possibly configured global password if password is missing on mail connect attempt
 - Bugfix #19777: Changed/deleted contacts is honored in existing distribution lists
 - Bugfix #14653 + #12985: Suppress notification message if no relevant changes can be detected
* Mon Jul 18 2011 - martin.herfurth@open-xchange.com
 - Bugfix #19490: Error during exception change.
 - Bugfix #19489: Removing recurrence information from a recurring appointment.
* Sun Jul 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #19821: Properly handling a disappeared mail folder on root level
* Sat Jul 16 2011 - thorben.betten@open-xchange.com
 - Bugfix #19726: Checking for existence of storage directory prior attempting to delete it
 - Bugfix #19747: Fixed possible NPE when parsing an ICal's participants
* Fri Jul 15 2011 - thorben.betten@open-xchange.com
 - Bugfix #19792: Fixed possible IndexOutOfBoundsException when fetching an IMAP folder's messages
 - Bugfix #19816: No alias check with NULL value
* Thu Jul 14 2011 - thorben.betten@open-xchange.com
 - Bugfix #19766: Reliable check for a folder's content type
 - Bugfix #19736: Retry mechanism for mail default folder check
* Wed Jul 13 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18688: Correct attachment count in calendar
* Wed Jul 13 2011 - tobias.prinz@open-xchange.com
 - Bugfix #19647: Tolerating whitespaces in subscription URIs 
* Wed Jul 13 2011 - thorben.betten@open-xchange.com
 - Bugfix #19752: Using newest stable twitter4j API to display home timeline (incl. retweets)
* Tue Jul 12 2011 - martin.herfurth@open-xchange.com
 - Bugfix #19667: Resources in a series are conflicting with itself in exceptions.
* Tue Jul 12 2011 - marcus.klein@open-xchange.com
 - Bugfix #19576: Removed the usage of the beta flag. It is still only present in the API.
* Tue Jul 12 2011 - thorben.betten@open-xchange.com
 - Bugfix #19739: Auto-detection of ACL entities
* Mon Jul 11 2011 - thorben.betten@open-xchange.com
 - Bugfix #19635: Ensure termination of consumer task upon mail mass import
 - Bugfix #19128: Accepting context administrator as a user's contact creator, too
* Mon Jul 11 2011 - steffen.templin@open-xchange.com
 - Bugfix #19487: The name of the updater installer that can be downloaded from web gui is configurable now
 - Bugfix #19488: The template values product-name, base-name and icon are configuration-file properties now
* Sun Jul 10 2011 - thorben.betten@open-xchange.com
 - Bugfix #19480: Reliable sorting of message by arrival date.
 - Bugfix #19595: Added permission check prior to creating attachment-publishing infostore folder
 - Bugfix #19608: Ensured proper removal from MailAccess watcher on close
* Fri Jul 08 2011 - thorben.betten@open-xchange.com
 - Bugfix #19751: Proper generation of FETCH command items if IMAPrev1 is supported
* Mon Jul 04 2011 - thorben.betten@open-xchange.com
 - Bugfix #19585: Allowing empty cookie values; e.g. 'mycookie='
 - Bugfix #19691: Dropping AJP connection (in AJP way) if a corrupt AJP cycle is detected
* Sat Jul 02 2011 - thorben.betten@open-xchange.com
 - Bugfix #19561: Proper re-initialization of LIST/LSUB cache
 - Bugfix #19683: Fixed NPE in Unified Mail
 - Bugfix #19684: Dealing with possible missing headers when writing OLOX2's structured JSON mail object
 - Bugfix #19628: Safe reading of a message's address headers
 - Bugfix #19657: Dealing with possible failure when reading from an account's folder
* Fri Jul 01 2011 - tobias.prinz@open-xchange.com
 - Bugfix #19600: Deleting the first and only user of a context made the filestore inaccessible to other users in that context. Fixed. 
* Fri Jul 01 2011 - thorben.betten@open-xchange.com
 - Bugfix #19536: Proper detection of possible quota exceeded error
* Thu Jun 30 2011 - thorben.betten@open-xchange.com
 - Bugfix #19658: [L3] Moving mails with the OX WebGUI makes them disappear (dovecot)
 - Bugfix #19669: Proper expunge flag on folder closure
* Thu Jun 30 2011 - steffen.templin@open-xchange.com
 - Bugfix #19670: To see the updater-download-link in the OX GUI the user also needs permissions for USM instead of only for OLOX20.
* Wed Jun 29 2011 - marcus.klein@open-xchange.com
 - Not creating always confirmed-spam and confirmed-ham folders anymore for Cloudmark spam handler. confirmed-spam is created if the
   configuration tells to move spam mails to that folder.
* Mon Jun 27 2011 - francisco.laguna@open-xchange.com
 - Bug #19598: Don't die on decryption errors so users can remove their OAuth accounts
* Fri Jun 24 2011 - marcus.klein@open-xchange.com
 - TA7380 of US6578, Bugfix #19609: Passing HTTP headers to authentication implementation.
* Thu Jun 23 2011 - thorben.betten@open-xchange.com
 - Bugfix #19591: Dealing with failing retrieval of a POP3 server's capabilities (through CAPA command)
* Wed Jun 22 2011 - thorben.betten@open-xchange.com
 - Bugfix #19571: Resolved warnings when creating a POP3 account
 - Bugfix #19584: Properly dealing with a CommandFailedException and switching fetch profile
* Tue Jun 21 2011 - thorben.betten@open-xchange.com
 - Bugfix #19540: Applying proper flag to contact instance if considered as distribution list
 - Bugfix #19471: Adapted to MS invitation mails
 - Bugfix #19270: Proper order of contacts for special sort field (607)
 - Bugfix #19560: Added missing tags to 'whitelist.properties' file
 - Bugfix #19562: Showing proper TNEF attachments if present
 - Bugfix #19566: Proper detection if a global folder is deleted
* Tue Jun 21 2011 - marcus.klein@open-xchange.com
 - TA7369 of US6559: Changed user attributes now go through a diff algorithm and then only real changes are applied to database.
* Mon Jun 20 2011 - francisco.laguna@open-xchange.com
 - Bugfix 19220: Show goodwill when user and context is not known.
* Mon Jun 20 2011 - marcus.klein@open-xchange.com
 - Bugfix #19495: listfilestore now works again if server_id something else than 2.
* Mon Jun 20 2011 - thorben.betten@open-xchange.com
 - Bugfix #19534: Fail for invalid transport server, too
* Fri Jun 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #19522: Added support for hexadecimal entities like &#xFC;
* Wed Jun 15 2011 - thorben.betten@open-xchange.com
 - Bugfix #19497: Setting proper "msgref" attribute
 - Bugfix #19512: Fixed mail composal
* Tue Jun 14 2011 - thorben.betten@open-xchange.com
 - Bugfix #19474: Fixed moving a mail folder if altnamespace enabled
* Tue Jun 14 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19458: Use secret service everywhere.
 - Bugfix #19484: Calculate correct until date when it occurs after the appointment.
* Sun Jun 12 2011 - thorben.betten@open-xchange.com
 - Bugfix #19496: Removing CDATA sections by disabling HtmlCleaner's "useCdata" parameter
 - Bugfix #19462: Set unread count in returned IMAP mail
* Sat Jun 11 2011 - thorben.betten@open-xchange.com
 - Bugfix #19466: Proper replacement of &apos; HTML entity
* Wed Jun 08 2011 - thorben.betten@open-xchange.com
 - Bugfix #19374: Hierarchical-wise display of shared folders
* Wed Jun 08 2011 - steffen.templin@open-xchange.com
 - Bugfix #19442: The OXUpdater now provides a fallback mechanism that delivers files in en_US if the users preferred language is not available.
* Tue Jun 07 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19410: No stack trace in publications on error.
 - Bugfix #18646: Better equality check for network interfaces in use in UDP push.
* Tue Jun 07 2011 - thorben.betten@open-xchange.com
 - Bugfix #19428: Using HtmlCleaner library to generate a well-formed and pretty-printed HTML because JTidy fails to do so
 - Bugfix #19459: Changed session-bound LIST/LSUB cache to user-bound one. Thus changes are available for all active sessions without the
                  need for propagating throughout sessions.
* Mon Jun 06 2011 - thorben.betten@open-xchange.com
 - Bugfix #19358: Ignoring invalid date strings and generate an appropriate warning
* Fri Jun 03 2011 - marcus.klein@open-xchange.com
 - Bugfix #19373: Not presenting OXtender for Outlook 1 if Outlook 2010 is installed.
* Fri Jun 03 2011 - thorben.betten@open-xchange.com
 - Bugfix #19418: Adding user's time zone offset to a message's date headers and its received date
* Thu Jun 02 2011 - thorben.betten@open-xchange.com
 - Bugfix #19291: Throwing proper error if an invalid host name is entered
 - Bugfix #19305: Disabled caching for Unified Mail folders
 - Bugfix #19335: Fixed NPE when converting TNEF to ICal
 - Bugfix #19084: Honor tab character when parsing ENVELOPE's subject
* Wed Jun 01 2011 - steffen.templin@open-xchange.com
 - Bugfix #19373 - The outlook updater should not display OXtender1 if outlook 2010 is installed
 - Bugfix #19226 - OutlookUpdater bundle now provides two settings to indicate if module is available and if the user has all necessary permissions to see it
* Wed Jun 01 2011 - thorben.betten@open-xchange.com
 - Bugfix #19407: Checking for forbidden subscribe operation on a default folder
 - Bugfix #19155: Introduced create-if-absent mechanism for external accounts' default folder full names
* Wed Jun 01 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19391: Check publication permission before delivering publication
 - Bugfix #18899: Don't lose title when saving a mail attachment in the infostore
* Tue May 31 2011 - francisco.laguna@open-xchange.com
  - Bugfix 18630: Improved logging of UDP send errors.
  - Bugfix 18875: Stricter filtering of server side templates.
* Tue May 31 2011 - thorben.betten@open-xchange.com
 - Bugfix #19400: Fixed possible IllegalArgumentException
* Mon May 30 2011 - thorben.betten@open-xchange.com
 - Bugfix #19362: Throwing a meaningful error if folder creation returns an invalid folder identifier
 - Bugfix #19377: Introduced listener for folder events in IMAP bundle to clear LIST/LSUB cache if necessary
* Fri May 27 2011 - thorben.betten@open-xchange.com
 - Bugfix #19372: Removing MS Word tags in a mail's HTML content
* Thu May 26 2011 - francisco.laguna@open-xchange.com
  - Bugfix 19140: Improved error message, improved reauthorization
  - Bugfix 19220: Make messaging configurable.
* Thu May 26 2011 - thorben.betten@open-xchange.com
 - Bugfix #19315: Converting distribution list to an appropriate VCard attachment
* Wed May 25 2011 - steffen.templin@open-xchange.com
 - Bugfix #18983: Changed the 7zip module of the outlook updater bundle and the installer name of the OXUpdater to support installing without administrator privileges
* Tue May 24 2011 - thorben.betten@open-xchange.com
 - Bugfix #18846: Fixed handling of nested mail in JSON mail structure
* Mon May 23 2011 - thorben.betten@open-xchange.com
 - Bugfix #19214: Fixed mail connection counter
 - Bugfix #19312: Applying proper server and port setting if SMTP authentication is disabled
 - Bugfix #19299: Allowing an empty subject in MAL API
* Mon May 23 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19317: Pay attention to https switch in infostore publications
* Fri May 20 2011 - thorben.betten@open-xchange.com
 - Bugfix #19142: Introduced more reliable/precise check if an AJP keep-alive needs to be performed
 - Bugfix #18885: Fixed display of winmail.dat attachments
 - Bugfix #19289: Disabled JSON message cache for external accounts
 - Bugfix #19228: Disabled admin permission on Unified Mail folders
 - Bugfix #19269: Applying proper fields on fast FETCH
* Wed May 18 2011 - marcus.klein@open-xchange.com
 - Bugfix #19281: Fixed a NullPointerException when testing transport connection.
 - Bugfix #19127: Not hiding image too large exception behind a broken picture exception anymore.
* Wed May 18 2011 - thorben.betten@open-xchange.com
 - Bugfix #19179: Probing for '�' character when decoding an encoded-word with big5 encoding. If present use Big5-HKSCS instead.
* Tue May 17 2011 - marcus.klein@open-xchange.com
 - Bugfix #18953: Checking for folder visible permissions on shared tasks folders. This solves a problem when a user is downgraded to PIM.
* Tue May 17 2011 - dennis.sieben@open-xchange.com
 - Bugfix #18656: Corrected order of headers and message when transferring a message to spamassassin for learning.
* Tue May 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #19259: Checking for possible null elements before sorting list
* Mon May 16 2011 - thorben.betten@open-xchange.com
 - Bugfix #18836: Dealing with possible runtime exception
 - Bugfix #19247: Fixed display of Japanese-encoded mail text
* Mon May 16 2011 - marcus.klein@open-xchange.com
 - Bugfix #19235: Improved some exception message to make it more understandable for end users.
* Sun May 15 2011 - thorben.betten@open-xchange.com
 - Bugfix #19175: Fixed access to POP3Folder's Protocol instance
 - Bugfix #19167: Perform an unsubscribe before renaming a folder
 - Bugfix #18742: Lowered log level to WARNING if a requested mail could not be found
 - Bugfix #19185: Fixed IllegalStateException on shut-down
* Thu May 12 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19140: Better error messages for facebook messaging, when a token was invalidated.
* Wed May 11 2011 - marcus.klein@open-xchange.com
 - Bugfix #19195: Fixed NPE when exporting iCal.
* Wed May 11 2011 - tobias.prinz@open-xchange.com
 - Updated ical4j from v1.0-beta to 1.0-release
* Tue May 10 2011 - martin.herfurth@open-xchange.com
 - Bugfix #18558: Appointments which are visible through several folders contain correct folder id.
 - Bugfix #19089: Added VTimZone Objects to ical exporter.
* Mon May 09 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19169: Switch for UWA Widgets.
 - Bugfix #19024: Avoid NPEs when trying to download file that doesn't exist.
* Mon May 09 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18875: Consider more than one .properties file in the templates directory.
 - Bugfix #19069: Merge contacts on CSV import.
* Sat May 07 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19088: Add possibility to use a deferrer step in multidomain setups even when API keys are configured to use a single domain.
* Sat May 07 2011 - thorben.betten@open-xchange.com
 - Bugfix #19156: Introduced batch-wise processing of a POP3 account's messages
* Thu May 05 2011 - marcus.klein@open-xchange.com
 - Bugfix #19119: Corrected drive letter parameter for OXUpdater installer.
* Thu May 05 2011 - francisco.laguna@open-xchange.com
 - Bugfix #19146: Fix NPE on shutdown.
* Wed May 04 2011 - thorben.betten@open-xchange.com
 - Bugfix #19124: Fixed NPE on IMAP bundle start
* Mon May 02 2011 - thorben.betten@open-xchange.com
 - Bugfix #19030: Changed wording
* Sat Apr 30 2011 - thorben.betten@open-xchange.com
 - Bugfix #19083: Reduced execution of avoidable POP3 commands while importing POP3 messages to backing storage to avoid possibly exceeding
                  provider restrictions regarding number of executed POP3 command
 - Bugfix #18784: Trying to recover from a MySQL integrity constraint violation while deleting an external IMAP account
* Thu Apr 28 2011 - thorben.betten@open-xchange.com
 - Bugfix #19065: Fixed body search for IMAP
* Wed Apr 27 2011 - steffen.templin@open-xchange.com
 - Bugfix #19028: RSS messages now contain unique ids and urls to their origin.
 - Bugfix #18469: Fixed NPE for mailfilter request with missing session parameter.
* Wed Apr 27 2011 - thorben.betten@open-xchange.com
 - Bugfix #19038: Using Netscape's date format for "expires" attribute: Wdy, DD-Mon-YY HH:MM:SS GMT
* Tue Apr 26 2011 - tobias.prinz@open-xchange.com
 - Bugfix #19046: ICal appointment series with a start date that is not part of the series (e.g. weekly series on Wednesday, starts Monday) now have a separate starting appointment.
* Tue Apr 26 2011 - thorben.betten@open-xchange.com
 - Bugfix #18981: Ignoring empty addresses occurring in RFC822 address header
 - Bugfix #18974: Introduced possibility to restrict number of concurrent connections to a subscribed/external IMAP account
* Thu Apr 21 2011 - choeger@open-xchange.com
 - Bugfix #19010: NullPointerException in IMAP Idle bundle
* Thu Apr 21 2011 - marcus.klein@open-xchange.com
 - Bugfix #19018: Added missing import for javax.xml.transform.stream to server bundle.
* Thu Apr 21 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18866: Allow regular users to be granted administrative access to the three root public folders.
* Wed Apr 20 2011 - marcus.klein@open-xchange.com
 - Bugfix #18997: Added an update task that fixes the wrong definitions for table oauthAccounts.
 - Bugfix #18868: Added timeout to apache balancer member configuration to not inherit this from the global apache timeout.
* Tue Apr 19 2011 - marcus.klein@open-xchange.com
 - Bugfix #18993: Not putting sessions into the long term session container anymore if auto login feature is disabled.
* Mon Apr 18 2011 - marcus.klein@open-xchange.com
 - Bugfix #18837: Calculating conflicts for series that start in the past but last into the future.
* Mon Apr 18 2011 - martin.herfurth@open-xchange.com
 - Bugfix #18896: IMIP generation respects parameter for internal participants on update.
* Fri Apr 15 2011 - marcus.klein@open-xchange.com
 - Bugfix #18816: Properly initializing the update tasks if they are executed in administration daemon.
* Thu Apr 14 2011 - marcus.klein@open-xchange.com
 - Bugfix #18946: Improved exception message telling a user that he can not give some special permission on a folder for another user.
* Wed Apr 13 2011 - marcus.klein@open-xchange.com
 - Bugfix #18421: Enabled editing the personal mail information for webmail users.
* Tue Apr 12 2011 - marcus.klein@open-xchange.com
 - Bugfix #18911: Remembering added links when formatting plain text email for HTML display.
* Mon Apr 11 2011 - thorben.betten@open-xchange.com
 - Bugfix #18826: Fixed missing keep-seen right "s" when applying read-all access to an IMAP folder
* Mon Apr 11 2011 - choeger@open-xchange.com
 - Bugfix #18914: [L3] Autostart symlinks in /etc/rc.d missing on Debian Squeeze
* Mon Apr 11 2011 - tobias.prinz@open-xchange.com
 - Bugfix #18875: Template selection now can be filtered by categories.
* Fri Apr 08 2011 - marcus.klein@open-xchange.com, martin.herfurth@open-xchange.com
 - Bugfix #18912: Using enumeration to pass order directory for data everywhere. Not showing failing SQL statements to the end user.
* Thu Apr 07 2011 - thorben.betten@open-xchange.com
 - Bugfix #18787: Throwing a more meaningful error if quota is exceeded
 - Bugfix #15227: Introduced new locale-sensitive error code
 - Bugfix #18890: Introduced entity-to-ACL mapping for MDaemon IMAP server
* Thu Apr 07 2011 - marcus.klein@open-xchange.com
 - Bugfix #18804: User contacts are not mixed up anymore if a part of them is already cached.
* Wed Apr 06 2011 - thorben.betten@open-xchange.com
 - Bugfix #18851: Proper display of non-ascii characters even though "Content-Transfer-Encoding: 7bit" is set
 - Bugfix #18870: Ignoring the \HasNoChildren flag and fall-back to a reliable subfolder check (through a LIST command)
* Wed Apr 06 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18865: Lazily load ipcheck whitelist on first access.
 - Bugfix #18618: Make account label translatable.
* Tue Apr 05 2011 - marcus.klein@open-xchange.com
 - Bugfix #18775: Improved mechanism to find correct user for an IMAP ACL.
 - Bugfix #18613: Interpreting an empty msgstr in PO files as not translated string.
* Tue Apr 05 2011 - thorben.betten@open-xchange.com
 - Bugfix #18856: Proper composal of plain-text part
 - Bugfix #18806: Suppress JavaMail debug logging
 - Bugfix #18852: Fixed selecting referenced message on forward operation
 - Bugfix #18842: Assume INBOX exists regardless of IMAP server's LIST response
 - Bugfix #18807: Fixed possible NPE
 - Bugfix #18797: Allow root level subfolders if Cyrus' "altNamespace" feature is enabled
 - Bugfix #18840: Proper error message if a javax.mail.MessageRemovedException occurs
* Mon Apr 04 2011 - marcus.klein@open-xchange.com
 - Bugfix #18806: Removed debug output into console log file.
 - Bugfix #18781: Improved an exception message.
* Mon Apr 04 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18835: Change column type of value column to TEXT.
* Thu Mar 31 2011 - marcus.klein@open-xchange.com
 - Bugfix #18788: Not dropping an existing schema if MySQL does not generate proper unique identifier.
 - Bugfix #18824: Fixed a coding problem setting the protocol with the additional S for secure.
* Mon Mar 28 2011 - marcus.klein@open-xchange.com
 - Bugfix #18745: SQL IN is a lot faster than INNER JOIN with UNION ALL. Fixed a N+1 select problem.
* Mon Mar 28 2011 - thorben.betten@open-xchange.com
 - Bugfix #18769: Proper initialization of AJP bundle
* Fri Mar 25 2011 - marcus.klein@open-xchange.com
 - Bugfix #18755: Correctly setting the secure flag for mail account URLs when parsing with IPv6 capable URIParser.
* Wed Mar 23 2011 - thorben.betten@open-xchange.com
 - Bugfix #18709: Removing starting white-space character on html2text conversion
 - Bugfix #18633: Proper removal from user-sensitive folder cache on move operation
* Mon Mar 21 2011 - choeger@open-xchange.com
 - Bugfix #18713: oauth-twitter is missing after updating
* Mon Mar 21 2011 - steffen.templin@open-xchange.com
 - Bugfix #18729: RSS feed from "Tagesschau" only shows the first headline
* Sun Mar 20 2011 - thorben.betten@open-xchange.com
 - Bugfix #18698: Setting appropriate javax.activation.DataHandler instance for mail part
* Thu Mar 17 2011 - marcus.klein@open-xchange.com
 - Bugfix #18643: Fixed old mail accounts without transport server for IPv6 support.
 - Bugfix #18681: Removed caching of user contacts and corrected SQL query to load them when context has more than 1000 users.
* Thu Mar 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #18683: Checking for POP3 account before performing delete listener actions
* Wed Mar 16 2011 - thorben.betten@open-xchange.com
 - Bugfix #18617: Using specified connection to load newly created mail account
* Wed Mar 16 2011 - marcus.klein@open-xchange.com
 - Bugfix #18643: Fixed mail accounts without transport server for IPv6 support.
* Tue Mar 15 2011 - marcus.klein@open-xchange.com
 - Bugfix #16324: Supporting IPv6 addresses for IMAP and SMTP backend connections.
 - Bugfix #17217: Removed not documented and not used preferences items: /modules/olox20/module and /modules/filestorage/module.
 - Bugfix #18640: Re-added erroneously removed import.
 - Bugfix #18636: Removed filestorage module from the preferences tree. Setting the module of olox20 to false so the UI does not try to load
   a plugin.
* Fri Mar 11 2011 - marcus.klein@open-xchange.com
 - Bugfix #18596: Not starting system bundle fragment to export xerces for IBM Java.
* Fri Mar 11 2011 - thorben.betten@open-xchange.com
 - Bugfix #18585: Be sure newly created POP3 folder is unsubscribed in backing mail account
 - Bugfix #18599: Handling UnsupportedCharsetException on bundle start-up
* Fri Mar 11 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18576: Fix some shutdown exceptions.
 - Bugfix #18562: Some cleanup to tie in oauth accounts into our regular secret/crypto/secretMigration mechanisms.
 - Bugfix #18571: Use oauth namespace in osgi events in oauth bundle.
* Thu Mar 10 2011 - thorben.betten@open-xchange.com
 - Bugfix #18552: Fallback to mail login/password if transport ones are missing
* Wed Mar 09 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18525: Make sure to select only one row in IDGenerator.
* Tue Mar 08 2011 - marcus.klein@open-xchange.com
 - Bugfix #18532: Correctly initializing the prepared statement for removing Facebook messaging account when Facebook OAuth account is
   removed.
* Mon Mar 07 2011 - steffen.templin@open-xchange.com
 - Bugfix #18490: Encode folder names in UTF-7
* Mon Mar 07 2011 - thorben.betten@open-xchange.com
 - Bugfix #18512: Drop possible cached locale-sensitive folder data on changed language
* Sun Mar 06 2011 - thorben.betten@open-xchange.com
 - Bugfix #18498: Introduced listener framework for OAuth account deletion
* Sat Mar 05 2011 - thorben.betten@open-xchange.com
 - Bugfix #18036: Introduced special bit to check for rename permission
 - Bugfix #18285: Preserve plain text signature delimiter
 - Bugfix #18448: Allowing to remove SMTP login/password field for external mail account
* Fri Mar 04 2011 - francisco.laguna@open-xchange.com
 - Bugfix #17638: Use entire primary key on updates to oxfolder_permissions in long running transactions, so as not to lock too many rows.
* Fri Mar 04 2011 - marcus.klein@open-xchange.com
 - TA5815 of US 5768: Moved the HTTP authorization header based login to another URL and added correct HTTP unauthorized response.
* Fri Mar 04 2011 - thorben.betten@open-xchange.com
 - Bugfix #18502: Checking POP3 INBOX folder's open status before closing it
* Thu Mar 03 2011 - marcus.klein@open-xchange.com
 - Bugfix #18499: Passing all configuration options for the login servlet through the ServletConfig.
* Wed Mar 02 2011 - marcus.klein@open-xchange.com
 - Bugfix #18487: Adding all necessary information to a task when triggering the event for accepting, declining etc.
* Wed Mar 02 2011 - tobias.prinz@open-xchange.com
 - Bugfix #18094: "other" address information is now exported to VCards (using type=dom) - and imported this way,too.
 - Bugfix #18482: Importing CSV works even with UTF8-encoded files that insist on using a Byte Order Mark.
* Wed Mar 02 2011 - steffen.templin@open-xchange.com
 - Bugfix #17327: Alarm time of appointments is not delivered with all request.
* Tue Mar 01 2011 - marcus.klein@open-xchange.com
 - Bugfix #18465: Compiling sources everywhere to Java5 compatible class files.
 - Bugfix #18463: Added necessary imports for OAuth LinkedIn bundle.
* Tue Mar 01 2011 - choeger@open-xchange.com
 - Bugfix #18493: [L3] Open-xchange init scripts don't display results on
   RHEL6 system with special package
* Mon Feb 28 2011 - marcus.klein@open-xchange.com
 - Bugfix #17892: Task creator now gets notification mails about changed participants states.
 - Bugfix #18263: Added links for documentation of mobile sync configuration on Android devices.
* Mon Feb 28 2011 - steffen.templin@open-xchange.com
 - Bugfix #18442: Temporary files now are deleted after creating an infostore item with file upload.
* Sat Feb 26 2011 - thorben.betten@open-xchange.com
 - Bugfix #18453: Converting unicode representation of primary email address to ASCII for com.openexchange.mail.loginSource=mail
 - Bugfix #18429: Using proper default folder indexes
* Thu Feb 24 2011 - thorben.betten@open-xchange.com
 - Bugfix #18423: Checking for possible wrong content type for uploaded file
 - Bugfix #18387: Fall-back error message if exception does not provide invalid addresses
* Wed Feb 23 2011 - thorben.betten@open-xchange.com
 - Bugfix #18396: Proper IMAP folder cache invalidation
* Wed Feb 23 2011 - steffen.templin@open-xchange.com
 - Bugfix #18204: The modification of a tasks recurrence information from 'after x times' to 'on date' is working correctly now.
* Wed Feb 23 2011 - francisco.laguna@open-xchange.com
 - Bugfix #18124: Escape backslashes for searches.
* Wed Feb 23 2011 - marcus.klein@open-xchange.com
 - Bugfix #18309: Using proper host names for UDP push remote host register package.
* Wed Feb 23 2011 - steffen.templin@open-xchange.com
 - Bugfix #18219: Unable to access publications created by PIM users.
* Tue Feb 22 2011 - martin.herfurth@open-xchange.com
 - Bugfix #18336: Fixed wrong end date for when removing sequence.
* Tue Feb 22 2011 - martin.herfurth@open-xchange.com
 - Bugfix #18455: Error messages in List-view.
* Mon Feb 21 2011 - marcus.klein@open-xchange.com
 - Bugfix #18399: Added session identifier to warning message for further debugging the cause of the warning.
* Mon Feb 21 2011 - thorben.betten@open-xchange.com
 - Bugfix #18052: Checking presence of datagram package's payload
* Fri Feb 18 2011 - thorben.betten@open-xchange.com
 - Bugfix #18374: Using ConfigurationService to detect default charset
 - Bugfix #18376: Dealing with an InputStream content
* Thu Feb 17 2011 - marcus.klein@open-xchange.com
 - Bugfix #18375: Corrected upgrade code for OXUpdater
* Thu Feb 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #18379: Fixed MANIFEST.MF of JavaMail library
* Wed Feb 16 2011 - thorben.betten@open-xchange.com
 - Bugfix #18280: Using wrapping MailMessage instance
 - Bugfix #18296: Introduced mail property to hide POP3 storage folders
* Wed Feb 16 2011 - marcus.klein@open-xchange.com
 - Bugfix #18312: Introduced quotes for OXUpdater install parameters.
 - Bugfix #18154: OXUpdater is now able to update itself.
* Tue Feb 15 2011 - thorben.betten@open-xchange.com
 - Bugfix #18165: Less strict parsing of address header
* Mon Feb 14 2011 - thorben.betten@open-xchange.com
 - Bugfix #18299: Setting proper locale-sensitive folder names
 - Bugfix #18329: Fixed ArrayIndexOutOfBoundsException in jTidy library
 - Bugfix #18302: Replacing URL code point
* Thu Feb 10 2011 - thorben.betten@open-xchange.com
 - Bugfix #18292: Fixed folder update
 - Bugfix #18281: Error response from server if authentication of POP3 account fails
* Tue Feb 08 2011 - thorben.betten@open-xchange.com
 - Bugfix #18291: Fixed NPE on call to an unregistered servlet path
* Mon Feb 07 2011 - marcus.klein@open-xchange.com
 - TA5815 of US 5768: Added a HTTP authorization header based login.
* Fri Feb 04 2011 - thorben.betten@open-xchange.com
 - Bugfix #18212: Retry with read-write connection if read-only connection fails to read newly created mail account
 - Bugfix #18155: Quoting "NIL" argument
* Thu Feb 03 2011 - thorben.betten@open-xchange.com
 - Bugfix #18229: Splitting IMAP SORT command to fit into max. allowed length
 - Bugfix #18232: Fixed AJP ping
 - Bugfix #18147: Fixed permission of Unified Mail's INBOX folder
 - Bugfix #17951: i18n of Unified Mail's default folders
 - Bugfix #18072: Fixed mail access counter
* Tue Feb 01 2011 - marcus.klein@open-xchange.com
 - TA5815 of US 5768: Added the easy login request to the login servlet. This makes the additional easylogin servlet obsolete.
* Tue Jan 25 2011 - thorben.betten@open-xchange.com
 - Bugfix #18080: Considering module when checking public folder access
* Sat Jan 22 2011 - thorben.betten@open-xchange.com
 - Bugfix #18012: Checking for multiple-mail-account permission prior to listing accounts in Outlook folder tree
* Thu Jan 20 2011 - martin.herfurth@open-xchange.com
 - Bugfix #17902: Closed SQL Connection was used.
* Tue Jan 18 2011 - choeger@open-xchange.com
 - Bugfix #18044: wrong file ownerships after initial installation
* Tue Jan 18 2011 - martin.herfurth@open-xchange.com
 - Bugfix #17535: Update of Yearly Series does no longer destroy the appointment.
* Mon Jan 17 2011 - thorben.betten@open-xchange.com
 - Bugfix #18035: Allowing surrounding quotes when parsing Content-Type header
* Fri Jan 14 2011 - thorben.betten@open-xchange.com
 - Bugfix #17997: Throwing appropriate error
 - Bugfix #17991: Removing script tags in HTML header prior to obtaining a validated HTML representation
* Thu Jan 13 2011 - thorben.betten@open-xchange.com
 - Bugfix #17989: Fixed caching in IMAP MAL implementation to return cloned objects to not store modifications
* Wed Jan 12 2011 - martin.herfurth@open-xchange.com
 - Bugfix #17883: Reminder in shared folders.
* Wed Jan 12 2011 - marcus.klein@open-xchange.com
 - TA5701 of US 5504: Made session handling more restrictive. Hash used in cookies will now be recalculated for every request.
* Wed Jan 12 2011 - thorben.betten@open-xchange.com
 - Bugfix #17891: Fixed possible IndexOutOfBounsdException when checking quotes in personal part of an email address
* Tue Jan 11 2011 - choeger@open-xchange.com
 - Bugfix #18000: updating on RHEL and SLES does not work
* Tue Jan 11 2011 - marcus.klein@open-xchange.com
 - Bugfix #18004: Allowing HTML italic tag through whitelist.properties.
* Mon Jan 10 2011 - choeger@open-xchange.com
 - Bugfix #17769: [L3] /tmp/.OX must be automatically re-created when tmpwatch deletes it
   /tmp/.OX is now /var/spool/open-xchange/uploads/ per default on new installations
* Mon Jan 10 2011 - thorben.betten@open-xchange.com
 - Bugfix #17803: Removed obsolete property "com.openexchange.mail.maxNumOfConnections" from mail module
 - Bugfix #17920: Invalidate IMAP folder cache when accessing unseen message
* Mon Jan 10 2011 - martin.herfurth@open-xchange.com
 - Bugfix #17890: Imip invitations for newly added external participants.
* Mon Jan 10 2011 - marcus.klein@open-xchange.com
 - Bugfix #17924: Using correct identifier when deleting data from database if a user is removed.
* Sat Jan 08 2011 - thorben.betten@open-xchange.com
 - Bugfix #17976: Returning empty in-memory tree if there is no entry for current user
* Fri Jan 07 2011 - tobias.prinz@open-xchange.com
 - Bugfix #17392: ICal exports now contain the timezone of the appointment on dates, so they are not "floating" (RFC5545) any more.
* Mon Jan 03 2011 - tobias.prinz@open-xchange.com
 - Bugfix #17937: If an import cannot be done because the UUID is in use, you now get to know which UUID is the problem
* Mon Dec 27 2010 - thorben.betten@open-xchange.com
 - Bugfix #17882: Retry fetching POP3 server's capabilities if cached check indicates failure
* Tue Dec 21 2010 - thorben.betten@open-xchange.com
 - Bugfix #17876: Fixed illegal monitor state when waiting on Condition instance
 - Bugfix #17877: Dealing with possible InterruptedException during concurrent loading of folder permissions
* Mon Dec 20 2010 - thorben.betten@open-xchange.com
 - Bugfix #17845: Ensure proper content type header on JSON response
 - Bugfix #17800: Proper check for User instance when fetching from cache
 - Bugfix #17833: Fixed JavaDoc
* Thu Dec 16 2010 - thorben.betten@open-xchange.com
 - Bugfix #17832: Fixed IndexOutOfBoundsException
 - Bugfix #17812: Enhanced JavaDoc
 - Bugfix #17649: Opening JavaMail folder if necessary
* Wed Dec 15 2010 - thorben.betten@open-xchange.com
 - Bugfix #17830: Detecting broken AJP cycles and closing socket if so
* Tue Dec 14 2010 - thorben.betten@open-xchange.com
 - Bugfix #17790: Disabled hard-coded setting of log levels for LoginPerformer and SessionHandler classes
 - Bugfix #17817: Fixed NPE
* Tue Dec 14 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17751: Set up cleanup Handler for infostore folder publications.
* Mon Dec 13 2010 - thorben.betten@open-xchange.com
 - Bugfix #17753: Ensured proper parent for shared database folders
* Mon Dec 13 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17787: Moved domain name generation for publication specific domain to a more centralized location (so they also work in multiple requests).
* Sun Dec 12 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17756: Ensure closing of HTTP connections. Don't use a single client and avoid locks.
 - Bugfix #17782: Take encryption password from a property.
 - Bugfix #17798: Leave JSESSIONID cookie untouched in action=redirect.
* Thu Dec 09 2010 - thorben.betten@open-xchange.com
 - Bugfix #17721: Throwing a more meaningful error on IMAP protocol error
* Wed Dec 08 2010 - thorben.betten@open-xchange.com
 - Bugfix #17758: Fixed NPE
 - Bugfix #17722: Fixed renaming of folders created in POP3 account
 - Bugfix #17762: Allowing "editpassword" module access for all module access combinations (webmail, pim, etc.)
 - Bugfix #17723: Updated to new twitter OAuth authorization URL
* Wed Dec 08 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17662: Changing recurrence type.
* Wed Dec 08 2010 - choeger@open-xchange.com
 - Bugfix #17735: [L3] max open files not set for gw process on rhel and sles
* Tue Dec 07 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17191: NullPointer during TimeZone evaluation.
* Tue Dec 07 2010 - thorben.betten@open-xchange.com
 - Bugfix #17689: Introduced batch-loading of folders, users, and user configurations
 - Bugfix #17714: Throwing a more generic error message to not confuse the user with cryptic database problems
* Mon Dec 06 2010 - thorben.betten@open-xchange.com
 - Bugfix #17684: Fixed checking duplicate name on folder creation/rename
* Thu Dec 02 2010 - thorben.betten@open-xchange.com
 - Bugfix #17712: Adapted to HTTP status codes
* Wed Dec 01 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17264: Reminder for shared calender folders.
* Tue Nov 30 2010 - thorben.betten@open-xchange.com
 - Bugfix #17641: Proper commit of used connection
* Tue Nov 30 2010 - choeger@open-xchange.com
 - Bugfix #17679: Can't update system because open-xchange-file-storage-config
   searches for twitter.properties
* Mon Nov 29 2010 - thorben.betten@open-xchange.com
 - Bugfix #17658: No poll (take with timeout) on a submitted task
 - Bugfix #17557: Escaped curly brace in pattern notation
* Mon Nov 29 2010 - marcus.klein@open-xchange.com
 - Bugfix #17640: Corrected HTTP status codes of free/busy interface.
* Fri Nov 26 2010 - thorben.betten@open-xchange.com
 - Bugfix #17653: Changed log level to warning
 - Bugfix #17647: Dropping cookies on failed IP check
* Fri Nov 26 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17596: Filter out subscriptions on folders that are not visible
* Thu Nov 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #17213: Added "text/directory" as accepted VCard content type
* Thu Nov 25 2010 - tobias.prinz@open-xchange.com
 - Bugfix #17562: Internal users are now properly recognized even if they are are referred to by e-mails with strange capitalization.
* Thu Nov 25 2010 - marcus.klein@open-xchange.com
 - Bugfix #17349: Log levels of classes LoginPerformer and SessionHandler should now always be INFO to be able to follow session life times.
* Wed Nov 24 2010 - thorben.betten@open-xchange.com
 - Bugfix #17627: Changed connection handling in mail folder storage
 - Bugfix #17596: Enhanced error message
 - Bugfix #17623: Showing all subfolders
* Wed Nov 24 2010 - marcus.klein@open-xchange.com
 - Bugfix #17632: Fixed a typo in an exception message.
* Mon Nov 22 2010 - thorben.betten@open-xchange.com
 - Bugfix #17605: Stripping JavaScript contained in href attribute from HTML tag
* Mon Nov 22 2010 - marcus.klein@open-xchange.com
 - Bugfix #17608: User contacts are not mixed up anymore when loading them through an all request on the users interface.
* Fri Nov 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #17523: Update tasks can now be excluded for administration daemon, too.
* Fri Nov 19 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17526: Fixed Calendar Printing.
* Fri Nov 19 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17517: Get the file size for quota management before deleting a file.
* Fri Nov 19 2010 - thorben.betten@open-xchange.com
 - Bugfix #17490: Removed folder permission check and delegating permission check to file storage layer
 - Bugfix #17561: Fixed folder creation on a Courier IMAP server
* Thu Nov 18 2010 - thorben.betten@open-xchange.com
 - Bugfix #17563: Ensured calling thread opened mail access before closing it
* Wed Nov 17 2010 - steffen.templin@open-xchange.com
 - Bugfix #17520: Remove old login and autologin cookies in redirects.
 - Bugfix #17568: Errors while removing session bound images.
* Wed Nov 17 2010 - thorben.betten@open-xchange.com
 - Bugfix #17329: Using a blocking queue to collect addresses
 - Bugfix #17571: Returning ISO-8859-1 charset if detector indicates "nomatch"
 - Partial fix for bug #17292: Grouping listed folders by their folder storage to get those folders by one
 - Bugfix #17423: Deleting distribution list entries which refer to deleted user
* Wed Nov 17 2010 - marcus.klein@open-xchange.com
 - Bugfix #17539: Improved the performance when get all contacts of the global address book.
* Tue Nov 16 2010 - thorben.betten@open-xchange.com
 - Partial fix for bug #17203: Added "; HttpOnly" flag to server cookies and added "cookielifetime" to config tree
* Tue Nov 16 2010 - karsten.will@open-xchange.com
 - Bugfix #17081: XING subscription failed (sometimes)
 - Bugfix #16834: Google calendar crawler failed
* Mon Nov 15 2010 - tobias.prinz@open-xchange.com
 - Bugfix #17492: Ignoring the SCHEDULE-AGENT parameter in an ical file now instead of considering the file to be broken.
* Mon Nov 15 2010 - thorben.betten@open-xchange.com
 - Bugfix #17551: Fixed NPE if no session is found for mail filter request
* Fri Nov 12 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17519: NullPointerException during sync fixed..
* Fri Nov 12 2010 - choeger@open-xchange.com
 - Bugfix #17518: open-xchange-file-storage-config is missing a dependency
* Fri Nov 12 2010 - thorben.betten@open-xchange.com
 - Bugfix #17527: No END_REPONSE package after a CPong response
* Thu Nov 11 2010 - thorben.betten@open-xchange.com
 - Bugfix #17471: SocketTimeoutException's message is no longer part of the condition to check a failed IMAP connect
 - Bugfix #17498: Proper cache invalidation when switching unseen flag
* Thu Nov 11 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17449: Made code a bit more robust against null values.
 - Bugfix #17501: com.openexchange.subscribe.subscriptionFlag honors both fullname and id
* Thu Nov 11 2010 - choeger@open-xchange.com
 - Bugfix #13480: DNS is not rediscovered for E-Mail servers
* Thu Nov 11 2010 - karsten.will@open-xchange.com
 - Bugfix #17441: GMX.com URL changed again
* Wed Nov 10 2010 - thorben.betten@open-xchange.com
 - Bugfix #17223: Replacing "ISO-2022-JP" charset with "CP50220" on charset encoding
 - Bugfix #17316: Setting proper HTTP headers on .docx attachment download
* Wed Nov 10 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17459: Delegate to correct method when saving a new infoitem.
* Wed Nov 10 2010 - steffen.templin@open-xchange.com
 - Bugfix #17227: Wrong end date in confirmation mails.
* Tue Nov 09 2010 - thorben.betten@open-xchange.com
 - Bugfix #17480: Proper mapping of "infostore" module to not mix up with new file storage folders
* Mon Nov 08 2010 - thorben.betten@open-xchange.com
 - Bugfix #17173: Adding space after formatting HTML tag like "em" or "strong"
 - Partial fix for bug #17415: Delivering content for inline text attachments
 - Bugfix #15476: Replaced favicon image URL with proxied one
* Fri Nov 05 2010 - choeger@open-xchange.com
 - Bugfix #17433: Missing dependency for filestorage related infostore packages
* Fri Nov 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #17389: Corrected SQL statement for removing the passcrypt user attribute.
 - Bugfix #17372: Notifications for imported iCal appointments can now be suppressed by using a optional parameter for the request.
* Thu Nov 04 2010 - thorben.betten@open-xchange.com
 - Bugfix #17421: No duplicate return to connection pool
* Thu Nov 04 2010 - marcus.klein@open-xchange.com
 - Bugfix #17425: Added new server dependency and added files to spec file for c.o.file.storage.composition for RPM based distributions.
* Thu Nov 04 2010 - tobias.prinz@open-xchange.com
 - Bugfix #16895: Ignoring the non-RFC element EMAIL in an iCal file ATTENDEE property
* Wed Nov 03 2010 - thorben.betten@open-xchange.com
 - Bugfix #17420: Ignoring file storage accounts which do not provide a root folder
* Tue Nov 02 2010 - tobias.prinz@open-xchange.com
 - Bugfix #17203 on server side: Added action=refreshSecret to Login servlet which allows to extend the expiry date for a secret cookie.
* Fri Oct 29 2010 - thorben.betten@open-xchange.com
 - Bugfix #17345: Changed log level to warning
 - Bugfix #17231: Proper parsing of address header "Disposition-Notification-To"
 - Bugfix #17212: Fixed NPE in kXML library
 - Bugfix #17337: Removing 'base' tag from whitelist.properties to properly display referenced images
* Tue Oct 26 2010 - tobias.prinz@open-xchange.com
 - Userstory 5247: Publications can now be configured to run on different (sub-)domains, HTML white-listing can be disabled.
* Tue Oct 26 2010 - tobias.prinz@open-xchange.com
 - Userstory 5244: If an internal user is added as an external participant, they get transformed into an internal one automagically.
* Mon Oct 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #17330: Proper folder fullname when renaming a folder on root level
 - Bugfix #17179: Handling an OutOfMemoryError on thread start-up as a rejected execution event
* Fri Oct 22 2010 - marcus.klein@open-xchange.com
 - Bugfix #17304: IMAP server ACL access identifier are now properly resolved if a global imap server configuration is used.
* Thu Oct 21 2010 - thorben.betten@open-xchange.com
 - Bugfix #17161: Fixed NPE
* Wed Oct 20 2010 - steffen.templin@open-xchange.com
 - Bugfix #17072: Contact field 'Profession' not exported to VCards.
* Wed Oct 20 2010 - steffen.templin@open-xchange.com
 - Bugfix #17261: Duplicate folder exception is thrown for synchronizing contact folder 'Vorgeschlagene Kontakte' with Outlook 2010.
* Mon Oct 18 2010 - marcus.klein@open-xchange.com
 - Bugfix #17275: Throwing an exception in recurrence type is not known instead of returning a null recurring results.
* Fri Oct 15 2010 - marcus.klein@open-xchange.com
 - Bugfix #17242: Made logger mandatory for AbstractIndexCallable to prevent possible NullPointerExceptions.
* Thu Oct 14 2010 - marcus.klein@open-xchange.com
 - Bugfix #16833: Added publication target name strings to I18N process.
 - Bugfix #17162: Using consistent codes for not visible and not found folders.
* Thu Oct 14 2010 - martin.herfurth@open-xchange.com
 - Bugfix #17662: Remove of occurrences value.
* Wed Oct 13 2010 - marcus.klein@open-xchange.com
 - Bugfix #17131: Changed object events now contain every user identifier and folder identifier that might be affected.
 - Bugfix #17230: Using the mainstream tool method to get a time zone.
* Tue Oct 12 2010 - choeger@open-xchange.com
 - Bugfix #17237 - Incorrect file permissions for configuration files in
   packages -twitter and -mailfilter
* Fri Oct 08 2010 - thorben.betten@open-xchange.com
 - Bugfix #17199: Proper cache invalidation after folder move operation
 - Bugfix #17161: Immediate delivery of delete event for mails
 - Bugfix #17198: Proper respect to property "com.openexchange.mail.ignoreSubscription"
* Fri Oct 08 2010 - steffen.templin@open-xchange.com
 - Bugfix #17195: Malformed URL-Parameters in HTML-Mails
* Fri Oct 08 2010 - steffen.templin@open-xchange.com
 - Bugfix #16634: Searching in public calendar results only show appointments where user is participant
* Thu Oct 07 2010 - karsten.will@open-xchange.com
 - Bugfix #17197: Crawler updates missing
* Thu Oct 07 2010 - francisco.laguna@open-xchange.com
 - Bugfix #17197: Bugfix #17196: Avoid NPE in shutdown of com.openexchange.secret.recovery.json bundle.
* Thu Oct 07 2010 - karsten.will@open-xchange.com
 - Bugfix #16734: Xing subscription stops
* Tue Oct 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #17027: Correctly sending deleted folder identifier in folder updates response.
* Fri Oct 01 2010 - thorben.betten@open-xchange.com
 - Bugfix #17027: Consistent folder structure in database
* Thu Sep 30 2010 - thorben.betten@open-xchange.com
 - Bugfix #17107: Direct instantiation of POP3Access instance
* Wed Sep 29 2010 - marcus.klein@open-xchange.com
 - Bugfix #17063: Removed WWW-Authenticate header proclaiming the backend supports digest authentication.
* Tue Sep 28 2010 - choeger@open-xchange.com
 - Bugfix #17031: open-xchange-folder-json package MUST be installed with
   6.18.rev2 but is not a dependency
* Tue Sep 28 2010 - marcus.klein@open-xchange.com
 - Bugfix #17049: Fixed wrongly installed bundle files for new file storage bundle.
* Mon Sep 27 2010 - thorben.betten@open-xchange.com
 - Bugfix #17046: Fixed closing of FolderObjectIterator
* Fri Sep 24 2010 - thorben.betten@open-xchange.com
 - Bugfix #16968: Returning zero number of sessions if SessiondService is missing
* Wed Sep 22 2010 - thorben.betten@open-xchange.com
 - Bugfix #16870: Showing inline images if content-type set to "application/octet-stream"
* Wed Sep 22 2010 - steffen.templin@open-xchange.com
 - Bugfix #16720: Making an existing appointment recurring causes disappearance in Outlook.
* Wed Sep 22 2010 - marcus.klein@open-xchange.com
 - Bugfix #16962: Fixed the not used cache for users.
 - Bugfix #16996: Workaround for some outdated contact columns.
* Wed Sep 22 2010 - tobias.prinz@open-xchange.com
 - Bugfix 16975: Whitelisting for publications is now defined in microformatWhitelisting.properties and more tolerant than before, allowing paths to CSS files.
* Mon Sep 20 2010 - thorben.betten@open-xchange.com
 - Bugfix #16800: Replacing non-ascii URLs with proper puny-code-encoded URLs
* Fri Sep 17 2010 - thorben.betten@open-xchange.com
 - Bugfix #15476: Replacing image URLs with a safe proxied URI in RSS messages
 - Bugfix #16894: Fixed ClassCastException
* Wed Sep 15 2010 - steffen.templin@open-xchange.com
 - Bugfix 16402: Receiving contact images should not cause 404 errors anymore.
* Tue Sep 14 2010 - thorben.betten@open-xchange.com
 - Bugfix #16857: No folder deletion on special IMAP error code "NO_ADMINISTER_ACCESS_ON_INITIAL"
* Tue Sep 14 2010 - tobias.prinz@open-xchange.com
 - Bugfix 16826: Publish-Templates now go through a whitelisting process to filter out potential harmful code.
* Mon Sep 13 2010 - tobias.prinz@open-xchange.com
 - User story #5212: Both resources and groups now allow to be queried for updates, just like other elements. See HTTP API for details.
* Mon Sep 13 2010 - marcus.klein@open-xchange.com
 - Bugfix #16796: Fixed creating a task without start and end date through first generation Outlook OXtender.
* Fri Sep 10 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16151: Move appointment from shared to private folder.
* Fri Sep 10 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16848: Use correct date format in response headers.
 - Bugfix #16846: Easy Login transmits "autologin" parameter to redirect method, which in turn forwards it to the UI, that, then will issue
                  store request.
* Fri Sep 10 2010 - thorben.betten@open-xchange.com
 - Bugfix #16809: Changed error message if twitter consumer key/secret pair is invalid.
                  Changed default consumer key/secret to Open-Xchange application.
 - Bugfix #16843: Fixed URLs parsing in plain text
* Fri Sep 10 2010 - marcus.klein@open-xchange.com
 - Bugfix #16763: Not throwing a session expired exception anymore if loading context or user fails.
* Thu Sep 09 2010 - marcus.klein@open-xchange.com
 - Bugfix #16835: post install of crawler bundle is now able to deal with filenames containing a white space character.
* Thu Sep 09 2010 - thorben.betten@open-xchange.com
 - Bugfix #15681: Checking POP3 account's default folders on path creation in primary mail account
* Tue Sep 07 2010 - choeger@open-xchange.com
 - Bugfix #16815: activation and jcharset packages to use OX versioning
* Fri Sep 03 2010 - thorben.betten@open-xchange.com
 - Bugfix #16776: Changed twitter API to use OAuth
 - Bugfix #16786: Support of field 'image1_content_type' in all request
 - Bugfix #16723: No appending of Href content for text-only mails
* Fri Sep 03 2010 - marcus.klein@open-xchange.com
 - Bugfix #16805: Added dependency from publish microformats component to templating json interface because UI using microformats to publish
   like to list available templates.
* Thu Sep 02 2010 - marcus.klein@open-xchange.com
 - Bugfix #16532: Update task fixes primary key on table publication and table publication_users is tried be created again. Additionally
   possibly wrong primary key on table subscriptions is fixed, too.
* Thu Sep 02 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15903: Removing of Participants.
* Tue Aug 31 2010 - thorben.betten@open-xchange.com
 - Bugfix #16762: No blind future use of previously passed connection
* Mon Aug 30 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16548: Reccurrence type 0 for normal appointments.
* Thu Aug 26 2010 - thorben.betten@open-xchange.com
 - Bugfix #16742: Fixed folder display of namespace folders
* Thu Aug 26 2010 - marcus.klein@open-xchange.com
 - Bugfix #16455: Implemented a more sophisticated search for a participant when using freebusy interface. Resources are now preferred if a
   user has the same mail address as alias.
* Wed Aug 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #16718: Fixed implementation of HttpServletRequest.getRequestURL()
* Wed Aug 25 2010 - steffen.templin@open-xchange.com
 - US4027: Backend provides the possibility to get all publications of a user.
* Tue Aug 24 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16714: No change notifications for category changes.
* Mon Aug 23 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16579: New Until for Appointment Series.
* Sun Aug 22 2010 - thorben.betten@open-xchange.com
 - Bugfix #16708: Removed 'final' modifier from checkFieldsBeforeConnect() method to allow overriding in concrete MAL implementations
* Fri Aug 20 2010 - thorben.betten@open-xchange.com
 - Bugfix #16410: Fixed TLS connection to SMTP server
* Thu Aug 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #16700: Fixed NullPointerException if no I18nService can be found for a specific locale in messaging component.
* Wed Aug 18 2010 - marcus.klein@open-xchange.com
 - Bugfix #16620: Contact collect folder can now be enabled again because options are always visible.
* Tue Aug 17 2010 - thorben.betten@open-xchange.com
 - Bugfix #16693: Fixed setting "subscr_subflds" field for shared folder
 - Bugfix #13785: Proper calculation of modified and "deleted" folders for XML/WebDAV interface
 - Partial fix for #16688: Increased space of PermGen heap section
* Tue Aug 17 2010 - marcus.klein@open-xchange.com
 - Bugfix #16615: Warning of exceeded database connection pool is only written once a minute.
 - Bugfix #16681: Translating form labels for dynamic forms sent by the back end for subscriptions and messaging.
* Tue Aug 17 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16689: Notifications for appointments which start in the past.
* Mon Aug 16 2010 - francisco.laguna@open-xchange.com
 - Add whitelist capability to ip check.
* Mon Aug 16 2010 - tobias.prinz@open-xchange.com
 - Bugfix #16613: Ignoring ID element in ATTACH in an ICAL file.
* Fri Aug 13 2010 - thorben.betten@open-xchange.com
 - Bugfix #16618: Allowing contact image URL on action=all
* Thu Aug 12 2010 - thorben.betten@open-xchange.com
 - Bugfix #16669: Proper quoting of regex replacement
* Wed Aug 11 2010 - steffen.templin@open-xchange.com
 - Bugfix #16643: Wrong series information in notification mails
* Fri Aug 06 2010 - steffen.templin@open-xchange.com
 - Bugfix #16515: fileas in contacts will be set correctly after creating a new contact with Outlook.
* Fri Aug 06 2010 - thorben.betten@open-xchange.com
 - Bugfix #16655: Fixed sorting of (infostore) folders in classic folder tree
* Tue Aug 03 2010 - thorben.betten@open-xchange.com
 - Bugfix #16495: Added config parameter to decide whether to add client's IP address to mail headers on
                  delivery as custom header "X-Originating-IP"
* Mon Aug 02 2010 - thorben.betten@open-xchange.com
 - Bugfix #16614: Fixed NPE in FolderObjectIterator class
 - Bugfix #16616: Using a blocking queue to avoid possible OutOfMemory error due to creation of too many threads
 - Bugfix #16531: No translation of IMAP folder names
* Fri Jul 30 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15138: Made a log message, complaining, that the server can't reach an external subscription service definition, more meaningful.
 - Bugfix #16351: Complain louder about missing property in templating service.
 - Bugfix #15302: Make subscription parser more resilient.
* Thu Jul 29 2010 - karsten.will@open-xchange.com
 - Bugfix #16591: Error loading JavaScript from [https://www.gmx.com/client/static/script/compiled-gecko-17-821660071.js]
* Thu Jul 29 2010 - marcus.klein@open-xchange.com
 - Bugfix #16532: Using correct database connection for creating table.
* Thu Jul 29 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16575: Survive inability to decode passwords in messaging subsystem
* Thu Jul 29 2010 - thorben.betten@open-xchange.com
 - Bugfix #16557: And additional > after a link as produced by the Mulberry mail client is not included in the link
* Wed Jul 28 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16249: Event for changed Attachments.
 - Bugfix #16540: Event for changed Alarm.
* Wed Jul 28 2010 - marcus.klein@open-xchange.com
 - Bugfix #16571: Extended logging to see the nested exceptions stack trace if this occurs again.
* Tue Jul 27 2010 - marcus.klein@open-xchange.com
 - Bugfix #16577: Fixed ClassCastException in FolderCache due to newly introduced conditional loading of folder objects.
 - Bugfix #16582: Fixed a NullPointerException if a task notification should be sent and the task does not have an end date.
* Mon Jul 26 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16553: Correct timer arrays length to avoid NPE.
* Mon Jul 26 2010 - marcus.klein@open-xchange.com
 - Bugfix #16558: Fixed a NullPointerException in FolderCacheManager when putting loaded folders through an FolderObjectIterator into the
   cache.
 - Bugfix #16561: Fixed a cleared prefetch structure in FolderObjectIterator when it should not be cleared because it is needed afterwards.
* Thu Jul 22 2010 - marcus.klein@open-xchange.com
 - Bugfix #16545: Revoking optimization for loading folder permissions along with finding the folder.
* Thu Jul 22 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16547: Get rid of context based methods in ImageService (and underlying implementation), bind session to image uids
* Wed Jul 21 2010 - choeger@open-xchange.com
 - Bugfix #16529: Errors when updating OX Packages on RHEL5
* Wed Jul 21 2010 - steffen.templin@open-xchange.com
 - Bugfix #16492: Setting a reminder for a sequence with first occurrence in the past and next occurrence in the future works now for insert
   appointments.
* Wed Jul 21 2010 - tobias.prinz@open-xchange.com
 - US1601 - serverside: ALL and LIST requests for appointments now have a new parameter called "showPrivate" (default: false). If it is set
   to true, private appointments in shared folders are also returned, but only as anonymized appointments (lacking all information except
   start and end date).
* Wed Jul 21 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16507: Ignore timezones when writing notification mails about appointments lasting the whole day. Artificially move the end date
   of an appointment to the previous day, if it is a whole day appointment.
* Wed Jul 21 2010 - marcus.klein@open-xchange.com
 - Bugfix #16348: filename parameter in Content-disposition: attachment header confuses Safari5. Omitting this header with this browser.
* Tue Jul 20 2010 - steffen.templin@open-xchange.com
 - Bugfix 16508: Users display name will be sent within notification mails.
* Tue Jul 20 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16481: Extend field for participant confirmation comment.
* Tue Jul 20 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15669: A display name now can be part of another display, it just is not allowed to be the same.
* Tue Jul 20 2010 - marcus.klein@open-xchange.com
 - Bugfix #16379: Not using context specific locks anymore filling a map until all memory is eaten up.
* Mon Jul 19 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16441: Delete Exceptions for series where the start date differs from the first occurrence.
 - Bugfix #16476: No more private appointments in search result.
* Fri Jul 16 2010 - thorben.betten@open-xchange.com
 - Bugfix #16168: Requesting folder including permission information from database
 - Bugfix #16514: Keeping order of address headers (From, To, Cc, Bcc, ...)
 - Bugfix #16148: Improved nearly all folder database queries to include permission information and thus avoiding to request them
                  separately for each folder
* Fri Jul 16 2010 - steffen.templin@open-xchange.com
 - Bugfix #16141: On importing mails the whole import stops if one mail is corrupt.
* Thu Jul 15 2010 - thorben.betten@open-xchange.com
 - Bugfix #16422: Changed log level to WARN if collecting an invalid address fails
 - Bugfix #16483: Removed unknown import
* Wed Jul 14 2010 - thorben.betten@open-xchange.com
 - Bugfix #16496: Proper folder information on relogin through removal from folder cache
* Wed Jul 14 2010 - marcus.klein@open-xchange.com
 - Bugfix #16484: A loaded contact object now always contains the private flag.
* Wed Jul 14 2010 - tobias.prinz@open-xchange.com
 - Bugfix #16107: Changing a series that stretches more than one week/month/year from "full time" to normal does not break the view in the
   second week/month/year
* Tue Jul 13 2010 - thorben.betten@open-xchange.com
 - Bugfix #16384: Fixed order of folders below user's private folder
* Tue Jul 13 2010 - steffen.templin@open-xchange.com
 - Bugfix #16089: Confirmation status of automatically added UserParticipant in public appointments without participants is set to user
   settings default.
* Mon Jul 12 2010 - thorben.betten@open-xchange.com
 - Bugfix #16472: Keeping other attributes when replacing "src" attribute in HTML "img" tags
 - Bugfix #16461: Show every RFC822 part as a nested mail regardless of Content-Disposition header
 - Bugfix #16467: Processing embedded images nested in "background=" attribute
* Fri Jul 09 2010 - steffen.templin@open-xchange.com
 - Bugfix #15776: Setting a reminder for a sequence with first occurrence in the past and next occurrence in the future works now.
* Fri Jul 09 2010 - francisco.laguna@open-xchange.com
 - Bugfix #16447: Have Publication and Subscription User Delete listeners take part in Admins transaction.
 - Bugfix #16099: Moving first task of a series to day where first occurrence of series should be as described in the series pattern.
* Thu Jul 08 2010 - marcus.klein@open-xchange.com
 - Bugfix #16199: Adding correct content-type to attachments servlet GET requests.
 - Bugfix #16420: Allowing zero size collections for SearchIteratorDelegator.
 - Bugfix #16397: Backend transfers all available time zones to the UI.
* Wed Jul 07 2010 - marcus.klein@open-xchange.com
 - Bugfix #16421: If a database socket is once broken it should be kept broken to not compromise ResultSets from that connection.
* Wed Jul 07 2010 - tobias.prinz@open-xchange.com
 - Bugfix #16367: MS Exchange 2007 tends to send broken iCal CN data. We ignore that now.
* Tue Jul 06 2010 - marcus.klein@open-xchange.com
 - Bugfix #16342: A background update task adds an initial filestore usage for every context.
 - Bugfix #16291: Creating pooled objects is now done outside the lock for the internal pool structures. This prevents a lot of waiting
   threads if creating object takes some time.
* Tue Jul 06 2010 - steffen.templin@open-xchange.com
 - Bugfix #14111: Missing reminders for appointment series and change exceptions.
* Mon Jul 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #16105: Writing a warn message to the log file is some thread has to wait for a database connection because all are exhausted.
* Fri Jul 02 2010 - thorben.betten@open-xchange.com
 - Bugfix #16407: Added warning if parsing of multipart mail failed on mail display
* Wed Jun 30 2010 - tobias.prinz@open-xchange.com
 - Bugfix #16287: Error message made easier to understand.
* Wed Jun 30 2010 - thorben.betten@open-xchange.com
 - Bugfix #16041: Added header "Importance" when dealing with a mail's priority level
* Tue Jun 29 2010 - thorben.betten@open-xchange.com
 - Bugfix #16357: Proper registration of new folder tree's delete listener (for user deletion)
 - Bugfix #16007: Throwing appropriate error on invalid entered email addresses
 - Bugfix #16321: Management bundle deals with IPv6 addresses
 - Bugfix #16231: Deep check if INBOX has user-visible subfolders in new folder tree
 - Bugfix #15477: Default database folders made locale-sensitive
 - Bugfix #15620: Purging all listener data on bundle stop
 - Bugfix #15708: Fixed java.lang.StringIndexOutOfBoundsException in AJP module
 - Bugfix #15709: Added POP3 timeout
* Tue Jun 29 2010 - steffen.templin@open-xchange.com
 - Bugfix #16358: Defect folder ids in reminders after moving appointments from public to private
* Tue Jun 29 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15662: As per RFC 2445, checking free/busy on an appointment marked as "FREE" returns "FREE" and not nothing at all.
* Mon Jun 28 2010 - thorben.betten@open-xchange.com
 - Bugfix #16284: Primary mail account's root folder does not exist in Outlook folder tree
 - Bugfix #16311: Proper parent identifier for shared database folders
 - Bugfix #16378: Checking empty list prior to generating IMAP number argument
 - Bugfix #16346: Added appropriate error message when authentication to SMTP server fails
 - Bugfix #16385: i18n for global address book folder
 - Bugfix #15874: Showing available content on invalid credentials to access a mail system
* Mon Jun 28 2010 - karsten.will@open-xchange.com
 - Bugfix #16334: Crawler for gmx.com does not work (again)
* Fri Jun 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #16360: No parsing of TNEF attachments when writing as structured JSON object
 - Bugfix #16273: No parsing of UUEncoded attachments (by default) when writing as structured JSON object
* Fri Jun 25 2010 - steffen.templin@open-xchange.com
 - Bugfix #16141: On importing mails the whole import stops if one mail is corrupt.
* Tue Jun 22 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16297: Fixed attachment tracking in calendar.
* Tue Jun 22 2010 - steffen.templin@open-xchange.com
 - Bugfix #15291: Adding groups with participants who already exists is working correctly now.
* Tue Jun 22 2010 - thorben.betten@open-xchange.com
 - Possible fix for bug #16273: Replaced ByteBuffer with ByteArrayInputStream to avoid an infinite blocking read() attempt
* Tue Jun 22 2010 - tobias.prinz@open-xchange.com
 - US4303: IMiP invitations to primary mail addresses of an of user now work again. They failed since 06-22 in case they were sorted before
   alias e-mail addresses on the server side.
* Mon Jun 21 2010 - choeger@open-xchange.com
 - Bugfix #16359: java-1.5.0-sun package obsoleted on rhel5
 - Bugfix #16364: Got message about not proper configured cache ports although
   they are
* Fri Jun 18 2010 - marcus.klein@open-xchange.com
 - Bugfix #15526: Other bundles can now register at caching bundle for buddy class loading to be able to put own classes into the cache.
 - Bugfix #16303: Fixed a caching problem if some users request loads anothers private folders.
* Thu Jun 17 2010 - karsten.will@open-xchange.com
 - Bugfix #16334: Crawler for gmx.com does not work
* Wed Jun 16 2010 - marcus.klein@open-xchange.com
 - Bugfix #13960: Corrected values in columns responses of appointments and contacts to get a consistent response to normal get values.
* Wed Jun 16 2010 - tobias.prinz@open-xchange.com
 - US #4303: If you get an IMiP appointment sent to an e-mail alias of yours and you accept it, you will be listed as internal participant
   instead of an external participant, gaining all the nice features of that.
* Tue Jun 15 2010 - karsten.will@open-xchange.com
 - Bugfix #16295: DefaultSenderAddress now correctly used in .ics-files for all attendees (if configured in notification.properties)
* Mon Jun 14 2010 - marcus.klein@open-xchange.com
 - Bugfix #16326: Using default error page template for EasyLogin if the configured file is not found.
* Fri Jun 11 2010 - marcus.klein@open-xchange.com
 - Bugfix #15986: Avoid a NullPointerException when updating an appointment with some special circumstances.
* Thu Jun 10 2010 - marcus.klein@open-xchange.com
 - Bugfix #16226: Default error page template for easy login returns to login page after 3 seconds.
* Thu Jun 10 2010 - viktor.pracht@open-xchange.com
  - Bugfix #16299: Unable to download infostore items
* Wed Jun 09 2010 - marcus.klein@open-xchange.com
 - Bugfix #15585: Corrected SQL query for finding the private folder information for free busy results.
* Wed Jun 09 2010 - choeger@open-xchange.com
 - Bugfix #16035: checkconfigconsistency should check if correct cache.ccf is used in system.properties
 - Bugfix #14500: Warnings due to missing LSB information added LSB Headers to debian init-script, which are not interpreted at all...
* Tue Jun 08 2010 - thorben.betten@open-xchange.com
 - Bugfix #15930: Error on creating a mail folder below a public folder
* Tue Jun 08 2010 - marcus.klein@open-xchange.com
 - Bugfix #13960: Corrected values in columns responses of appointments and contacts to get a consistent response to normal get values.
* Mon Jun 07 2010 - thorben.betten@open-xchange.com
 - Bugfix #16267: Marking shared folder as non-default folder
 - Bugfix #16228: Proper order of delete listeners and more robust implementation
 - Bugfix #15901: Allowing additional list tags in whitelist.properties (<dl>, <dt>, <dd>)
* Mon Jun 07 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15400: If import fields exceed maximum db field lengths, they are truncated instead of not imported.
* Fri Jun 04 2010 - marcus.klein@open-xchange.com
 - Bugfix #16227: If session migration takes place multiple threads asking for a session are serialized.
 - Bugfix #16224: The list of known tasks is searched for the requested update task if they should be started through JMX interface.
* Fri Jun 04 2010 - karsten.will@open-xchange.com
 - Bugfix #16244 - Wizard always checks for e-mail account, even if "Contacts only" is selected
* Fri Jun 04 2010 - steffen.templin@open-xchange.com
 - Bugfix #15229 - Escaped colons in VCards don't cause parser exceptions anymore.
* Tue Jun 01 2010 - thorben.betten@open-xchange.com
 - Bugfix #15856: Proper resource prefix in notification mail if content is multipart/*
 - Bugfix #15914: Changed module of root folder to "system"
 - Partial fix #16233: Added virtual subfolders to INBOX listing
 - Bugfix #16201: Allowing blank character in filename of a uuencoded part
 - Bugfix #16190: Shared folders have no subfolders
 - Bugfix #16234: Proper check of folder permissions
 - Bugfix #15737: Removed useless properties from javamail.properties file
 - Bugfix #16070: Only appending new-line on paragraph tag
 - Bugfix #15847: Fixed error message to handle with non-numeric mail identifiers
 - Bugfix #15845: Modified JTidy sources to avoid java.lang.StringIndexOutOfBoundsException
 - Bugfix #16238: Proper check for a shared folder
 - Bugfix #14585: Keeping inline images on reply/forward
 - Bugfix #15618: Proper update of answered/forwarded flag when selecting a different account
 - Bugfix #15642: Allowing "view" parameter from action=get for action=reply and action=forward
* Tue Jun 01 2010 - martin.herfurth@open-xchange.com
 - Bugfix #16008: Internal users as external participants.
 - Bugfix #16203: No notification mails for external users about confirmation status.
* Tue Jun 01 2010 - steffen.templin@open-xchange.com
 - Bugfix #16155: No ugly exceptions during context delete anymore.
* Tue Jun 01 2010 - marcus.klein@open-xchange.com
 - Bugfix #16158: Putting session back into first container must be done having a write lock.
* Tue Jun 01 2010 - karsten.will@open-xchange.com
 - Bugfix #16160: (Subscriptions) UID collision detected even if the user does not have any other appointments.
* Mon May 31 2010 - steffen.templin@open-xchange.com
 - Bugfix #15590: Moving an appointment to a shared folder doesn't cause "Unknown SQL-Exception" anymore.
* Mon May 31 2010 - thorben.betten@open-xchange.com
 - Bugfix #16216: Checking passed String instance for null
 - Bugfix #15898: Stripping surrounding quotes from uploaded file's Content-Type header
 - Bugfix #16132: Generating a unique value for pop3.path property
 - Bugfix #16182: Added isVisible() method to also consider administrator flag
 - Bugfix #15820: Passing limit argument to IMAP command
* Fri May 28 2010 - thorben.betten@open-xchange.com
 - Bugfix #16162: Fixed subscribe mail folder dialog
 - Bugfix #15973: Fixed action=updates for tree=1
 - Bugfix #16124: Handling already existing folder entry in virtualTree table
 - Bugfix #16114: Setting locale-sensitive name for the infostore folder names (public and userstore)
 - Bugfix #16085: Fixed name of INBOX folder
 - Bugfix #16125: Re-Create mail folder if absent
* Thu May 27 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15927: Empty contacts (or those with just one name) are not duplicated when synchronizing
* Wed May 26 2010 - marcus.klein@open-xchange.com
 - Bugfix #16194: Implemented move from public to private folder.
* Tue May 25 2010 - marcus.klein@open-xchange.com
 - Bugfix #16117: Corrected identifier check for move to public infostore folder.
* Fri May 21 2010 - steffen.templin@open-xchange.com
- Bugfix #13173: If you switch several times between done and undone of a recurring task no duplicates for the next occurrence will be
  created.
* Fri May 21 2010 - marcus.klein@open-xchange.com
 - Bugfix #16163: Path requests return now correct path for shared private folders.
 - Bugfix #16102: Implemented RSS messaging folder on the new folder tree.
* Fri May 21 2010 - steffen.templin@open-xchange.com
 - Bugfix #15113: Adding an empty group to a task now throws an exception.
* Thu May 20 2010 - karsten.will@open-xchange.com
 - Bugfix #15903: Appt is shown, even if i am no participant
* Thu May 20 2010 - marcus.klein@open-xchange.com
 - Bugfix #16087: Keeping mails as unseen now works when passing the save parameter as true on mail get request.
* Wed May 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #16027: Only set sender header of sent emails if from is not from alias list.
 - Bugfix #16026: Adding header X-Originating-IP in emails containing the IP address of the client using the backend.
 - Bugfix #15980: Folder path requests for public folders in Outlook like folder tree now have IPM_ROOT as topmost folder.
* Tue May 18 2010 - thorben.betten@open-xchange.com
 - Bugfix #15970: Showing all folders in subscribe dialog
 - Bugfix #16115: Applying locale-sensitive name to folders
 - Bugfix #16116: Setting module to "infostore" for virtual infostore folder
* Tue May 18 2010 - karsten.will@open-xchange.com
 - Bugfix #16108: Subscription to Google Calendar does not work
* Tue May 18 2010 - marcus.klein@open-xchange.com
 - Bugfix #16006: Using time zone parameter when creating a new task. This was the only task action not respecting the time zone parameter.
* Mon May 17 2010 - thorben.betten@open-xchange.com
 - Bugfix #15963: Changed text of error message
 - Bugfix #15946: No Unified Mail is not enabled
* Fri May 14 2010 - steffen.templin@open-xchange.com
- Bugfix #15740: Now using getUntil() of appointments for deleting old reminders.
* Wed May 12 2010 - marcus.klein@open-xchange.com
 - Bugfix #16063: Added messaging folder type and changed their identifier to contain the scheme.
* Wed May 12 2010 - thorben.betten@open-xchange.com
 - Bugfix #16098: Using existing defaults and laterals for later added caches.
* Tue May 11 2010 - viktor.pracht@open-xchange.com
  - Bugfix #15507: Opening iCal attachments from invitation e-mail the content is imported immediately
* Fri May 07 2010 - steffen.templin@open-xchange.com
 - Bugfix #15300: Added the possibility to use primary mail address or default sender address as from header in notification mails.
* Fri May 07 2010 - marcus.klein@open-xchange.com
 - Bugfix #16039: Corrected cache initialization to fix not working cache invalidation. More detailed logging if problem occur.
* Wed May 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #15884: Finishing task iterator pre reader even if some problem occurs.
* Tue May 04 2010 - viktor.pracht@open-xchange.com
  - TA2994 for US4375: Call history
* Tue May 04 2010 - marcus.klein@open-xchange.com
 - Bugfix #15995: Setting correct folder name for global address book.
 - Bugfix #15061: Pass warnings from the POP3 background sync process to the frontend.
* Mon May 03 2010 - marcus.klein@open-xchange.com
 - Bugfix #16021: Fixed NullPointerException on session closing if sessions random was used.
 - Bugfix #15933: Using DB master server to change the POP3 storage provider name.
* Mon May 03 2010 - thorben.betten@open-xchange.com
 - Bugfix #15975: Properly marking messages as \Deleted prior to expunge on INBOX folder
* Fri Apr 30 2010 - marcus.klein@open-xchange.com
 - Bugfix #15880: Made path to the UI fully configurable and it can be passed through parameters on easylogin and login redirect requests.
 - Bugfix #15936: Session identifier is not passed anymore as document fragment on login redirect request.
* Tue Apr 27 2010 - thorben.betten@open-xchange.com
 - Bugfix #15947: Marking messaging folders as subscribed by default.
* Mon Apr 26 2010 - marcus.klein@open-xchange.com
 - Bugfix #15937: Ignoring number of attachments if sent from client.
* Fri Apr 23 2010 - marcus.klein@open-xchange.com
 - Bugfix #15897: Prefering task identifier in URL on confirm requests.
* Tue Apr 20 2010 - karsten.will@open-xchange.com
 - Bugfix #15842: yahoo crawler auto generates the address class on import
* Mon Apr 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #15900: Corrected class loading problem due to annotation based exception framework.
* Mon Apr 19 2010 - thorben.betten@open-xchange.com
 - Bugfix #15804: Fixed permission update if folder holds a system permission
* Thu Apr 15 2010 - marcus.klein@open-xchange.com
 - Bugfix #15891: Do not check global address book folder permissions if user edits his own contact through user interface.
* Wed Apr 14 2010 - marcus.klein@open-xchange.com
 - Bugfix #15790: Not failing if some context has no login mappings.
* Mon Apr 12 2010 - choeger@open-xchange.com
 - Bugfix #15612: /opt/open-xchange/etc/oxfunctions.sh does not work on Ubuntu, which is using dash instead of bash
* Thu Apr 08 2010 - marcus.klein@open-xchange.com
 - Bugfix #15367: Removing MAL poll database entries when a user should be deleted.
 - Bugfix #15826: Not removing simple HTML tags from emails if their attributes are filtered.
* Wed Apr 07 2010 - karsten.will@open-xchange.com
 - Fix for unnamed Bug that occurs when crawling an empty GMX-Addressbook
 - Bugfix #15724: LoginWithHttpClientStep expanded to throw correct error when entering wrong credentials
* Tue Apr 06 2010 - marcus.klein@open-xchange.com
 - Bugfix #15656: Disabling configurable envelope-from for external mail accounts.
* Tue Apr 06 2010 - karsten.will@open-xchange.com
 - Bugfix #15794: Empty Yahoo-Addressbooks work now
 - Bugfix #15789: Removed reflection from the crawler bundle
* Mon Apr 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #15744: Generating display name for GMX contacts from given and sure name.
 - Bugfix #15764: Web crawler for web.de works now with an empty address book.
 - Bugfix #15747: Using a more genering exception message if the crawler login fails.
* Thu Apr 01 2010 - marcus.klein@open-xchange.com
 - Bugfix #15731: Proper initializing the refactored quota file storage.
* Thu Apr 01 2010 - francisco.laguna@open-xchange.com
 - Bugfix #14932: Move error messages around for translation and make them nicer.
* Thu Apr 01 2010 - karsten.will@open-xchange.com
 - Bugfix #15725: Yahoo crawler now gives readable exception if invalid credentials are entered.
* Wed Mar 31 2010 - marcus.klein@open-xchange.com
 - Bugfix #15718: Fixed wrong initialization of refactored file storage implementation.
* Wed Mar 31 2010 - thorben.betten@open-xchange.com
 - Bugfix #15711: Showing more sophisticated error message.
 - Bugfix #15730: Correctly decoding base64 encoded header.
 - Bugfix #15614: Fixed possible null dereference.
 - Bugfix #15686: No spam handler for external accounts
* Wed Mar 31 2010 - karsten.will@open-xchange.com
 - Bugfix #15726: Crawler now gives better error message if account is invalid.
* Tue Mar 30 2010 - marcus.klein@open-xchange.com
 - Bugfix #15721: Correct ordner and proper transaction handling when deleting a user that has POP3 accounts.
 - Bugfix #15700: Creating a new action for importing mails from body of request as multipart/form-data stream.
* Mon Mar 29 2010 - marcus.klein@open-xchange.com
 - Bugfix #15710: Fixed wrong call to super class.
* Thu Mar 25 2010 - karsten.will@open-xchange.com
 - Bugfix #15660: Using commons HttpClient for login to linked in to fix redirect problems with HtmlUnit.
* Thu Mar 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #15671: Probing for optional TOP and UIDL POP3 commands
* Wed Mar 24 2010 - marcus.klein@open-xchange.com
 - Bugfix #15608: Store flags as requested on action new with optional folder parameter.
* Wed Mar 24 2010 - thorben.betten@open-xchange.com
 - Bugfix #15655: Ignoring error when creating a default folder for an external account
* Wed Mar 24 2010 - tobias.prinz@open-xchange.com
 - RFC 2447: IMIP behaviour: Party crashers (people not invited, e.g. responding from another e-mail address) can now be accepted as
   participants by the organizer of the event
* Tue Mar 23 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15640: Full contact publication template now uses right keys for phone numbers
* Thu Mar 11 2010 - marcus.klein@open-xchange.com
 - Bugfix #15582: Only replacing end date of an appointment if it is a series.
 - Bugfix #15580: Correctly parsing values written as null from the client.
* Thu Mar 11 2010 - francisco.laguna@open-xchange.com
 - Bugfix #14820: Reserve filename using a mutex
* Wed Mar 10 2010 - thorben.betten@open-xchange.com
 - Bugfix #15561: Ignoring mail folders on admin login if admin has no mailbox
 - Bugfix #15538: Properly parsing an URL when surrounded by parentheses
* Wed Mar 10 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15571: Send notification mails for secondary events to all users, only external users get the IMIP attachment.
* Wed Mar 10 2010 - marcus.klein@open-xchange.com
 - Bugfix #10071: Correctly storing zero values for tasks target and actual costs and duration.
* Tue Mar 09 2010 - marcus.klein@open-xchange.com
 - Bugfix #15549: Correcting algorithm to create full month block for calendar printing.
* Mon Mar 08 2010 - thorben.betten@open-xchange.com
 - Bugfix #14098: Fixed loading root folder when updating folder cache
* Fri Mar 05 2010 - marcus.klein@open-xchange.com
 - Bugfix #15545: Adding users to listing appointments for calendar printing to get the folder identifier.
* Thu Mar 04 2010 - karsten.will@open-xchange.com
 - fixed bug 15347 (uid-column was not loaded from db to compare it to possible update)
* Thu Mar 04 2010 - choeger@open-xchange.com
 - Bugfix #15524: VoipNow administration login is readable for all linux users
   on that machine
* Wed Mar 03 2010 - thorben.betten@open-xchange.com
 - Bugfix #15539: Allowing registration of HttpServlets without a default constructor
* Wed Mar 03 2010 - tobias.prinz@open-xchange.com
 - Related to bug #15231: Outlook imports for different languages can now be easily extended by creating .properties files with mapping for
   them.
* Tue Mar 02 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15468: Stop topward iteration when hitting parentId null
* Tue Mar 02 2010 - thorben.betten@open-xchange.com
 - Bugfix #15520: Fixed color label in new folder tree
 - Bugfix #15523: Fixed changed dependency due to move of update task
 - Bugfix #14216: Changed error message
* Tue Mar 02 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15031: Resource conflict during update.
* Mon Mar 01 2010 - thorben.betten@open-xchange.com
 - Bugfix #15504: Ignoring possible FQL query result size mismatch
 - Bugfix #15408: Added support for non-ascii characters occurring in mail headers
 - Bugfix #15494: Fixed start-up order
 - Bugfix #15512: Fixed deletion of a DB folder nested below a non-DB folder
* Mon Mar 01 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15509: Null Pointer in Conversion Servlet.
* Mon Mar 01 2010 - marcus.klein@open-xchange.com
 - Bugfix #15429: Giving proper exception message if a contact in a public folder should marked private.
 - Bugfix #15501: Enabling the contact collector for all user.
* Mon Mar 01 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15500: Use a different method of accessing the currently logged in user.
* Fri Feb 26 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15472: Don't preset password fields.
* Fri Feb 26 2010 - thorben.betten@open-xchange.com
 - Bugfix #15440: Safe execution of IMAP server start-up checks for a read-only IMAP server
 - Bugfix #15492: Created a custom JCS v1.3 library
 - Bugfix #15490: Logging of unexpected socket exception
* Fri Feb 26 2010 - marcus.klein@open-xchange.com
 - Bugfix #15497: Returning connections to correct pool.
* Thu Feb 25 2010 - marcus.klein@open-xchange.com
 - Bugfix #15463: Added missing imports for javax.swing components needed for HTML parsing of emails.
 - Bugfix #15467: Patching of change exceptions uid now works if the series master has no uid set.
* Thu Feb 25 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15247: Outlook CSV field "Account" is not set as first e-mail address anymore.
* Thu Feb 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #15466: Dealing with broken Reply-To header in ENVELOPE fetch item and lowered log level
 - Bugfix #15456: Checking for proper credentials
* Wed Feb 24 2010 - thorben.betten@open-xchange.com
 - Bugfix #15448: Fixed wrong array type in complex search term
 - Bugfix #15433: Less strict parsing of multipart/* parts within JavaMail routines
* Wed Feb 24 2010 - tobias.prinz@open-xchange.com
 - Bugfix #13557: Error message when importing broken VCards gives line and column information now.
 - Bugfix #15231 (partial): Can now import most data from Dutch Outlook CSV files
* Wed Feb 24 2010 - matthias.biggeleben@open-xchange.com
 - Bugfix #15038: [L3] print week-view within firefox: 2.nd page no vertical lines.
* Tue Feb 23 2010 - thorben.betten@open-xchange.com
 - Bugfix #14593: A default folder's module must not be changed
 - Bugfix #15446: Skipping empty parameters when sanitizing Content-Type header on mail transport
* Tue Feb 23 2010 francisco.laguna@open-xchange.com
 - Bugfix #15180: Move Strings around for translation
 - Bugfix #15040: Don't skip permission checks for internal users
* Tue Feb 23 2010 - marcus.klein@open-xchange.com
 - Bugfix #15001: Removing reminder if the according appointment ends in the past.
 - Bugfix #15364: Moved messaging preferences items to messaging JSON bundle.
* Mon Feb 22 2010 - thorben.betten@open-xchange.com
 - Bugfix #15413: Checking account name prior to insertion
 - Bugfix #15313: Added properties file to define handling of confirmed-spam/confirmed-ham folders
 - Bugfix #15337: Fixed parsing of "subscribed" folder JSON field
 - Bugfix #15324: Proper display of plain-text content of a multipart/alternative mail
* Mon Feb 22 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15040: Censor user data, when global address book is deactivated
* Mon Feb 22 2010 - marcus.klein@open-xchange.com
 - Bugfix #15259: Removed free marked appointments from free/busy data. Was marked there as free but all clients seem to ignore this.
* Fri Feb 19 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15420: Confirm status for creator.
 - Bugfix #15419: Allow null value for contact collect folder.
* Fri Feb 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #15225: Optimized tables and update tasks for push malpoll component.
 - Bugfix #15317: Denying delete of user contacts even if folder permission permits that.
* Thu Feb 18 2010 - thorben.betten@open-xchange.com
 - Bugfix #15380: Using faster URL detection throughout mail module
* Wed Feb 17 2010 - francisco.laguna@open-xchange.com
 - Bugfix 14919 - Reflect automatic participant status changes in cdao for events
 - Bugfix #15083: No link to OX for external participants on confirmation change
* Wed Feb 17 2010 - thorben.betten@open-xchange.com
 - Bugfix #15410: Proper parsed message if no recipients specified
 - Bugfix #15367: Reliably deleting existing user data in MAL Poll tables
 - Bugfix #15406: Auto-detect proper POP3 storage's path
* Tue Feb 16 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15241 - VCards by Apple Addressbook with inline images are imported now
* Mon Feb 15 2010 - thorben.betten@open-xchange.com
 - Bugfix #15365: Added admin daemon to build files
 - Bugfix #15378: Auto-detection of proper content type if "name" parameter is present
* Fri Feb 12 2010 - thorben.betten@open-xchange.com
 - Bugfix #15350: Fixed retrieval of session from given cookies
 - Bugfix #14946: No null values in response to client
* Fri Feb 12 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15074: Writing full time appointments even if the master is not in the requested time frame.
* Thu Feb 11 2010 - francisco.laguna@open-xchange.com
 - Bugfix #15257: Don't get servlet writer after having selected an output stream.
 - Bugfix #15228: Gracefully handle missing infostore permissions for publications and templating
 - Bugfix #14597: Maintain a full set of properties for each property file
* Thu Feb 11 2010 - marcus.klein@open-xchange.com
 - Bugfix #15361: Added missing return statement when the attribute has been written.
* Tue Feb 09 2010 - marcus.klein@open-xchange.com
 - Bugfix #15354: Updating user attributes inside a transaction to prevent loss of aliases.
* Wed Feb 03 2010 - marcus.klein@open-xchange.com
 - Bugfix #14623: Switched sessionId and secret so the sessionId gets used as URL parameter.
* Mon Feb 01 2010 - thorben.betten@open-xchange.com
 - Bugfix #15287: Returning 7 bit characters on QuotedInternetAddress.getAddress()
 - Bugfix #15282: Fixed NPE
* Fri Jan 29 2010 - choeger@open-xchange.com
 - Bugfix #15293: calendar-printing templates are not marked as configfiles on
   RHEL and SLES
* Wed Jan 27 2010 - thorben.betten@open-xchange.com
 - Bugfix #15258: Added day of week to appointment notification's start/end date replacement
* Wed Jan 27 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15274: Values read from OXMF are now stripped of trailing whitespaces
* Tue Jan 26 2010 - tobias.prinz@open-xchange.com
 - Bugfix #15247: Outlook CSV import now translates "E-mail Address" as EMAIL1 in English Outlook files, too
* Mon Jan 25 2010 - thorben.betten@open-xchange.com
 - Bugfix #15256: Fixed NPE in Unified Mail processing
* Thu Jan 21 2010 - martin.herfurth@open-xchange.com
 - Bugfix #14679: Changing interval of a weekly series.
* Mon Jan 18 2010 - thorben.betten@open-xchange.com
 - Bugfix #14425: Fixed KXML parser to properly check string length
* Thu Jan 14 2010 - marcus.klein@open-xchange.com
 - Bugfix #15202: Added old UpdateTask
* Wed Jan 13 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15112: Intelligent contact collector settings.
* Mon Jan 11 2010 - thorben.betten@open-xchange.com
 - Bugfix #15061: Adding a warning to response object if an account's authentication fails
* Mon Jan 11 2010 - martin.herfurth@open-xchange.com
 - Bugfix #14922: Confirm status in public folders.
* Mon Jan 11 2010 - choeger@open-xchange.com
 - Bugfix #15176: Crawler YML files can not be updated
* Wed Jan 06 2010 - martin.herfurth@open-xchange.com
 - Bugfix #15155: Changing start date of a series (server side).
* Wed Jan 06 2010 - thorben.betten@open-xchange.com
 - Bugfix #15161: Applying user time zone to DateFormat instance when filling reply/forward template
* Mon Jan 04 2010 - choeger@open-xchange.com
 - Bugfix #15163: some packages are missing the dependency on sun java 6
* Mon Jan 04 2010 - thorben.betten@open-xchange.com
 - Bugfix #15128: Display of broken multipart/alternative mail
* Mon Dec 28 2009 - martin.herfurth@open-xchange.com
 - Bugfix #15046: Participant delete for Outlook.
* Tue Dec 22 2009 - thorben.betten@open-xchange.com
 - Bugfix #15126: Caching only low-cost fields while loading headers in demand
* Mon Dec 21 2009 - marcus.klein@open-xchange.com
 - Bugfix #15102: Reduced logging of missing table inside ReplicationMonitor. Improved update tasks to use connection to the database master
   only.
* Fri Dec 18 2009 - marcus.klein@open-xchange.com
 - Bugfix #15077: Increased default max object count for some user related caches to increase performance in large contexts.
* Fri Dec 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #15108: Fixed error handling on failing move operation of IMAP folders
* Fri Dec 18 2009 - francisco.laguna@open-xchange.com
 - Bugfix #15110: Apply download constraints only to OXMF pages and not attached infostore documents.
* Thu Dec 17 2009 - thorben.betten@open-xchange.com
 - Bugfix #15089: Checking for special identifier for all-groups-and-users
 - Bugfix #15099: Fixed line folding of text mails
* Thu Dec 17 2009 - marcus.klein@open-xchange.com
 - Bugfix #15096: Added missing close of prepared statement.
* Wed Dec 16 2009 - thorben.betten@open-xchange.com
 - Bugfix #15094: Fixed AJP keep-alive mechanism to not mess up AJP communication cycle
* Tue Dec 15 2009 - thorben.betten@open-xchange.com
 - Bugfix #15075: Fixed un-quoting personal part of an email
* Mon Dec 14 2009 - marcus.klein@open-xchange.com
 - Bugfix #15070: Corrected category for concurrent modifications exceptions on contacts.
* Wed Dec 09 2009 - thorben.betten@open-xchange.com
- Bugfix #15022: Fallback on non-parseable content type header
* Fri Dec 04 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14929: Setting until.
* Thu Dec 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #15020: Fixed NPE when writing JSON user data
 - Bugfix #15005: Fixed display of public folder if it contains no subfolders
* Wed Dec 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #14993: Checking for default auxiliary on cache start-up
* Tue Dec 01 2009 - choeger@open-xchange.com
 - Bugfix #15007: dependency missing on package open-xchange-user-json
* Tue Dec 01 2009 - thorben.betten@open-xchange.com
 - Bugfix #15009: Fixed exception on image retrieval
* Mon Nov 30 2009 - thorben.betten@open-xchange.com
 - Bugfix #14937: MAL poll uses DB storage for remembered mail IDs
* Fri Nov 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #14890: Fixed display of a mail containing multiple inline text/* parts
* Fri Nov 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14624: Using a faster initialization of update task list that does not wait for a timeout of 2 seconds when creating the update
   task list.
* Wed Nov 25 2009 - thorben.betten@open-xchange.com
 - Bugfix #14741: Removed usage of javax.swing.text.BadLocationException
 - Bugfix #14938: Fixed display of mails with no text body from cached JSON representation
 - Bugfix #14948: Building a self-describing URL for image look-up if no longer in image cache
 - Bugfix #14946: Safety check if folder fullname is null in delivered request body
* Wed Nov 25 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14984: SQL-Syntax error with special folder rights fixed.
* Tue Nov 24 2009 - thorben.betten@open-xchange.com
 - Bugfix #14947: Checking for i18n strings when looking for a duplicate folder
* Mon Nov 23 2009 - thorben.betten@open-xchange.com
 - Bugfix #14944: Fixed NPE in mail prefetcher
 - Bugfix #14949: Checking changed permissions for system folders
* Fri Nov 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #14940: Fixed NPE when unregistering from unknown servlet path
* Wed Nov 18 2009 - marcus.klein@open-xchange.com
 - Bugfix #14889: Using the same connection for deleting reminder when a user is deleted.
 - Bugfix #14834: Not trying to initialize the nested cause of a ServletException because this gives a IllegalStateException.
   Logging nested cause instead additionally.
* Wed Nov 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #14891: Using proper login to check IMAP authentication
 - Bugfix #14336: Ignoring invalid FETCH response line
* Thu Nov 12 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14530: More resillience towards disappearing subscription sources and publication targets.
 - Bugfix #14402: Consider two companies different.
 - Bugfix #14027: Generate absoulte URLs for publications.
* Thu Nov 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #14851: Fixed composing image URL
* Thu Nov 12 2009 - marcus.klein@open-xchange.com
 - Bugfix #14722: Added command so print view directly opens print dialogue.
* Wed Nov 11 2009 - marcus.klein@open-xchange.com
 - Bugfix #14723: Always putting a complete table into the print view.
* Tue Nov 10 2009 - marcus.klein@open-xchange.com
 - Bugfix #14810: Correctly setting displayStart and displayEnd if range is greater than a day.
* Fri Nov 06 2009 - thorben.betten@open-xchange.com
 - Bugfix #14706: More robust browser detection from client's "user-agent" header
 - Bugfix #14664: Added JSON writer for field 103 "number_of_links" when writing a contact
* Thu Nov 05 2009 - thorben.betten@open-xchange.com
 - Bugfix #14772: Improved SQL query performance
 - Bugfix #14781: Less strict parsing of UUEncoded mails
 - Bugfix #14698: Crawling more information from facebook.com
* Wed Nov 04 2009 - thorben.betten@open-xchange.com
 - Bugfix #14532: Checking Content-Type and Content-Length header of provided OXMF URL
 - Partial bugfix #14698: Fixed NPE on missing title
* Tue Nov 03 2009 - marcus.klein@open-xchange.com
 - Bugfix #14505: Correctly handling null values when comparing InfoStore objects.
* Mon Nov 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #14677: Fixed performance issue when searching for a user's shared folders
* Mon Nov 02 2009 - marcus.klein@open-xchange.com
 - Bugfix #14701: Wrote JDBC4 wrapper for java.sql.Connection to be compatible with Java6.
* Sat Oct 31 2009 - thorben.betten@open-xchange.com
 - Bugfix #14742: Fixed setting path info in servlet's request
* Fri Oct 30 2009 - thorben.betten@open-xchange.com
 - Bugfix #14741: Parsing RTF parts as "application/rtf" attachments
* Thu Oct 29 2009 - thorben.betten@open-xchange.com
 - Bugfix #14495: Added update task to remove duplicate contact collector folders
* Thu Oct 29 2009 - marcus.klein@open-xchange.com
 - Bugfix 14724: Fixed null value returned when requesting Outlook updater the first time.
* Wed Oct 28 2009 - thorben.betten@open-xchange.com
 - Bugfix #14727: Fixed NPE on missing FETCH item
 - Partial bugfix #14495: Using a writable connection when checking for contact collector folder
* Tue Oct 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14719: Adding commons.codec.digest export to common bundle for outlook updater bundle.
 - Bugfix #14392: Changed a label.
 - Bugfix #14507: Marked subscription form labels as texts to translate.
* Tue Oct 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #14716: More robust detection if a message contains file attachments
 - Bugfix #14691: Fixed start-letter-search in collected contacts folder
 - Bugfix #14681: No read permission check in image framework's delete listener
* Mon Oct 26 2009 - thorben.betten@open-xchange.com
 - Bugfix #14694: Using a write-connection to check for an already existing mail account to avoid mast-slave-latency problems
* Wed Oct 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #14688: Dropping cached account's default folders if updated
* Mon Oct 19 2009 - marcus.klein@open-xchange.com
 - Bugfix #14668: Reading values correctly from user_attribute table.
 - Bugfix #14672: Not removing aliases from the attributes set of a user.
* Mon Oct 19 2009 - thorben.betten@open-xchange.com
 - Bugfix #14676: Ignoring unknown entity when parsing ACLs
 - Bugfix #14671: Proper check if Unified INBOX is enabled
* Sat Oct 17 2009 - thorben.betten@open-xchange.com
 - Bugfix #14336: Enhanced logging if a fetch item is missing in IMAP server's FETCH response
* Fri Oct 16 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14653: No notification mails for pseudo changes.
* Wed Oct 14 2009 - marcus.klein@open-xchange.com
 - Added information about user module access permission to detail method of reporting JMX interface.
* Tue Oct 13 2009 - marcus.klein@open-xchange.com
 - Bugfix #14655: Conditionally sending CAPABILITY after STARTTLS only for Cyrus that is not sieve draft conform. dovecot is conform.
* Mon Oct 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #14638: Fixed download of a mail as eml file with IE
* Sun Oct 11 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14625: User is no longer forced to be participant in public folders.
* Wed Oct 07 2009 - stefan.preuss@open-xchange.com
 - Added additional search fields 'department, street_business and city_business' in module contacts (US3195)
* Tue Oct 06 2009 - marcus.klein@open-xchange.com
 - Bugfix #14635: Using normal version string for WebDAV/XML interface.
* Wed Sep 30 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13226: Change user will not be added to participant list, if he is already member of a participating group.
* Mon Sep 28 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14357: Fixed yearly pattern with workdays.
* Sun Sep 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #14571: Fixed setting "In-Reply-To" and "References" headers on reply to a message of an external mail account
* Fri Sep 25 2009 - thorben.betten@open-xchange.com
 - Bugfix #14570: Enhanced error message by necessary information to recognize affected user and account
 - Bugfix #14572: More tolerant parsing of GETSCRIPT response
* Wed Sep 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #14558: Implemented setting the confirm message for tasks through WebDAV/XML interface.
* Tue Sep 22 2009 - thorben.betten@open-xchange.com
 - Bugfix #14525: Added configuration option to define SIEVE auth encoding
 - Bugfix #14533: Fixed plain-text reply version
 - Bugfix #14544: Setting right array size when resolving UIDs to sequence numbers
* Tue Sep 22 2009 - marcus.klein@open-xchange.com
 - Bugfix #14453: Ignoring not loadable user contact in ImageRegistryDeleteListener and continue deleting that user.
 - Bugfix #14561: Not converting login information to lower case in WebDAV interface login method.
* Mon Sep 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #14539: Fixed parsing of content type header
 - Bugfix #14494: Don't use CopyOnWriteArrayList in Collections.sort() routine
* Thu Sep 17 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14504: Show only public contacts in publications
 - Bugfix #14506: Use transfer encoding "chunked" for infostore subscriptions
* Tue Sep 15 2009 - martin.herfurth@open-xchange.com
 - Bugfix #12050: Series conflicts with own exceptions.
* Fri Sep 11 2009 - thorben.betten@open-xchange.com
 - Bugfix 14489: Auto-Detecting charset of a RFC 2047 "encoded-word" if unknown
* Thu Sep 10 2009 - thorben.betten@open-xchange.com
 - Bugfix #14467: Fixed reply of multipart/related message without HTML content
 - Bugfix #14466: Fixed HttpSessionWrapper.invalidate() method
* Wed Sep 09 2009 - marcus.klein@open-xchange.com
 - Bugfix #14454: Escaped dash character in email regex for crawling contact from Google.
* Fri Sep 04 2009 - marcus.klein@open-xchange.com
 - Bugfix #14450: Storing sent null value for task attributes target duration, actual duration, target costs and actual costs as null and
   not as zero.
* Thu Sep 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #14445: Fixed incorrect admin permission on top level infostore folder
* Thu Sep 03 2009 - marcus.klein@open-xchange.com
 - Bugfix #14389: Administration daemon is now more resistant on context deletion if the context information is not complete.
* Wed Sep 02 2009 - tobias.prinz@open-xchange.com
 - Bugfix #14350: Removed special handling of empty cells in parser, these are now treated as null. That made it possible to remove
   workaround for bug 7248, too.
 - Bugfix #14349: VCard 3.0 allows for a list of nicknames. That led to the brackets around the nickname.
* Tue Sep 01 2009 - francisco.laguna@open-xchange.com
 - Bugfix 14428: Don't overwrite existing secrets.
* Tue Sep 01 2009 - choeger@open-xchange.com
 - Bugfix #14403: log4j does not work on SLES11, file permission problem
 - Bugfix #14395: Unable to install lang and lang-community packages at the same time
* Tue Sep 01 2009 - marcus.klein@open-xchange.com
 - Bugfix #14178: Additionally to check on the database a String.equals() check is added. The collation is changed to utf_8_bin on column
   uid of table login2user.
* Mon Aug 31 2009 - thorben.betten@open-xchange.com
 - Bugfix #14396: Properly writing mail account's properties when writing JSON data
 - Bugfix #14421: Added new virtual folder tables to SQL initialization scripts
 - Bugfix #14399: Fixed NPE on missing inline image file name
 - Bugfix #14425: Fixed safety routine to correct invalid mail-safe encoded header values
* Thu Aug 27 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14337: Interpret CLASS:CONFIDENTIAL as private.
 - Bugfix #14354: Setting the attachment upload limit to 0 in attachment.properties should be interpreted as "unlimited"
* Thu Aug 27 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14390: Wrong folder id in team-view.
* Tue Aug 25 2009 - tobias.prinz@open-xchange.com
 - Bugfix #14331: Translation of privacy disclaimer for published contacts and infostore based on user locale
* Tue Aug 25 2009 - tobias.prinz@open-xchange.com
 - Bugfix #14330: Using new OXMF format in default contact publication template
* Mon Aug 24 2009 - thorben.betten@open-xchange.com
 - Bugfix #14365: New CLI tools no more trigger update process
 - Bugfix #14362: Improved error handling in new CLI tools
 - Bugfix #14363: Improved error handling in new CLI tools
 - Bugfix #14364: Improved error handling in new CLI tools
 - Bugfix #14361: Changed argument identifiers to not collide
* Mon Aug 24 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14343: Format dates in CSV export so the import understands them.
 - Bugfix #14343: Don't set caching headers in file downloads for IE. Set content-disposition header for nice file names.
* Mon Aug 24 2009 - tobias.prinz@open-xchange.com
 - Bugfix #14229: Handling SIEVE scripts with empty addresses
* Fri Aug 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #14346: Considering all available inline text parts when composing reply version of a mail
 - Bugfix #13322: Fixed NPE in CalendarCollection
* Thu Aug 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #14345: Proper escaping/un-escaping '"' and '\' characters in filenames
* Wed Aug 19 2009 - martin.herfurth@open-xchange.com
 - HTTP-API: Additional return parameter for free/busy:
             folder_id, if the appointment is visible for the user.
* Tue Aug 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #14333: Fixed parsing of duplicate parameters in parameterized list
 - Bugfix #13631: Supporting SIEVE STARTTLS
* Mon Aug 17 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13782: Added marker attribute for internal email addresses
* Mon Aug 17 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14250: Fake delete for outlook.
* Fri Aug 14 2009 - thorben.betten@open-xchange.com
 - Bugfix #14038: Fixed permission check to ignore infostore folders when considering limited public folder access
* Fri Aug 14 2009 - choeger@open-xchange.com
 - Bugfix #14319 -  Publication Templates must be marked as configuration files
* Fri Aug 14 2009 - martin.herfurth@open-xchange.com
 - Bugfix #14309: Link in notification mails for internal participants.
* Thu Aug 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #14264: Improved auto-complete search in contact module
* Wed Aug 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #14292: Support of AJPv13 syntax for attribute "req_attribute"
* Tue Aug 11 2009 - thorben.betten@open-xchange.com
 - Bugfix #14298: Update task for mail account migration runs per context while logging experienced errors.
   Thus admin is able to see which context's users weren't migrated properly.
* Tue Aug 04 2009 - thorben.betten@open-xchange.com
 - Bugfix #14272: Discarding non-existing user/group permission previously detected as being corrupt
* Mon Aug 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #14246: Allowing any letter character in an URL
 - Bugfix #14271: Fixed StringIndexOutOfBoundsException when un-quoting an email address
 - Bugfix #14269: Setting proper "Date" header in user's time zone when sending an email
* Tue Jul 28 2009 - thorben.betten@open-xchange.com
 - Bugfix #14225: Allowing static setup of update tasks
 - Bugfix #14232: Checking for Drafts folder prior to deleting old draft version
* Mon Jul 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14213: Setting configuration file permissions to reduce readability to OX processes.
* Fri Jul 24 2009 - thorben.betten@open-xchange.com
 - Bugfix #14050: Maintaining quoted personal name of an email address when
   generating address' mail-safe version to be compatible with other mail client
 - Bugfix #14217: Maintaining quoted personal name of an email address when
   generating address' mail-safe version to properly quote umlauts
* Thu Jul 23 2009 - thorben.betten@open-xchange.com
 - Bugfix #14211: Using static delimiter character '/' to separate mail account part form fullname part within a mail folder identifier
* Wed Jul 22 2009 - martin.herfurth@open-xchange.com
 - New User Configuration: defaultStatusPrivate/defaultStatusPublic for setting a default
                           confirmation status for participants in private/public folders.
* Tue Jul 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #14196: Fixed error message arguments
* Mon Jul 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #14181: Improved regex to detect MS conditional comments
* Mon Jul 20 2009 - martin.herfurth@open-xchange.com
 - New Parameter: com.openexchange.calendar.undefinedstatusconflict, conflict behaviour for
                  appointments with status: waiting/none
* Sun Jul 19 2009 - choeger@open-xchange.com
 - Bugfix #14193: Update from SP5 to 6.10 RC5 fails with error on Debian
* Fri Jul 17 2009 - marcus.klein@open-xchange.com
 - Bugfix #14115: Added renaming of oxreport.in to oxreport and setting its executable permission. Added missing library to classpath.
* Thu Jul 16 2009 - marcus.klein@open-xchange.com
 - Bugfix #14154: Corrected replacements in PO files for forwarded mails.
* Tue Jul 14 2009 - martin.herfurth@open-xchange.com
 - Update Task #58: Repair bad null value in in recurrence pattern.
* Mon Jul 13 2009 - martin.herfurth@open-xchange.com
 - Bugfix #12509: Participant creates change exception of an appointment
                  which is not located in the creators default folder.
* Mon Jul 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #14152: Added support for Dovecot ACL identifiers
* Fri Jul 10 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14143: Force connection close after a subscription has been loaded.
 - Bugfix #14075: Don't recreate the collected addresses folder if the feature has been disabled explicitely.
 - Bugfix #14135: Don't share string builder instance among threads.
* Thu Jul 09 2009 - martin.herfurth@open-xchange.com
 - Bugfix #11210: Conflicts for appointment series.
 - New Config parameter for limiting the search range for series conflicts.
* Thu Jul 09 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14134: WebDAV Infostore disregards uploadfilesizelimitperfile.
 - Bugfix #14107: Clients may specify the ID of the task to confirm in either the request body or the parameters. If both are sent, body
   wins.
 - Bugfix #14134: Infostore and Object Attachments disregard user specific file upload quotas.
* Tue Jul 07 2009 - francisco.laguna@open-xchange.com
 - Bugfix #14082: Make webdav.version conform to new version numbering scheme.
* Mon Jul 06 2009 - marcus.klein@open-xchange.com
 - Bugfix #14077: Tasks in public folders now get context administrator as creating or changing user if original user is removed.
 - Bugfix #14074: Moving series is now respecting different time zone offsets due to daylight saving times.
* Fri Jul 03 2009 - marcus.klein@open-xchange.com
 - Bugfix #14072: Corrected version number in server start log entries.
* Fri Jul 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #12623: action=updates requests return changes (new, modified, and deleted) greater than passed last-modified time stamp
* Thu Jul 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #14061: Properly initialization of AbstractMailAccount if mail properties have not been initialized, yet
* Wed Jul 01 2009 - thorben.betten@open-xchange.com
 - Bugfix #14028: Using Java's concurrent read-write lock to control access to HTTP servlet manager
 - Bugfix #13736: Fallback to ContextStorage implementation if ContextService is missing
 - Bugfix #14047: Checking for virtual folder IDs on action=get to folder servlet
* Wed Jul 01 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13342: Last modified for delete exceptions.
 - Bugfix #13446: Changed field size for change exceptions to accept lots of exceptions.
 - Bugfix #13447: Change sequence to single appointment and vice versa.
 - Bugfix #13505: Change weekly sequence to monthly.
* Wed Jul 01 2009 - marcus.klein@open-xchange.com
 - Bugfix #12215: Logging was changed to not log invalid email addresses.
 - Bugfix #14048: Added missing import to xerces bundle.
* Tue Jun 30 2009 - marcus.klein@open-xchange.com
 - Bugfix #13487: Improved indexes on InfoStore tables to improve performance.
 - Bugfix #12251: Removed stack trace on warning message about not parseable Priority header in an email.
* Tue Jun 30 2009 - thorben.betten@open-xchange.com
 - Bugfix #14035: Fixed IMAP folder deletion
 - Bugfix #14033: Added logging if a cached session is found
 - Bugfix #14034: Throwing quota-exceeded exception if necessary bundle to perform "publish, don't attach" feature is missing
 - Bugfix #13832: Sending proper error to front-end
 - Bugfix #14032: Checking until date for recurring events to decide whether to drop notifications
 - Bugfix #13573: Fixed read acknowledgment for external mail accounts
* Tue Jun 30 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13914: Multiplex search for multiple contact interface implementations and merge their results.
* Tue Jun 30 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13995: Creator of appointment in public folder accepts.
* Mon Jun 29 2009 - thorben.betten@open-xchange.com
 - Bugfix #14008: Fixed notification messages for resource participants
 - Bugfix #14024: Removed byte formatting in error message
 - Bugfix #14023: Fixed static insertion of primary account's default folder on action=path to folder servlet
* Mon Jun 29 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13811: Throw an exception if a preferences item path is claimed more than once.
* Sat Jun 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #14010: Added timeout to IMAP default folder check routine
* Fri Jun 26 2009 - thorben.betten@open-xchange.com
 - Bugfix #13975: Replaced folder name with folder ID in error message FLD-0003
 - Bugfix #14001: Proper multipart/* parsing if message contains empty text content
 - Bugfix #14000: Maintaining file extension when composing new file name on duplicate infostore file
 - Bugfix #14005: Dealing with corrupt image path on reply/forward
 - Bugfix #13971: Suppressing error logs on disabled mail account for admin
* Fri Jun 26 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13931: Make special sorting case insensitive.
* Fri Jun 26 2009 - choeger@open-xchange.com
 - Bugfix #13997: IBM and SUN xerces packages contain package description for SUN Java
* Fri Jun 26 2009 - marcus.klein@open-xchange.com
 - Bugfix #13380: Changed the default scaling size for contact images to 90x90 pixels.
 - Bugfix #13951: Writing understandable exception to RMI client if database is updated.
* Fri Jun 26 2009 - marcus.klein@open-xchange.com
 - Bugfix #14022: Adding additional unique indexes on prg_dates_members table to improve performance.
* Thu Jun 25 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13625: Search for Tags in appointments.
* Thu Jun 25 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12380: When during iCal import a series master is found to be outside the recurrence, create an additional appointment at the
   masters date.
 - Bugfix #13963: Default to editors language in notification mails for resource admins.
* Thu Jun 25 2009 - marcus.klein@open-xchange.com
 - Bugfix #13360: Display, first and sure name are mandatory attributes for editing user contacts.
* Wed Jun 24 2009 - thorben.betten@open-xchange.com
 - Bugfix #13968: Fixed IE < 8 vulnerability for HTML content nested inside corrupt image files
* Wed Jun 24 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13826: No move between folders for recurring appointments.
* Tue Jun 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #13852: Adding OSGi services for creating and removing genconf, publish and subscribe tables to admin.
* Tue Jun 23 2009 - thorben.betten@open-xchange.com
 - Bugfix #13949: Dealing with missing From header on action=new in mail servlet
 - Bugfix #13853: Displaying broken header as it is
 - Bugfix #13952: Fixed missing argument in login exception
* Tue Jun 23 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13184: Don't send notifications with link to appointment/task which the can not see.
 - Bugfix #13942: Changing reminder does not affect confirmation status.
* Mon Jun 22 2009 - marcus.klein@open-xchange.com
 - Bugfix #12352: Changed type for target_id in reminder SQL statements to string. Then existing indices are used on reminder table.
* Mon Jun 22 2009 - thorben.betten@open-xchange.com
 - Bugfix #13943: Fixed NPE if requested message does not exist
 - Bugfix #13932: Fixed NPE on message forward with empty subject and endless loop in front-end if message contains empty subject
* Mon Jun 22 2009 - choeger@open-xchange.com
 - Bugfix #13928: Update SP5 -> 6.10 does not work
* Sun Jun 21 2009 - marcus.klein@open-xchange.com
 - Bugfix #13749: Checking if user really lost its calendar access permission before deleting his invisible data.
* Fri Jun 19 2009 - martin.herfurth@open-xchange.com
 - API Change: Added field "id" (user id) to ajax confirm request to change confirm status of other users.
 - API Change: Changed ajax confirm request to fulfil api documentation.
 - Bugfix #13828: Change confirm status of other users (Server side).
* Fri Jun 19 2009 - thorben.betten@open-xchange.com
 - Bugfix #13794: Performing an ignore-case look-up of default folders by name
* Thu Jun 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #13891: Fixed NPE on control bundle stop
 - Bugfix #13791: Trimming mail account properties which must not contain leading/trailing whitespaces
 - Bugfix #13450: Using proper ContactInterface on contact search
 - Bugfix #13449: Fixed requests to attachments of a contact provided through ContactInterface
* Wed Jun 17 2009 - thorben.betten@open-xchange.com
 - Bugfix #13903: Delivering empty content on "No content" error while showing message source
* Tue Jun 16 2009 - thorben.betten@open-xchange.com
 - Bugfix #12821: Inline images made visible when composing reply/forward mail
* Tue Jun 16 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13869: Don't overwrite contact attributes with empty strings in XING subscriptions
 - Bugfix #13866: Use streams instead of Strings when handling websites.
* Tue Jun 16 2009 - choeger@open-xchange.com
 - Bugfix #12859: [L3] Changing umask for Infostore documents
 - Bugfix #13477: [L3] "Error: Invalid email address" when clearing email2 or email3 with ""
* Tue Jun 16 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13501: Change a daily sequence into a weekly sequence.
* Mon Jun 15 2009 - marcus.klein@open-xchange.com
 - Bugfix #13845: Adding information to iCal files to enable invitations for Notes. Adding iCal version information, too.
* Mon Jun 15 2009 - thorben.betten@open-xchange.com
 - Bugfix #13535: Added "Cc" to generated text on inline forward
 - Bugfix #13899: Fixed possible stack overflow on access to non-existing folder
 - Bugfix #13900: Checking mail(s) existence prior to creating reply/forward version
 - Bugfix #13897: Proper look-up of registered ContactInterfaceProvider services
* Mon Jun 15 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13865: React to intricacies of service dependencies correctly in templating bundle
 - Bugfix #13864: Close your statements!
* Sat Jun 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #13830: Fixed occurring NPEs on framework closure
* Fri Jun 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #13480: Enabling TTL for successful look-ups from the name service.
 - Bugfix #13833: Fixed mail path parsing if path contains umlauts
 - Bugfix #13843: Checking for "[LOGIN-DELAY]" response code on authentication error when validating POP3 mail account
 - Bugfix #13552: Replaced folder ID with folder name in error message
* Fri Jun 12 2009 - marcus.klein@open-xchange.com
 - Bugfix #13879: Added shift-jis as alias charset of Shift_JIS.
 - Bugfix #6692: Renamed group 0 to "All users" and group 1 to "Standard group". An update task fixes values in the database.
* Wed Jun 10 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13260: Delete occurrences of fulltime appointment sequences.
 - Bugfix #12280: Length of fulltime appointment sequences fixed.
* Wed Jun 10 2009 - marcus.klein@open-xchange.com
 - Bugfix #13873: Catching RuntimeExceptions in database update tasks. Fixed causes of RuntimeExceptions in database update tasks.
* Tue Jun 09 2009 - thorben.betten@open-xchange.com
 - Bugfix #13807: Supporting TLS for SMTP and IMAP provider
 - Bugfix #13431: Proper error message on missing read permission to contact folder
* Mon Jun 08 2009 - thorben.betten@open-xchange.com
 - Bugfix #13777: Creating INBOX folder if absent
* Mon Jun 08 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13800: Don't inherit permissions from system folders when a folder is created via infostore webdav interface.
 - Config Change: Map /publications namespace to ajp server in apache configuration
* Fri Jun 05 2009 - thorben.betten@open-xchange.com
 - Bugfix #13716: Setting proper context ID when creating an EmailableParticipant instance for an external user
 - Bugfix #13746: Programmatically setting JTidy "clean" configuration option to false
   and removed this option from TidyConfiguration.properties file.
 - Bugfix #13776: Providing more account/user information if checking account's default folders fails
 - Bugfix #13804: Fixed NPE on autosave draft operation
* Tue Jun 02 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13681: Change last editor on webdav lock
* Fri May 29 2009 - thorben.betten@open-xchange.com
 - Bugfix #13771: Fixed validation of entered mail/transport configuration
 - Bugfix #13771: Fixed validation of entered mail/transport configuration
 - Bugfix #13768: Fixed update of a POP3 account
 - Bugfix #13767: Only authentication is performed when checking POP3 account
* Wed May 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #13742: Dealing with sun.io.MalformedInputException on IBM Java when reading mail content
* Tue May 26 2009 - thorben.betten@open-xchange.com
 - Bugfix #13734: Proper dealing with multiple resolved user IDs during ACL mapping
* Tue May 26 2009 - choeger@open-xchange.com
 - Bugfix #12859: [L3] Changing umask for Infostore documents
* Mon May 25 2009 - thorben.betten@open-xchange.com
 - Bugfix #13712: Deleting account properties prior to deleting account data
 - Bugfix #13705: Checking needed fields on insert action
 - Bugfix #13718 and #13721: No unnecessary content loading when generating editable reply/forward message
 - Bugfix #13710: Fixed NPE on POP3 access
* Fri May 22 2009 - marcus.klein@open-xchange.com
 - Bugfix #13089: Fixed null pointer access when checking for permission of reading a task through a link.
* Tue May 19 2009 - thorben.betten@open-xchange.com
 - Bugfix #13685: More robust parsing of mail account JSON data
* Mon May 18 2009 - marcus.klein@open-xchange.com
 - Bugfix #6277: Sending group 0 through WebDAV interface.
* Thu May 14 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13482: Relock without body needs to update referenced lock from ifheader
* Wed May 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #13604: SWitching to strict detection whether a mail part's disposition is "INLINE" or not when generating forward mail
* Mon May 11 2009 - francisco.laguna@open-xchange.com
 - Bugfix 13482: Content-Length of 0 means there is no body. Don't use the XML parser on that.
* Fri May 08 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13627: Fixed sorting in action=versions of infostore.
* Tue May 05 2009 - thorben.betten@open-xchange.com
 - Bugfix #13576: Improved alternative UID look-up if UIDPLUS capability is missing on IMAP server
* Sat May 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #13579: Using more sophisticated (cache) key for MailAccount instances to avoid collisions
* Wed Apr 29 2009 - thorben.betten@open-xchange.com
 - Bugfix #13473: Fixed cookie parsing when an ending ";" is present
* Tue Apr 28 2009 - thorben.betten@open-xchange.com
 - Bugfix #13553: Using a specified charset name when encrypting/decrypting passwords
 - Bugfix #13549: Detecting proper account when replying/forwarding an email to load possibly referenced parts from right account
* Thu Apr 16 2009 - marcus.klein@open-xchange.com
 - Bugfix #13437: Implemented free busy for resources.
* Mon Apr 13 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13482: Survive empty LOCK request bodies in webdav infostore.
 - Bugfix #13477: Move from batching infostore deletes on user delete to deleting individual documents.
* Mon Apr 06 2009 - thorben.betten@open-xchange.com
 - Bugfix #13473: Fixed parsing cookies which contain an ending semicolon
* Mon Apr 06 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13465: Set timezone when writing creation date and last modified in webdav to UTC, as promised by the pattern.
* Mon Mar 30 2009 - thorben.betten@open-xchange.com
 - Bugfix #13451: Retry parsing IMAP server's STATUS response in a more tolerant way if a parsing error occurs
* Fri Mar 27 2009 - dennis.sieben@open-xchange.com
 - Bugfix #13442: Correctly parsing multi line sieve rules.
* Wed Mar 25 2009 - thorben.betten@open-xchange.com
 - Bugfix #13343: Proper last-accessed check when deciding whether to send an AJP KEEP-ALIVE or not
* Mon Mar 23 2009 - thorben.betten@open-xchange.com
  - Bugfix #12220: Enhanced forward/reply calls by an optional "view" parameter to define the desired format.
* Wed Mar 18 2009 - thorben.betten@open-xchange.com
 - Bugfix #13406: Ensured applying receiver's locale to action replacement for proper translation
* Tue Mar 17 2009 - thorben.betten@open-xchange.com
 - Bugfix #13362: Checking requested columns for last-modified field prior to adding it
 - Bugfix #13048: Proper export/import of VCards with empty "URL"/"ORG" property. This change fixes handling of "PHOTO" property, too.
 - Postprocessings for bugfix #13154
* Mon Mar 16 2009 - marcus.klein@open-xchange.com
 - Bugfix #13394: Adding delete listener to remove server settings for a user.
 - Bugfix #13396: Using group storage with group 0 to resolve participants for notification.
* Mon Mar 16 2009 - thorben.betten@open-xchange.com
 - Bugfix #13372: Extending auto complete search to support distribution lists.
* Fri Mar 13 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13358: Deleted groups are resolved and members are added to the appointments.
 - Bugfix #13377: Remove Participant after changing an appointments timeframe.
* Fri Mar 13 2009 - thorben.betten@open-xchange.com
 - Bugfix #13382: Setting JTidy's line-wrap argument to zero (max. line length)
* Wed Mar 11 2009 - thorben.betten@open-xchange.com
 - Bugfix #13157: Fixed tons of logging through handling broken socket connection in servlet output stream
 - Bugfix #13366: Fixed reading mail folder information (total, unread, etc.) on dovecot IMAP server
* Wed Mar 11 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13333: Added null check.
* Tue Mar 10 2009 - thorben.betten@open-xchange.com
 - Bugfix #13364: Setting proper Content-Disposition header when parsing TNEF attachments
* Sun Mar 08 2009 - thorben.betten@open-xchange.com
 - Bugfix #13340: Accepting ID of a virtual folder on action=get
* Fri Mar 06 2009 - thorben.betten@open-xchange.com
 - Bugfix #13329: Notification mails are pooled per changing user
 - Bugfix #13324: Removed customer names from property file
 - Bugfix #13334: Ensured enough capacity in backing array
* Thu Mar 05 2009 - marcus.klein@open-xchange.com
 - Bugfix #12241: Removed context load waiting if context is not found. Added check if filestore still exists.
* Thu Mar 05 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13158: Internal calculation fix.
* Wed Mar 04 2009 - thorben.betten@open-xchange.com
 - Bugfix #13313: Allowing "base" tag in whitelist.properties to support relative image paths
* Wed Mar 04 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13238: Expired locks have infoitems show up in updates response.
* Wed Mar 04 2009 - choeger@open-xchange.com
 - Bugfix #13316: postinstall script breaks update under certain circumstances
* Tue Mar 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #13295: Using unicode charset on message reply to an ASCII text message
 - Bugfix #13277: Enhanced SpamHandler interface to indicate whether confirmed-spam/confirmed-ham folders shall be created on default
   folder check and if their subscription shall be disabled or not.
* Tue Mar 03 2009 - martin.herfurth@open-xchange.com
 - Bugfix #12923: Move of an appointment into a shared folder.
* Mon Mar 02 2009 - francicso.laguna@open-xchange.com
 - Bugfix #13126: Added Validation to check for filenames containing slashes
 - Bugfix #13227: Allow clients to specify the contact search to use the OR habit.
* Mon Mar 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #11629: Added new update task to extends size of VARCHAR column 'dn' in working/backup calendar rights table
* Fri Feb 27 2009 - thorben.betten@open-xchange.com
 - Bugfix #13284: Resolved storage inconsistency on message append
 - Bugfix #12954: Removing unnecessary CDATA tags from style elements produced by JTidy
 - Bugfix #13283: Applying proper subscription status "true" if ignore-subscription is enabled
* Thu Feb 26 2009 - thorben.betten@open-xchange.com
 - Partial bugfix #13284: Checking possible null reference when retrieving formerly auto-saved draft message to throw a meaningful error
* Thu Feb 26 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13260: Deletion of an occurrence in an endless series via outlook.
* Tue Feb 24 2009 - marcus.klein@open-xchange.com
 - Bugfix #12175: Directly getting results from recurring calculation if calculation is done with dedicated time frame.
* Tue Feb 24 2009 - thorben.betten@open-xchange.com
 - Bugfix #13255: Fixed possible NPE on broken pipe on socket connection
 - Bugfix #13259: Fixed close() in AJP's servlet output stream implementation
 - Bugfix #11211: Fixed order of passed string replacements to String.format()
* Mon Feb 23 2009 - thorben.betten@open-xchange.com
 - Bugfix #9872: Added "MACINTOSH" as an alias charset for "MacRoman"
 - Bugfix #13249: Fixed illegal charset name exception
 - Bugfix #5840: Immediate closing of obtained SearchIterator instance
* Mon Feb 23 2009 - choeger@open-xchange.com
 - Bugfix #12517: [L3] Foldercache does not synchronize properly
    set jcs.region.OXFolderCache.elementattributes.IsLateral=false on update
* Fri Feb 20 2009 - thorben.betten@open-xchange.com
 - Bugfix #13236: Proper MIME decoding of header "Organization"
* Fri Feb 20 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13180: Ignore locks in If Header from Microsoft Data Access Internet Publishing Provider DAV
* Thu Feb 19 2009 - thorben.betten@open-xchange.com
 - Bugfix #12949: Avoiding long running recurring calculations
 - Bugfix #10755: Allowing empty title on appointment insert
* Thu Feb 19 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13027: Fix calculation of mini calendar appointment occurrences.
* Wed Feb 18 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13214: Check for start and end date on updates.
* Tue Feb 17 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13131: Added transaction handling for last-modified during one multistatus.
* Tue Feb 17 2009 - marcus.klein@open-xchange.com
 - Bugfix #13221: Checking if start date is before end date on task update.
* Mon Feb 16 2009 - marcus.klein@open-xchange.com
 - Implemented more user friendly sorting of contacts.
* Thu Feb 12 2009 - thorben.betten@open-xchange.com
 - Bugfix #13164: Showing inline ICal as attachments rather than mail text
 - Bugfix #13120: Added string constants for ordinals
* Tue Feb 10 2009 - marcus.klein@open-xchange.com
 - Bugfix #11524: Using more efficient poll for the udp push queue.
* Tue Feb 10 2009 - thorben.betten@open-xchange.com
 - Bugfix #13154: Moved quota-check to message parser to ensure all attachments are checked prior to sending a mail
* Tue Feb 10 2009 - francisco.laguna@open-xchange.com
 - Node #3267: Omit version attribute in iCal file so that outlook 2003 likes to import them.
   See: http://calendarswamp.blogspot.com/2005/08/outlook-2003-for-ical-import-use.html
* Mon Feb 09 2009 - francisco.laguna@open-xchange.com
 - Bugfix #9771: Write cell phone into cell column even if it's marked as a "home" phone
* Mon Feb 09 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13121: Deleting Appointment as a Participant through a shared folder.
* Sun Feb 08 2009 - thorben.betten@open-xchange.com
 - Bugfix #13138: Using a charset which supports non-ascii characters when generating a forward message
* Sat Feb 07 2009 - thorben.betten@open-xchange.com
 - Bugfix #13147: Lowered max. IMAP command length to RFC 2683 recommendation
   of 8000 octets.
* Fri Feb 06 2009 - marcus.klein@open-xchange.com
 - Bugfix #13145: Header in POParser must only be remembered for a single file. Using new POParser for every new file.
 - Bugfix #13091: Corrected handling of starting letter search.
* Thu Feb 05 2009 - marcus.klein@open-xchange.com
 - Added config switch if the users are allowed to search for contacts across all contact folders.
 - Bugfix #13134: Adding E-Mail address into display name in contact collector if display name is missing in email address.
* Wed Feb 04 2009 - thorben.betten@open-xchange.com
 - Bugfix #13127: Changing an user's permission propagated to front-end
   according to mail folder's capability to hold folders/messages
* Tue Feb 03 2009 - thorben.betten@open-xchange.com
 - Bugfix #13081: Proper handling of empty form data in AJP module
* Mon Feb 02 2009 - thorben.betten@open-xchange.com
 - Bugfix #13116: Checking IMAP response's key prior to casting to a FetchResponse
* Mon Feb 02 2009 - marcus.klein@open-xchange.com
 - Bugfix #13100: Corrected typo in exception message.
 - Bugfix #13115: Adding check if a found contact can be updated.
* Fri Jan 30 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12939: Use correct folder id when copying links and attachments to new change exception.
 - Bugfix #13086: Pay only attention to end date for notification sending in tasks.
* Fri Jan 30 2009 - marcus.klein@open-xchange.com
 - Bugfix #13112: Search spanning all folders was missing the folder definition in the SQL statement.
* Wed Jan 28 2009 - thorben.betten@open-xchange.com
 - New search/filter API.
 - Dummy registration of corresponding search service in server's activator for software-based search.
 - Added attribute fetcher for basic modules task, calendar, and contact.
 - Bugfix #13070: Using prefixes "Re: " and "Fwd: " for mail reply/forward in all languages
* Wed Jan 28 2009 - martin.herfurth@open-xchange.com
 - Bugfix #11835: Default parameter for "no recurrence" at webdav interface.
* Tue Jan 27 2009 - marcus.klein@open-xchange.com
 - Improved error handling of user server preferences storage classes.
 - Bugfix #12931: Read permissions on a task must be checked on all folders a task is mapped into.
* Tue Jan 27 2009 - martin.herfurth@open-xchange.com
 - Bugfix #13068: No reminder for Appointments, which are moved to the past.
* Fri Jan 23 2009 - francisco.laguna@open-xchange.com
 - Bugfix #13046: Decode plus signs correctly
* Fri Jan 23 2009 - marcus.klein@open-xchange.com
 - Added contact collect folder to email contact auto complete search and fixed searching for already existing collected contacts.
* Thu Jan 22 2009 - francisco.laguna@open-xchange.com
 - Node 3087: Added Test to verify search behaviour for first and last name search.
* Wed Jan 21 2009 - francisco.laguna@open-xchange.com
 - Bugfix #12985: Suppress notifications if only alarm setting is changed.
* Wed Jan 21 2009 - thorben.betten@open-xchange.com
 - Bugfix #11677: CLT control bundle tools work when JMX authentication is
   enabled
 - Bugfix #12952: Checking parsed ICal appointment if it lasts exactly one
   day. If so treat it as a full-time appointment.
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
* Fri Jan 16 2009 - marcus.klein@open-xchange.com
 - Bugfix #12885: Improved message for the delivery receipt email.
 - Bugfix #12971: Setting locale for start and end date in notification mails.
 - Bugfix #12947: Series reminder is actualized to most current occurrence instead of the next occurrence.
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
infostore*:com.openexchange.webdav.Infostore
in the apache configuration:
JkMount /infostore ajp13
JkMount /infostore/ ajp13
JkMount /infostore/* ajp13
#####################################################################
 - Fixed Bug #8676: Return correct lockdiscovery property on LOCK for LockNull resources.
* Fri Aug 03 2007 - thorben.betten@open-xchange.com
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
 - Bugfix #7679: Extended to allow special value "*" that will initialize
   the affected server sockets (AJP & JMX) with bind address left to null,
   thus it binds to all interfaces.
* Wed Jun 20 2007 - martin.kauss@open-xchange.com
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
