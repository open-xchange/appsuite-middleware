%define __jar_repack %{nil}

Name:          open-xchange-push-imapidle
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange IMAP IDLE Push Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-imap >= @OXVERSION@

%description
The Open-Xchange IMAP IDLE Push Bundle

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

    PFILE=/opt/open-xchange/etc/push_imapidle.properties

    # SCR-4030
    ox_set_property com.openexchange.push.imapidle.clusterLock local $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/push_imapidle.properties
%config(noreplace) /opt/open-xchange/etc/hazelcast/imapidle.properties
/usr/share
%doc /usr/share/doc/open-xchange-push-imapidle/properties/

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
