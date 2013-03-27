
Name:          open-xchange-ajp
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 15
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       AJPv13 protocol based connector between backend and web server
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-httpservice
Conflicts:     open-xchange-grizzly

%description
This package installs the OSGi bundle that implements an AJPv13 protocol based web server connector. The web server and the Open-Xchange
backend are connected through this connector to provide the backend data to the web frontend or any other client through the HTTP or HTTPS
protocol. Normally Apache is used as web server and it is possible to use mod_jk or mod_proxy_ajp to establish the connection.
AJPv13 is optimized to use as few as possible connections to the backend by reusing AJPv13 connections for subsequent HTTP/HTTPS requests.
This package and its bundle provide the OSGi http service internally for registering HTTP servlets and resources.

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

    # SoftwareChange_Request-1120
    pfile=/opt/open-xchange/etc/ajp.properties
    if ! ox_exists_property AJP_BACKLOG $pfile; then
       ox_set_property AJP_BACKLOG 0 $pfile
    fi

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
* Wed Mar 27 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-04-04
* Fri Mar 01 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-03-07
* Mon Feb 25 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-02-22
* Fri Feb 15 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-02-13
* Tue Jan 29 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 10 2013 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2013-01-10
* Thu Jan 03 2013 Marc Arens <marc.arens@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marc Arens <marc.arens@open-xchange.com>
Build for public patch 2012-12-31
* Wed Dec 12 2012 Marc Arens <marc.arens@open-xchange.com>
Build for public patch 2012-12-04
* Mon Nov 26 2012 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marc Arens <marc.arens@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marc Arens <marc.arens@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 06 2012 Marc Arens <marc.arens@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Marc Arens <marc.arens@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marc Arens <marc.arens@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marc Arens <marc.arens@open-xchange.com>
prepare for 6.22.1
* Wed Oct 10 2012 Marc Arens <marc.arens@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marc Arens <marc.arens@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marc Arens <marc.arens@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marc Arens <marc.arens@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marc Arens <marc.arens@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Marc Arens <marc.arens@open-xchange.com>
Release build for EDP drop #2
* Wed Jun 20 2012 Marc Arens <marc.arens@open-xchange.com>
Initial release
