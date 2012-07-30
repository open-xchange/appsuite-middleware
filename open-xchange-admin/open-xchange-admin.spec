
Name:          open-xchange-admin
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange backend administration extension
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-admin-plugin-hosting = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting <= %{version}
Provides:      open-xchange-admin-lib = %{version}
Obsoletes:     open-xchange-admin-lib <= %{version}
Provides:      open-xchange-admin-plugin-hosting-client = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-client <= %{version}
Provides:      open-xchange-admin-plugin-hosting-doc = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-doc <= %{version}
Provides:      open-xchange-admin-client = %{version}
Obsoletes:     open-xchange-admin-client <= %{version}
Provides:      open-xchange-admin-plugin-hosting-lib = %{version}
Obsoletes:     open-xchange-admin-plugin-hosting-lib <= %{version}
Provides:      open-xchange-admin-doc = %{version}
Obsoletes:     open-xchange-admin-doc <= %{version}

%description
This package installs the OSGi bundles to the backend that provide the RMI interface to administer the installation.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post

if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    ##
    ## start update from < 6.21
    ##
    CONFFILES="AdminDaemon.properties Group.properties ModuleAccessDefinitions.properties RMI.properties Resource.properties Sql.properties mpasswd plugin/hosting.properties"
    for FILE in ${CONFFILES}; do
        if [ -e /opt/open-xchange/etc/admindaemon/${FILE} ]; then
            if [ -e /opt/open-xchange/etc/${FILE} ]; then
		mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
	    fi
            mv /opt/open-xchange/etc/admindaemon/${FILE} /opt/open-xchange/etc/${FILE}
        fi
    done
    if [ -e /opt/open-xchange/etc/admindaemon/User.properties ]; then
        mv /opt/open-xchange/etc/AdminUser.properties /opt/open-xchange/etc/AdminUser.properties.rpmnew
        mv /opt/open-xchange/etc/admindaemon/User.properties /opt/open-xchange/etc/AdminUser.properties
    fi

    ofile=/opt/open-xchange/etc/AdminDaemon.properties
    pfile=/opt/open-xchange/etc/rmi.properties
    if ox_exists_property BIND_ADDRESS $ofile; then
	oval=$(ox_read_property BIND_ADDRESS $ofile)
	if [ -n "$oval" ]; then
	   ox_set_property com.openexchange.rmi.host $oval $pfile
	fi
	ox_remove_property BIND_ADDRESS $ofile
    fi
    ofile=/opt/open-xchange/etc/RMI.properties
    if [ -e $ofile ]; then
	oval=$(ox_read_property RMI_PORT $ofile)
	if [ -n "$oval" ]; then
	   ox_set_property com.openexchange.rmi.port $oval $pfile
	fi
	rm -f $ofile
    fi

    # SoftwareChange_Request-1091
    # -----------------------------------------------------------------------
    pfile=/opt/open-xchange/etc/AdminUser.properties
    ox_remove_property CREATE_HOMEDIRECTORY $pfile
    ox_remove_property HOME_DIR_ROOT $pfile
    pfile=/opt/open-xchange/etc/AdminDaemon.properties
    ox_remove_property USER_PROP $pfile
    ox_remove_property GROUP_PROP $pfile
    ox_remove_property RESOURCE_PROP $pfile
    ox_remove_property RMI_PROP $pfile
    ox_remove_property SQL_PROP $pfile
    ox_remove_property MASTER_AUTH_FILE $pfile
    ox_remove_property ACCESS_COMBINATIONS_FILE $pfile

    # SoftwareChange_Request-1100
    # -----------------------------------------------------------------------
    pfile=/opt/open-xchange/etc/AdminDaemon.properties
    ox_remove_property SERVER_NAME $pfile
    ##
    ## end update from < 6.21
    ##

    ox_update_permissions "/opt/open-xchange/etc/mpasswd" open-xchange:root 600
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
/opt/open-xchange/etc/mysql
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) %attr(600,open-xchange,root) /opt/open-xchange/etc/mpasswd
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
