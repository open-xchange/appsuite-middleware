%define __jar_repack %{nil}

Name:          open-xchange-admin
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange backend administration extension
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-admin-plugin-hosting = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting < %{version}
Provides:      open-xchange-admin-lib = %{version}
Obsoletes:     open-xchange-admin-lib < %{version}
Provides:      open-xchange-admin-plugin-hosting-client = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-client < %{version}
Provides:      open-xchange-admin-plugin-hosting-doc = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-doc < %{version}
Provides:      open-xchange-admin-client = %{version}
Obsoletes:     open-xchange-admin-client < %{version}
Provides:      open-xchange-admin-plugin-hosting-lib = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-lib < %{version}
Provides:      open-xchange-admin-doc = %{version}
Obsoletes:     open-xchange-admin-doc < %{version}
%if 0%{?suse_version}
Requires:      mysql-client >= 5.0.0
%endif
%if 0%{?fedora_version} || 0%{?rhel_version}
Requires:      mysql >= 5.0.0
%endif
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
Requires:      util-linux-ng
%else
Requires:      util-linux
%endif
Requires:      bzip2
Requires:      coreutils
Requires:      gawk
Requires:      grep
Requires:      gzip
Requires:      file
Requires:      findutils
Requires:      sed
Requires:      tar

%description
This package installs the OSGi bundles to the backend that provide the RMI interface to administer the installation. This package contains
the RMI interfaces for the overall administrative tasks like registering, changing and deleting servers, databases and filestores. It also
contains the interfaces for creating, changing and deleting contexts, users, groups and resources.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # SoftwareChange_Request-2197
    ox_add_property SCHEMA_MOVE_MAINTENANCE_REASON 1431655765 /opt/open-xchange/etc/plugin/hosting.properties

    # SoftwareChange_Request-2285
    MOD_PASSWDS=0
    TMPFILE=$(mktemp)
    while read LINE; do
        case "$LINE" in
            \#*|*:crypt:*|*:bcrypt:*)
                # ignore commented and already converted lines
                echo $LINE
                ;;
            *)
                IFS=":"
                PARTS=( $LINE )
                unset IFS
                # only modify matching lines
                if [ -n "${PARTS[0]}" ] && [ -n "${PARTS[1]}" ]
                then
                    echo ${PARTS[0]}:crypt:${PARTS[1]}
                    MOD_PASSWDS=$(($MOD_PASSWDS+1))
                else
                    echo $LINE
                fi
                ;;
        esac
    done < /opt/open-xchange/etc/mpasswd >$TMPFILE
    if [ ${MOD_PASSWDS} -gt 0 ]
    then
      cat $TMPFILE > /opt/open-xchange/etc/mpasswd
    fi
    rm $TMPFILE

    # SoftwareChange_Request-2382
    ox_add_property MASTER_ACCOUNT_OVERRIDE false /opt/open-xchange/etc/AdminDaemon.properties

    # SoftwareChange_Request-2535
    # ox_add_property drive globaladdressbookdisabled,infostore,deniedportal /opt/open-xchange/etc/ModuleAccessDefinitions.properties
    # Bug 44000
    pfile=/opt/open-xchange/etc/ModuleAccessDefinitions.properties
    if ox_exists_property drive $pfile
    then
        defin=$(ox_read_property drive $pfile)
        if [ "$defin" = "globaladdressbookdisabled,infostore,deniedportal" ]
        then
            ox_set_property drive infostore,deniedportal,readcreatesharedfolders,editpublicfolders $pfile
        fi
    else
        ox_add_property drive infostore,deniedportal,readcreatesharedfolders,editpublicfolders $pfile
    fi

    # SoftwareChange_Request-2699
    ox_add_property ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET false /opt/open-xchange/etc/AdminUser.properties

    ox_add_property AVERAGE_USER_SIZE 100 /opt/open-xchange/etc/AdminUser.properties

    # SoftwareChange_Request-3226
    PFILE=/opt/open-xchange/etc/ModuleAccessDefinitions.properties
    VALUE=$(ox_read_property drive $PFILE)
    if [ "infostore,deniedportal,readcreatesharedfolders,editpublicfolders" = "$VALUE" ]; then
        ox_set_property drive 'infostore,deniedportal,contacts,collectemailaddresses' $PFILE
    fi

    # SoftwareChange_Request-3676
    ox_add_property DEFAULT_TIMEZONE Europe/Berlin /opt/open-xchange/etc/AdminUser.properties

    # SoftwareChange_Request-4170
    ox_add_property LOCK_ON_WRITE_CONTEXT_INTO_PAYLOAD_DB false /opt/open-xchange/etc/plugin/hosting.properties

    # SoftwareChange_Request-4351
    PFILE=/opt/open-xchange/etc/ModuleAccessDefinitions.properties
    expression='# webdavxml (interface for OXtender for Microsoft Outlook, used by KDE for synchronization)'
    $(contains "^${expression}$" $PFILE) && sed -i "s/^${expression}$/${expression} [DEPRECATED]/" $PFILE

    # SoftwareChange_Request-147
    ox_remove_property CREATE_CONTEXT_USE_UNIT /opt/open-xchange/etc/plugin/hosting.properties

    SCR=SCR-322.admin
    ox_scr_todo ${SCR} && {
      prop_file=/opt/open-xchange/etc/ModuleAccessDefinitions.properties
      orig_line='# publication (Permission to publish content of folders)'
      new_line='# publication (Permission to publish content of folders, Deprecated with v7.10.2, will have no impact) [DEPRECATED]'
      $(contains "^${orig_line}$" ${prop_file}) && {
        sed -i -e "s/${orig_line}/${new_line}/" ${prop_file}
      }
      ox_scr_done ${SCR}
    }

    SCR=SCR-338
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/plugin/hosting.properties
      prop_key=CONTEXT_STORAGE
      old_default=com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage
      new_default=com.openexchange.admin.plugin.hosting.storage.mysqlStorage.OXContextMySQLStorage
      curr_val=$(ox_read_property ${prop_key} ${pfile})
      if [ "${old_default}" = "${curr_val}" ]
      then
        ox_set_property ${prop_key} ${new_default} ${pfile}
      fi
      ox_scr_done ${SCR}
    }

    SCR=SCR-466
    ox_scr_todo ${SCR} && {
      pfile=/opt/open-xchange/etc/AdminUser.properties
      p_key=DEFAULT_PASSWORD_MECHANISM
      old_default=SHA
      new_default=SHA-256
      curr_val=$(ox_read_property ${p_key} ${pfile})
      if [ "${curr_val}" = "${old_default}" ]
      then
        ox_set_property ${p_key} ${new_default} ${pfile}
      fi
      ox_scr_done ${SCR}
    }

fi
ox_update_permissions "/opt/open-xchange/etc/mpasswd" root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
/opt/open-xchange/etc/mysql
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/mpasswd
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*
%dir /usr/share/doc/open-xchange-admin/
%dir /usr/share/doc/open-xchange-admin/javadoc/
/usr/share/doc/open-xchange-admin/javadoc/*
%doc /usr/share/doc/open-xchange-admin/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
