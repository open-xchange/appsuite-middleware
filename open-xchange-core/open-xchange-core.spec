%define configfiles configfiles.list
%define __jar_repack %{nil}

Name:          open-xchange-core
BuildArch:     noarch
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The essential core of an Open-Xchange backend
Autoreqprov:   no
Requires(post): patch
Requires:      open-xchange-osgi >= @OXVERSION@
Requires:      open-xchange-xerces >= @OXVERSION@
Requires:      open-xchange-hazelcast
Requires(pre): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-system >= @OXVERSION@
Obsoletes:     open-xchange-freebusy < %{version}
Conflicts:     open-xchange-publish < 7.10.2
Obsoletes:     open-xchange-publish < 7.10.2
Conflicts:     open-xchange-geoip-ip2location >= 7.10.2
Obsoletes:     open-xchange-geoip-ip2location >= 7.10.2

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
cp -rv --preserve=all ./opt ./usr %{buildroot}/
(cd %{buildroot}/opt/open-xchange/bundles/com.openexchange.logback.configuration && ln -s ../../etc/logback.xml)
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

    # SoftwareChange_Request-193
    VALUE=$(ox_read_property com.openexchange.push.allowedClients /opt/open-xchange/etc/mail-push.properties)
    if [ "\"USM-EAS*\", \"USM-JSON*\", \"open-xchange-mailapp\", \"open-xchange-mobile-api-facade*\"" = "${VALUE}" ]; then
        ox_set_property com.openexchange.push.allowedClients "\"USM-EAS*\", \"open-xchange-mobile-api-facade*\"" /opt/open-xchange/etc/mail-push.properties
    fi

    SCR=SCR-208
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/configdb.properties

      declare -A dmap
      dmap[3]="useUnicode=true"
      dmap[4]="characterEncoding=UTF-8"
      dmap[5]="autoReconnect=false"
      dmap[6]="useServerPrepStmts=false"
      dmap[7]="useTimezone=true"
      dmap[8]="serverTimezone=UTC"
      dmap[9]="connectTimeout=15000"
      dmap[10]="socketTimeout=15000"

      for x in {3..10}
      do
        default_val=${dmap[$x]}
        for prop_type in readProperty writeProperty
        do
          prop=${prop_type}.${x}
          curr_val=$(ox_read_property ${prop} ${pfile})
          if [ -n "${curr_val}" ]
          then
            if [ "${default_val}" == "${curr_val}" ]
            then
              ox_remove_property ${prop} ${pfile}
            fi
          fi
        done
      done
      ox_scr_done ${SCR}
    }

    # SoftwareChange_Request-236
    PFILE=/opt/open-xchange/etc/cache.ccf
    NAMES=( jcs.region.CalendarCache jcs.region.CalendarCache.cacheattributes jcs.region.CalendarCache.cacheattributes.MaxObjects jcs.region.CalendarCache.cacheattributes.MemoryCacheName jcs.region.CalendarCache.cacheattributes.UseMemoryShrinker jcs.region.CalendarCache.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.CalendarCache.cacheattributes.ShrinkerIntervalSeconds jcs.region.CalendarCache.cacheattributes.MaxSpoolPerRun jcs.region.CalendarCache.elementattributes jcs.region.CalendarCache.elementattributes.IsEternal jcs.region.CalendarCache.elementattributes.MaxLifeSeconds jcs.region.CalendarCache.elementattributes.IdleTime jcs.region.CalendarCache.elementattributes.IsSpool jcs.region.CalendarCache.elementattributes.IsRemote jcs.region.CalendarCache.elementattributes.IsLateral )
    for I in $(seq 1 ${#NAMES[@]}); do
      ox_remove_property ${NAMES[$I-1]} $PFILE
    done

    # SoftwareChange_Request-240
    pfile=/opt/open-xchange/etc/contact.properties
    image_k=com.openexchange.contact.scaleVCardImages
    width_k=com.openexchange.contact.image.maxWidth
    height_k=com.openexchange.contact.image.maxHeight
    image_v=$(ox_read_property ${image_k} ${pfile})
    width_v=$(ox_read_property ${width_k} ${pfile})
    height_v=$(ox_read_property ${height_k} ${pfile})
    if [ "200x200" == "${image_v}" ] && [ "250" == "${width_v}" ] && [ "250" == "${height_v}" ]
    then
      ox_set_property ${image_k} "600x800" ${pfile}
      ox_set_property ${width_k} "600" ${pfile}
      ox_set_property ${height_k} "800" ${pfile}
    fi

    # SoftwareChange_Request-287
    pfile=/opt/open-xchange/etc/contact.properties
    scale_k=com.openexchange.contact.image.scaleType
    scale_v=$(ox_read_property ${scale_k} ${pfile})
    if [ -n "${scale_v}" ]
    then
      if [ "2" == "${scale_v}" ]
      then
        ox_set_property ${scale_k} "1" ${pfile}
      else
        ox_set_property ${scale_k} ${scale_v} ${pfile}
      fi
    fi

    SCR=SCR-299
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/cache.ccf
      for region in OXFolderCache OXFolderQueryCache GlobalFolderCache
      do
        curr_val=$(ox_read_property jcs.region.${region}.elementattributes.MaxLifeSeconds ${pfile})
        if [ "-1" = "${curr_val}" ]
        then
          ox_set_property jcs.region.${region}.elementattributes.MaxLifeSeconds 3600 ${pfile}
        fi
      done
      ox_scr_done ${SCR}
    }

    SCR=SCR-322.core
    ox_scr_todo ${SCR} && {
      prop_file=/opt/open-xchange/etc/server.properties
      prop_key=PUBLISH_REVOKE
      if ox_exists_property ${prop_key} ${prop_file}
      then
        prop_val=$(ox_read_property ${prop_key} ${prop_file})
        if [ -z "${prop_val}" ]
        then
          ox_remove_property ${prop_key} ${prop_file} 
        fi
      fi
      ox_scr_done ${SCR}
    }

    SCR=SCR-391
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/mime.types
      type="video/x-matroska mkv"
      if ! contains "${type}" ${pfile}
      then
        echo "${type}" >> ${pfile}
        LC_COLLATE=C sort -o ${pfile} ${pfile}
      fi
      ox_scr_done ${SCR}
    }

    SCR=SCR-422
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/mime.types
      type="image/heic heic"
      if ! contains "${type}" ${pfile}
      then
        echo "${type}" >> ${pfile}
        LC_COLLATE=C sort -o ${pfile} ${pfile}
      fi
      type="image/heif heif"
      if ! contains "${type}" ${pfile}
      then
        echo "${type}" >> ${pfile}
        LC_COLLATE=C sort -o ${pfile} ${pfile}
      fi
      ox_scr_done ${SCR}
    }

    # SCR-426
    if ! contains "Allow users to configure the showContactImage setting" /opt/open-xchange/etc/settings/ui.properties; then
        cat <<EOF | (cd /opt/open-xchange/etc && patch -p3 -N -r -)
diff --git a/com.openexchange.groupware.settings.extensions/conf/settings/ui.properties b/com.openexchange.groupware.settings.extensions/conf/settings/ui.properties
index 3ce3af8146f..61ee86367b8 100644
--- a/com.openexchange.groupware.settings.extensions/conf/settings/ui.properties
+++ b/com.openexchange.groupware.settings.extensions/conf/settings/ui.properties
@@ -2,7 +2,7 @@
 # Possible values: embedded and popups
 ui/global/windows/mode/value=embedded

-# All user to configure the latter
+# Allow users to configure the windows mode
 # Possible values: true|false
 ui/global/windows/mode/configurable=true

@@ -10,7 +10,7 @@ ui/global/windows/mode/configurable=true
 # Possible values: tabbased|simple
 ui/global/toolbar/mode/value=tabbased

-# All user to configure the latter
+# Allow users to configure the toolbar mode
 # Possible values: true|false
 ui/global/toolbar/mode/configurable=true

@@ -18,7 +18,7 @@ ui/global/toolbar/mode/configurable=true
 # Possible values: true|false
 ui/mail/showContactImage/value = true

-# All user to configure the latter
+# Allow users to configure the showContactImage setting
 # Possible values: true|false
 # Please configure the ui.yml accordingly.
 ui/mail/showContactImage/configurable = true
EOF
    fi

    # SCR-470
    if ! contains "onmouseleave" /opt/open-xchange/etc/globaleventhandlers.list; then
      sed -i "s/onmounseleave/onmouseleave/" /opt/open-xchange/etc/globaleventhandlers.list
  fi

  SCR=SCR-480
  ox_scr_todo ${SCR} && {
    set -e
    TMPFILE=$(mktemp)
    rm -f $TMPFILE
    /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -s 480 -o $TMPFILE
    if [ -e $TMPFILE ]; then
      cat $TMPFILE > /opt/open-xchange/etc/logback.xml
      rm -f $TMPFILE
    fi
    set +e
    ox_scr_done ${SCR}
  }

  SCR=SCR-481
  ox_scr_todo ${SCR} && {
    pfile=/opt/open-xchange/etc/hazelcast.properties
    pkey=com.openexchange.hazelcast.group.password
    comment="# - ${pkey}"

    if contains "${comment}" ${pfile}
    then
      sed -i -e "/^# If this is a single-node installation/,/^# - com.openexchange.hazelcast.network.interfaces/{
        /${comment}/d
      }" ${pfile}
    fi

    ox_remove_property ${pkey} ${pfile}
    ox_scr_done ${SCR}
  }

    SCR=SCR-489
    ox_scr_todo ${SCR} && {
      pfile_sessiond=/opt/open-xchange/etc/sessiond.properties
      pkey_sessiond=com.openexchange.sessiond.autologin
      pfile_share=/opt/open-xchange/etc/share.properties
      pkey_share=com.openexchange.share.autoLogin

      ox_remove_property ${pkey_sessiond} ${pfile_sessiond}
      ox_remove_property ${pkey_share} ${pfile_share}

      ox_scr_done ${SCR}
    }
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
/opt/open-xchange
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%config(noreplace) /opt/open-xchange/etc/hazelcast
%dir %attr(750, open-xchange, root) /var/log/open-xchange
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/uploads
/usr/share
%doc /usr/share/doc/open-xchange-core/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
