%define __jar_repack %{nil}

Name:          open-xchange-grizzly
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange HTTP Server and Servlet Container
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-httpservice

%description
This package implements an HTTP Server and provides the OSGi HTTP service.


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
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PFILE=/opt/open-xchange/etc/grizzly.properties

    # SoftwareChange_Request-2289
    ox_remove_property com.openexchange.http.grizzly.hasAJPEnabled $PFILE

    # SoftwareChange_Request-2864
    ox_add_property com.openexchange.http.grizzly.keepAlive true $PFILE
    ox_add_property com.openexchange.http.grizzly.tcpNoDelay true $PFILE
    ox_add_property com.openexchange.http.grizzly.readTimeoutMillis 60000 $PFILE
    ox_add_property com.openexchange.http.grizzly.writeTimeoutMillis 60000 $PFILE

    # SoftwareChange_Request-3248
    ox_add_property com.openexchange.http.grizzly.hasSSLEnabled false $PFILE
    ox_add_property com.openexchange.http.grizzly.enabledCipherSuites '' $PFILE
    ox_add_property com.openexchange.http.grizzly.keystorePath '' $PFILE
    ox_add_property com.openexchange.http.grizzly.keystorePassword '' $PFILE

    # SoftwareChange_Request-3827
    ox_remove_property com.openexchange.http.grizzly.ping $PFILE
    ox_remove_property com.openexchange.http.grizzly.pingDelay $PFILE
    ox_remove_property com.openexchange.http.grizzly.maxPingCount $PFILE

    # SoftwareChange_Request-3828
    if ! grep "documented properties" $PFILE > /dev/null; then
        cat<<EOF >> $PFILE
### HTTP session
################################################################################

# Please see documented properties at https://documentation.open-xchange.com/latest/middleware/configuration/properties.html#grizzly
EOF
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/grizzly.properties
/usr/share
%doc /usr/share/doc/open-xchange-grizzly/properties/

%changelog
* Mon Jun 17 2019 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marc Arens <marc.arens@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marc Arens <marc.arens@open-xchange.com>
First preview for 7.10.2 release
