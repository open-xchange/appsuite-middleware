%define configfiles configfiles.list
%define __jar_repack %{nil}

Name:          open-xchange-core
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-osgi
BuildRequires: open-xchange-xerces
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 20
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The essential core of an Open-Xchange backend
Autoreqprov:   no
Requires:      open-xchange-osgi >= @OXVERSION@
Requires:      open-xchange-xerces >= @OXVERSION@
Requires(pre): open-xchange-system >= @OXVERSION@
Obsoletes:     open-xchange-freebusy < %{version}

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
perl -pi -e 's;^(.*?)\s+(.*/paths.perfMap)$;$2;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(autoconfig|mail|configdb|server|filestorage|management|secret|sessiond)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(secrets|tokenlogin-secrets))$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

%post
. /opt/open-xchange/lib/oxfunctions.sh
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # Fix for bug 25999
    ox_remove_property com.openexchange.servlet.sessionCleanerInterval /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-2456
    ox_add_property com.openexchange.caching.jcs.remoteInvalidationForPersonalFolders false /opt/open-xchange/etc/cache.properties

    # SoftwareChange_Request-2464
    ox_add_property com.openexchange.hazelcast.shutdownOnOutOfMemory false /opt/open-xchange/etc/hazelcast.properties

    # SoftwareChange_Request-2470
    ox_add_property com.openexchange.publish.createModifyEnabled false /opt/open-xchange/etc/publications.properties

    # SoftwareChange_Request-2541
    VALUE=$(ox_read_property com.openexchange.hazelcast.maxOperationTimeout /opt/open-xchange/etc/hazelcast.properties)
    if [ "5000" = "$VALUE" ]; then
        ox_set_property com.openexchange.hazelcast.maxOperationTimeout 30000 /opt/open-xchange/etc/hazelcast.properties
    fi

    # SoftwareChange_Request-2546
    VALUE=$(ox_read_property com.openexchange.push.allowedClients /opt/open-xchange/etc/mail-push.properties)
    if [ "\"USM-EAS*\", \"USM-JSON*\"" = "$VALUE" ]; then
        ox_set_property com.openexchange.push.allowedClients "\"USM-EAS*\", \"USM-JSON*\", \"open-xchange-mailapp\"" /opt/open-xchange/etc/mail-push.properties
    fi
    ox_add_property com.openexchange.push.allowPermanentPush true /opt/open-xchange/etc/mail-push.properties
    ox_add_property com.openexchange.push.credstorage.enabled false /opt/open-xchange/etc/mail-push.properties
    ox_add_property com.openexchange.push.credstorage.passcrypt "" /opt/open-xchange/etc/mail-push.properties
    ox_add_property com.openexchange.push.credstorage.rdb false /opt/open-xchange/etc/mail-push.properties

    # SoftwareChange_Request-2549
    VALUE=$(ox_read_property com.openexchange.IPCheckWhitelist /opt/open-xchange/etc/server.properties)
    if [ "" = "$VALUE" ]; then
        ox_set_property com.openexchange.IPCheckWhitelist "\"open-xchange-mailapp\"" /opt/open-xchange/etc/server.properties
    fi

    # SoftwareChange_Request-2568
    ox_add_property com.openexchange.contact.storeVCards true /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.maxVCardSize 4194304 /opt/open-xchange/etc/contact.properties

    # SoftwareChange_Request-2575
    ox_add_property com.openexchange.capability.mobile_mail_app false /opt/open-xchange/etc/permissions.properties

    # SoftwareChange_Request-2630
    ox_add_property com.openexchange.capability.share_links true /opt/open-xchange/etc/permissions.properties
    ox_add_property com.openexchange.capability.invite_guests true /opt/open-xchange/etc/permissions.properties

    # SoftwareChange_Request-2652
    ox_add_property com.openexchange.contact.image.scaleImages true /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.image.maxWidth 250 /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.image.maxHeight 250 /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.image.scaleType 2 /opt/open-xchange/etc/contact.properties

    # SoftwareChange_Request-2662
    ox_add_property com.openexchange.file.storage.numberOfPregeneratedPreviews 20 /opt/open-xchange/etc/filestorage.properties

    # SoftwareChange_Request-2665
    ox_add_property com.openexchange.calendar.notify.poolenabled true /opt/open-xchange/etc/notification.properties

    # SoftwareChange_Request-2672
    ox_add_property com.openexchange.connector.shutdownFast false /opt/open-xchange/etc/server.properties
    ox_add_property com.openexchange.connector.awaitShutDownSeconds 90 /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-2698
    ox_add_property com.openexchange.mail.rateLimitDisabledRange "" /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-2772
    ox_add_property com.openexchange.ajax.response.includeArguments false /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-2811
    PFILE=/opt/open-xchange/etc/excludedupdatetasks.properties
    if ! grep "com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2" >/dev/null $PFILE; then
        cat >> $PFILE <<EOF

# Creates indexes on tables "prg_contacts" and "del_contacts" to improve auto-complete
!com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2
EOF
    fi

    # SoftwareChange_Request-2815
    ox_add_property html.tag.s '""' /opt/open-xchange/etc/whitelist.properties

    # SoftwareChange_Request-2821
    ox_add_property com.openexchange.mail.autoconfig.http.proxy "" /opt/open-xchange/etc/autoconfig.properties
    ox_add_property com.openexchange.mail.autoconfig.http.proxy.login "" /opt/open-xchange/etc/autoconfig.properties
    ox_add_property com.openexchange.mail.autoconfig.http.proxy.password "" /opt/open-xchange/etc/autoconfig.properties
    ox_add_property com.openexchange.mail.autoconfig.allowGuess true /opt/open-xchange/etc/autoconfig.properties

    # SoftwareChange_Request-2831
    ox_add_property com.openexchange.tools.images.transformations.maxSize 5242880 /opt/open-xchange/etc/server.properties
    ox_add_property com.openexchange.tools.images.transformations.maxResolution 12087962 /opt/open-xchange/etc/server.properties
    ox_add_property com.openexchange.tools.images.transformations.waitTimeoutSeconds 10 /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-2849
    PFILE=/opt/open-xchange/etc/permissions.properties
    if ! ox_exists_property com.openexchange.capability.archive_emails $PFILE; then
        ox_set_property com.openexchange.capability.archive_emails true $PFILE
    fi

    # SoftwareChange_Request-2914
    PFILE=/opt/open-xchange/etc/excludedupdatetasks.properties
    if ! grep "com.openexchange.groupware.update.tasks.FolderCorrectOwnerTask" >/dev/null $PFILE; then
        cat >> $PFILE <<EOF

# Corrects values in the 'created_from' column for folders nested below/underneath personal 'Trash' folder
!com.openexchange.groupware.update.tasks.FolderCorrectOwnerTask
EOF
    fi

    # SoftwareChange_Request-2990
    ox_add_property com.openexchange.hazelcast.healthMonitorLevel silent /opt/open-xchange/etc/hazelcast.properties

    # SoftwareChange_Request-2993
    ox_add_property com.openexchange.contact.fulltextAutocomplete false /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.fulltextIndexFields "DISPLAY_NAME, SUR_NAME, GIVEN_NAME, TITLE, SUFFIX, MIDDLE_NAME, COMPANY, EMAIL1, EMAIL2, EMAIL3" /opt/open-xchange/etc/contact.properties

    # SoftwareChange_Request-3000
    ox_add_property com.openexchange.contact.autocomplete.fields "GIVEN_NAME, SUR_NAME, DISPLAY_NAME, EMAIL1, EMAIL2, EMAIL3" /opt/open-xchange/etc/contact.properties
    ox_add_property com.openexchange.contact.search.fields "ADDRESS_FIELDS, EMAIL_FIELDS, NAME_FIELDS, PHONE_FIELDS, CATEGORIES, COMPANY, DEPARTMENT, COMMERCIAL_REGISTER, POSITION" /opt/open-xchange/etc/contact.properties

    # SoftwareChange_Request-3034
    ox_add_property com.openexchange.mail.bodyDisplaySizeLimit 10485760 /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-3054
    ox_add_property com.openexchange.mail.forwardUnquoted false /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-3113
    TMPFILE=$(mktemp)
    rm -f $TMPFILE
    cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/logger -d @name -r -
<configuration>
    <logger name="liquibase.ext.logging.slf4j.Slf4jLogger" level="WARN"/>
</configuration>
EOF
    if [ -e $TMPFILE ]; then
      cat $TMPFILE > /opt/open-xchange/etc/logback.xml
      rm -f $TMPFILE
    fi

    # SoftwareChange_Request-3159
    ox_add_property com.openexchange.snippet.quota.limit -1 /opt/open-xchange/etc/snippets.properties

    # SoftwareChange_Request-3219
    ox_remove_property com.openexchange.participant.MaximumNumberParticipants /opt/open-xchange/etc/participant.properties

    # SoftwareChange_Request-3248
    ox_add_property com.openexchange.connector.networkSslListenerPort 8010 /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-3254
    VALUE=$(ox_read_property com.openexchange.mail.account.blacklist /opt/open-xchange/etc/mail.properties)
    if [ "" = "$VALUE" ]; then
        ox_set_property com.openexchange.mail.account.blacklist "127.0.0.1-127.255.255.255,localhost" /opt/open-xchange/etc/mail.properties
    fi
    ox_add_property com.openexchange.mail.account.whitelist.ports "143,993, 25,465,587, 110,995" /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-3225
    ox_add_property com.openexchange.tools.images.transformations.preferThumbnailThreshold 0.8 /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-3246
    ox_add_property com.openexchange.mail.mailStartTls false /opt/open-xchange/etc/mail.properties
    ox_add_property com.openexchange.mail.transportStartTls false /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-3350
    if grep "XX:DisableExplicitGC" >/dev/null /opt/open-xchange/etc/ox-scriptconf.sh; then
      sed -i '/^JAVA_XTRAOPTS=/s/ -XX:+DisableExplicitGC//' /opt/open-xchange/etc/ox-scriptconf.sh
    fi

    # SoftwareChange_Request-3355,3417
    oldlink=$(ox_read_property object_link /opt/open-xchange/etc/notification.properties)
    if [[ ${oldlink} == *"[uiwebpath]#m=[module]&i=[object]&f=[folder]" ]]
    then
      newlink=$(echo ${oldlink} | sed -e 's;^\(.*\)/\[uiwebpath\].*$;\1/[uiwebpath]#!!\&app=io.ox/[module]\&id=[object]\&folder=[folder];')
      ox_set_property object_link ${newlink} /opt/open-xchange/etc/notification.properties
    fi

    # SoftwareChange_Request-3356
    ox_add_property com.openexchange.ajax.login.checkPunyCodeLoginString false /opt/open-xchange/etc/login.properties

    # SoftwareChange_Request-3405
    ox_add_property com.openexchange.ical.updateTimezones true /opt/open-xchange/etc/server.properties

    # SoftwareChange_Request-3406
    # search from the named appender up to the next closing pattern or
    # suffixPattern and replace only %message with %sanitisedMessage in that
    # context
    logconfig=/opt/open-xchange/etc/logback.xml
    tmp=${logconfig}.tmp
    cp -a --remove-destination $logconfig $tmp
    sed -r -i '/<appender .*name="FILE".*>/,/<\/pattern>/ s/%message/%sanitisedMessage/g' $tmp
    sed -r -i '/<appender .*name="FILE_COMPAT".*>/,/<\/pattern>/ s/%message/%sanitisedMessage/g' $tmp
    sed -r -i '/<appender .*name="SYSLOG".*>/,/<\/suffixPattern>/ s/%message/%sanitisedMessage/g' $tmp
    if [[ $(ox_md5 $tmp) != $(ox_md5 $logconfig) ]]; then
      cat $tmp >$logconfig
    else
      rm $tmp
    fi

    # SoftwareChange_Request-3421
    ox_remove_property com.openexchange.mail.transport.enablePublishOnExceededQuota /opt/open-xchange/etc/transport.properties
    ox_remove_property com.openexchange.mail.transport.publishPrimaryAccountOnly /opt/open-xchange/etc/transport.properties
    ox_remove_property com.openexchange.mail.transport.sendAttachmentToExternalRecipients /opt/open-xchange/etc/transport.properties
    ox_remove_property com.openexchange.mail.transport.provideLinksInAttachment /opt/open-xchange/etc/transport.properties
    ox_remove_property com.openexchange.mail.transport.publishedDocumentTimeToLive /opt/open-xchange/etc/transport.properties
    ox_remove_property com.openexchange.mail.transport.externalRecipientsLocale /opt/open-xchange/etc/transport.properties

    # SoftwareChange_Request-3482
    ox_add_property com.openexchange.secret.recovery.fast.enabled true /opt/open-xchange/etc/secret.properties

    # SoftwareChange_Request-3528
    ox_add_property html.tag.code '""' /opt/open-xchange/etc/whitelist.properties

    # Bug #45347
    old_key=io.ox.calendar//participantBlacklist
    new_key=io.ox/calendar//participantBlacklist
    propfile=/opt/open-xchange/etc/settings/participant-blacklist.properties

    if ! ox_exists_property ${new_key} ${propfile}
    then
        if ox_exists_property ${old_key} ${propfile}
        then
            value=$(ox_read_property ${old_key} ${propfile})
            ox_remove_property ${old_key} ${propfile}
            ox_add_property ${new_key} "${value}" ${propfile}
        else
            ox_comment ${old_key} remove ${propfile}
            ox_remove_property ${old_key} ${propfile}
            ox_add_property ${new_key} "" ${propfile}
        fi
    fi

    # SoftwareChange_Request-3616
    ox_add_property com.openexchange.mail.compose.share.preview.timeout 1000 /opt/open-xchange/etc/mail-compose.properties

    # SoftwareChange_Request-3637
    VALUE=$(ox_read_property com.openexchange.connector.maxRequestParameters /opt/open-xchange/etc/server.properties)
    if [ "30" = "$VALUE" ]; then
        ox_set_property com.openexchange.connector.maxRequestParameters 1000 /opt/open-xchange/etc/server.properties
    fi

    # SoftwareChange_Request-3773
    if grep '^# Maximum number of open Files for the groupware$' >/dev/null /opt/open-xchange/etc/ox-scriptconf.sh; then
      sed -i '/^# Maximum number of open Files for the groupware$/{i\
# Maximum number of open Files for the groupware. This value will only be\
# applied when using sysv init. For systemd have a look at the drop-in configs\
# at /etc/systemd/system/open-xchange.service.d
d
}' /opt/open-xchange/etc/ox-scriptconf.sh
    fi

    # SoftwareChange_Request-3784
    VALUE=$(ox_read_property com.openexchange.IPCheckWhitelist /opt/open-xchange/etc/server.properties)
    if [ "\"open-xchange-mailapp\"" = "$VALUE" ]; then
        ox_set_property com.openexchange.IPCheckWhitelist "\"open-xchange-mailapp\", \"open-xchange-mobile-api-facade\"" /opt/open-xchange/etc/server.properties
    fi

    # SoftwareChange_Request-3859
    VALUE=$(ox_read_property NRFILES /opt/open-xchange/etc/ox-scriptconf.sh)
    VALUE=${VALUE//\"/}
    if [ "8192" = "$VALUE" ]; then
        ox_set_property NRFILES 65536 /opt/open-xchange/etc/ox-scriptconf.sh
    fi

    # SoftwareChange_Request-3862
    if ! grep -Eq "^#\s?html.tag.form" >/dev/null /opt/open-xchange/etc/whitelist.properties; then
      ox_comment html.tag.form add /opt/open-xchange/etc/whitelist.properties
    fi
    if ! grep -Eq "^#\s?html.tag.input" >/dev/null /opt/open-xchange/etc/whitelist.properties; then
      ox_comment html.tag.input add /opt/open-xchange/etc/whitelist.properties
    fi

    # SoftwareChange_Request-3882
    ox_add_property NPROC 65536 /opt/open-xchange/etc/ox-scriptconf.sh

    # SoftwareChange_Request-3934
    if ! grep -Eq "^#\s?html.style.list-style-image" >/dev/null /opt/open-xchange/etc/whitelist.properties; then
      ox_comment html.style.list-style-image add /opt/open-xchange/etc/whitelist.properties
    fi

    # SoftwareChange_Request-4033
    TMPFILE=$(mktemp)
    rm -f $TMPFILE
    cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/logger -d @name -m -
<configuration>
    <logger name="com.hazelcast.internal.monitors" level="INFO"/>
</configuration>
EOF
    if [ -e $TMPFILE ]; then
      cat $TMPFILE > /opt/open-xchange/etc/logback.xml
      rm -f $TMPFILE
    fi
    # Bug #53993: Zap duplicate loggers
    rm -f $TMPFILE
    cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/logger -d @name -z -r -
<configuration>
    <logger name="com.hazelcast.internal.diagnostics" level="INFO"/>
</configuration>
EOF
    if [ -e $TMPFILE ]; then
      cat $TMPFILE > /opt/open-xchange/etc/logback.xml
      rm -f $TMPFILE
    fi

    # SoftwareChange_Request-4059
    ox_remove_property com.openexchange.mail.enforceSecureConnection /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-4094
    VALUE=$(ox_read_property com.openexchange.mail.autoconfig.ispdb /opt/open-xchange/etc/autoconfig.properties)
    if [ "https://live.mozillamessaging.com/autoconfig/v1.1/" = "$VALUE" ]; then
        ox_set_property com.openexchange.mail.autoconfig.ispdb "https://autoconfig.thunderbird.net/v1.1/" /opt/open-xchange/etc/autoconfig.properties
    fi

    # SoftwareChange_Request-4096
    # use subshell to not pollute current env when sourcing ox-scriptconf.sh
    (
      . /opt/open-xchange/lib/oxfunctions.sh
      to_migrate=/opt/open-xchange/etc/ox-scriptconf.sh

      # Pitfall_1 JAVA_XTRAOPTS="${JAVA_XTRAOPTS} foo=bar"
      # was added by some customers to ox-scriptconf.sh
      unset JAVA_XTRAOPTS
      . ${to_migrate}
      opts_old=${JAVA_XTRAOPTS}

      # only migrate if pre RM-177 style options are found
      if [[ ! -z "${opts_old}" ]]
      then
        backup=${to_migrate}.$(date +%s)
        cp -a --remove-destination ${to_migrate} ${backup}

        gc_regx="-XX:\+UseConcMarkSweepGC|-XX:\+UseParNewGC|-XX:CMSInitiatingOccupancyFraction|-XX:\+UseCMSInitiatingOccupancyOnly|-XX:NewRatio|-XX:\+DisableExplicitGC"
        log_regx="-Dlogback.threadlocal.put.duplicate|-XX:-OmitStackTraceInFastThrow"
        mem_regx="-Xmx|-XX:MaxHeapSize|-XX:MaxPermSize|-XX:\+UseTLAB"
        net_regx="-Dsun.net.inetaddr.ttl|-Dnetworkaddress.cache.ttl|-Dnetworkaddress.cache.negative.ttl"
        osgi_regx="-Dosgi.compatibility.bootdelegation"
        server_regx="-server|-Djava.awt.headless"

        opts_gc=()
        opts_log=()
        opts_mem=()
        opts_net=()
        opts_osgi=()
        opts_server=()
        opts_other=()

        ((debug)) && echo "The options are: ${opts_old}"

        opt_arr=(${opts_old})
        num_opts=${#opt_arr[@]}
        ((debug)) && echo "There are ${num_opts} options to migrate"
        for ((i=0; i < num_opts; i++))
        do
          #assign single options to appropriate array
          ((debug)) && echo "Option ${i}: ${opt_arr[${i}]}"
          curr_opt=${opt_arr[${i}]}
          if [[ "${curr_opt}" =~ ${gc_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is a gc opt"
            opts_gc+=(${curr_opt})
          elif [[ "${curr_opt}" =~ ${log_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is a log opt"
            opts_log+=(${curr_opt})
          elif [[ "${curr_opt}" =~ ${mem_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is a mem opt"
            opts_mem+=(${curr_opt})
          elif [[ "${curr_opt}" =~ ${net_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is a net opt"
            opts_net+=(${curr_opt})
          elif [[ "${curr_opt}" =~ ${osgi_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is an osgi opt"
            opts_osgi+=(${curr_opt})
          elif [[ "${curr_opt}" =~ ${server_regx} ]]
          then
            ((debug)) && echo "${curr_opt} is a server opt"
            opts_server+=(${curr_opt})
          else
            ((debug)) && echo "${curr_opt} is another opt"
            opts_other+=(${curr_opt})
          fi
        done

        ox_add_property JAVA_OPTS_GC     "\"${opts_gc[*]}\""     "${to_migrate}"
        ox_add_property JAVA_OPTS_LOG    "\"${opts_log[*]}\""    "${to_migrate}"
        ox_add_property JAVA_OPTS_MEM    "\"${opts_mem[*]}\""    "${to_migrate}"
        ox_add_property JAVA_OPTS_NET    "\"${opts_net[*]}\""    "${to_migrate}"
        ox_add_property JAVA_OPTS_OSGI   "\"${opts_osgi[*]}\""   "${to_migrate}"
        ox_add_property JAVA_OPTS_SERVER "\"${opts_server[*]}\"" "${to_migrate}"
        ox_add_property JAVA_OPTS_OTHER  "\"${opts_other[*]}\""  "${to_migrate}"
        sed -i -e '${a\
    # Define options for debugging the groupware Java virtual machine, disabled by default.\
    #JAVA_OPTS_DEBUG="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/mnt/heapdump -Xloggc:/var/log/open-xchange/gc.log -verbose:gc -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintTenuringDistribution"
        }' "${to_migrate}"

        # Pitfall_1: removes all occurrences
        ox_remove_property JAVA_XTRAOPTS "${to_migrate}"
      fi
    )

    # SoftwareChange_Request-4098
    ox_remove_property com.openexchange.mail.attachmentDisplaySizeLimit /opt/open-xchange/etc/mail.properties

    # SoftwareChange_Request-4149
    ox_set_property marital_status 'Marital status' /opt/open-xchange/importCSV/open-xchange.properties
    if [ "Number of employee" == "$(ox_read_property employee_type /opt/open-xchange/importCSV/open-xchange.properties)" ]; then
      sed -i 's/employee_type=Number of employee/number_of_employees=Employee ID/g' /opt/open-xchange/importCSV/open-xchange.properties
    fi

    # SoftwareChange_Request-4204
    pfile=/opt/open-xchange/etc/whitelist.properties
    for property in html.style.page-break-{after,before,inside}
    do
      ox_remove_property ${property} ${pfile}
    done

    # SoftwareChange_Request-4249
    set -e
    TMPFILE=$(mktemp)
    rm -f $TMPFILE
    /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -s 4249 -o $TMPFILE
    if [ -e $TMPFILE ]; then
      cat $TMPFILE > /opt/open-xchange/etc/logback.xml
      rm -f $TMPFILE
    fi
    set +e

    # SoftwareChange_Request-66
    ox_remove_property com.openexchange.hazelcast.jmxDetailed /opt/open-xchange/etc/hazelcast.properties

    # SoftwareChange_Request-77
    PFILE=/opt/open-xchange/etc/cache.ccf
    NAMES=( jcs.region.User.cacheattributes.MaxObjects jcs.region.UserConfiguration.cacheattributes.MaxObjects jcs.region.UserPermissionBits.cacheattributes.MaxObjects jcs.region.UserSettingMail.cacheattributes.MaxObjects jcs.region.Context.cacheattributes.MaxObjects )
    OLDDEFAULTS=( 40000 20000 20000 20000 10000 )
    NEWDEFAULTS=( 4000000 4000000 4000000 4000000 1000000 )
    for I in $(seq 1 ${#NAMES[@]}); do
      VALUE=$(ox_read_property ${NAMES[$I-1]} $PFILE)
      if [ "${VALUE}" = "${OLDDEFAULTS[$I-1]}" ]; then
        ox_set_property ${NAMES[$I-1]} "${NEWDEFAULTS[$I-1]}" $PFILE
      fi
    done

    # SoftwareChange_Request-82
    ox_remove_property com.openexchange.caching.jcs.enabled /opt/open-xchange/etc/cache.properties

    # SoftwareChange_Request-151
    VALUE=$(ox_read_property com.openexchange.push.allowedClients /opt/open-xchange/etc/mail-push.properties)
    if [ "\"USM-EAS*\", \"USM-JSON*\", \"open-xchange-mailapp\"" = "${VALUE}" ]; then
        ox_set_property com.openexchange.push.allowedClients "${VALUE}, \"open-xchange-mobile-api-facade*\"" /opt/open-xchange/etc/mail-push.properties
    fi

    # SoftwareChange_Request-160
    whlipr=/opt/open-xchange/etc/whitelist.properties
    ox_add_property html.style.webkit-box-sizing '""' ${whlipr}
    ox_add_property html.style.moz-box-sizing '""' ${whlipr}
    ox_add_property html.style.box-sizing '"border-box"' ${whlipr}
    ox_add_property html.style.-webkit-box-sizing '"border-box"' ${whlipr}
    ox_add_property html.style.-moz-box-sizing '"border-box"' ${whlipr}

    # SoftwareChange_Request-174
    ox_remove_property IGNORE_SHARED_ADDRESSBOOK /opt/open-xchange/etc/foldercache.properties

    # SoftwareChange_Request-175
    ox_add_property com.openexchange.server.migrationRedirectURL "" /opt/open-xchange/etc/server.properties

fi

PROTECT=( autoconfig.properties configdb.properties hazelcast.properties jolokia.properties mail.properties mail-push.properties management.properties secret.properties secrets server.properties sessiond.properties share.properties tokenlogin-secrets )
for FILE in "${PROTECT[@]}"
do
    ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
done
ox_update_permissions "/opt/open-xchange/etc/ox-scriptconf.sh" root:root 644
ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
ox_update_permissions "/var/spool/open-xchange/uploads" open-xchange:root 750
ox_update_permissions "/var/log/open-xchange" open-xchange:root 750
ox_update_permissions "/opt/open-xchange/sbin/reloadconfiguration" root:open-xchange 740
exit 0

%clean
%{__rm} -rf %{buildroot}

%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/documentation
%dir /opt/open-xchange/documentation/etc
/opt/open-xchange/documentation/etc/*.yml
%dir /opt/open-xchange/etc
%dir /opt/open-xchange/etc/contextSets
%dir /opt/open-xchange/etc/meta
%dir /opt/open-xchange/etc/settings
%dir /opt/open-xchange/i18n/
%dir /opt/open-xchange/importCSV/
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/osgi/config.ini.template
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/templates/
/opt/open-xchange/templates/*
%dir /opt/open-xchange/etc/hazelcast
%dir %attr(750, open-xchange, root) /var/log/open-xchange
%dir /var/spool/open-xchange
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/uploads
%doc docs/
%doc com.openexchange.server/doc/examples
%doc com.openexchange.database/doc/examples

%changelog
* Mon Nov 12 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-11-19 (4966)
* Mon Oct 29 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-11-05 (4933)
* Mon Oct 08 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-10-15 (4918)
* Tue Sep 25 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-10-01 (4897)
* Mon Sep 24 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-09-21 (4900)
* Mon Sep 10 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-09-17 (4882)
* Mon Aug 27 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-09-03 (4870)
* Wed Aug 15 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-08-20 (4863)
* Thu Aug 02 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-08-13 (4853)
* Fri Jul 20 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-07-25 (4835)
* Fri Jun 29 2018 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Marcus Klein <marcus.klein@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Marcus Klein <marcus.klein@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Marcus Klein <marcus.klein@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Marcus Klein <marcus.klein@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.0 release
* Fri May 19 2017 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Marcus Klein <marcus.klein@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Marcus Klein <marcus.klein@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Marcus Klein <marcus.klein@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Tue Oct 20 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Mon Oct 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Thu Oct 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.8.0 release
* Wed Sep 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-28 (2767)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.8.0 release
* Tue Sep 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Mon Sep 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.0 release
* Wed Sep 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Fri Aug 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.0 release
* Mon Aug 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Wed Aug 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.8.0
* Tue Aug 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Mon Mar 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Marcus Klein <marcus.klein@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-25
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Wed Feb 11 2015 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-26
* Wed Jan 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-29
* Mon Jan 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Mon Jan 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Fri Dec 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.6.2 release
* Thu Dec 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-09
* Tue Dec 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.6.2 release
* Thu Nov 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Wed Nov 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-21
* Tue Nov 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Wed Nov 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.0 release
* Tue Nov 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-10
* Fri Oct 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Mon Jan 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.2
* Wed Jan 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-22
* Mon Jan 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-29
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Tue Oct 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-11
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-15
* Thu Oct 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-07
* Thu Sep 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-23
* Tue Sep 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Ninth candidate for 7.4.0 release
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Thu Sep 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-05
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-26
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.4.0 release
* Fri Aug 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-30
* Wed Aug 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-03
* Tue Aug 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.0
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
Second build for patch 2013-07-18
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
