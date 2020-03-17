%define __jar_repack %{nil}

Name:          open-xchange-sessionstorage-hazelcast
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange hazelcast-based sessionstorage
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for the hazelcast-based sessionstorage.

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

    PFILE=/opt/open-xchange/etc/hazelcast/sessions.properties

    # SCR-4041
    ox_set_property com.openexchange.hazelcast.configuration.map.name "sessions-7" $PFILE
    ox_set_property com.openexchange.hazelcast.configuration.map.indexes.attributes "altId, contextId, userId" $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%dir %config(noreplace) /opt/open-xchange/etc
%dir %config(noreplace) /opt/open-xchange/etc/hazelcast
/usr/share
%doc /usr/share/doc/open-xchange-sessionstorage-hazelcast/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release