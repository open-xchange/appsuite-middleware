
Name:          open-xchange-ajp
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 1
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange AJP Connector
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-httpservice
Conflicts:     open-xchange-grizzly

%description
This package implements an AJP Connector and provides the OSGi HTTP service.


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
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc ajp.properties

    # SoftwareChange_Request-1081
    pfile=/opt/open-xchange/etc/ajp.properties
    ox_remove_property AJP_COYOTE_SOCKET_HANDLER $pfile

    # SoftwareChange_Request-1093
    pfile=/opt/open-xchange/etc/ajp.properties
    ox_remove_property AJP_CONNECTION_POOL $pfile
    ox_remove_property AJP_CONNECTION_POOL_SIZE $pfile
    ox_remove_property AJP_REQUEST_HANDLER_POOL $pfile
    ox_remove_property AJP_REQUEST_HANDLER_POOL_SIZE $pfile
    ox_remove_property AJP_MOD_JK $pfile
    ox_remove_property AJP_MAX_NUM_OF_SOCKETS $pfile
    ox_remove_property AJP_CHECK_MAGIC_BYTES_STRICT $pfile
    ##
    ## end update from < 6.21
    ##
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Tue Jul 03 2012 Marc Arens <marc.arens@open-xchange.com>
Release build for EDP drop #2
* Wed Jun 20 2012 Marc Arens <marc.arens@open-xchange.com>
Initial release
