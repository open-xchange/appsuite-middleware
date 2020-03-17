%define __jar_repack %{nil}

Name:          open-xchange-imap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange IMAP Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-mailstore

%description
This package implements the IMAP connection to the mail storages for Open-Xchange.

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

    PFILE=/opt/open-xchange/etc/imap.properties

    # SoftwareChange_Request-2820
    ox_add_property com.openexchange.imap.allowSORTDISPLAY false $PFILE

    # SoftwareChange_Request-3134
    ox_add_property com.openexchange.imap.fallbackOnFailedSORT false $PFILE

    # SoftwareChange_Request-3343
    ox_add_property com.openexchange.imap.rootSubfoldersAllowed "" $PFILE

    # SoftwareChange_Request-3345
    ox_add_property com.openexchange.imap.auditLog.enabled false $PFILE

    # SoftwareChange_Request-3413
    ox_add_property com.openexchange.imap.initWithSpecialUse false $PFILE

    # SoftwareChange_Request-3447
    VALUE=$(ox_read_property com.openexchange.imap.imapSearch $PFILE)
    if [ "imap" = "$VALUE" ]; then
        ox_set_property com.openexchange.imap.imapSearch force-imap $PFILE
    fi

    # SoftwareChange_Request-3636
    VALUE=$(ox_read_property com.openexchange.imap.ssl.protocols $PFILE)
    if [ "SSLv3 TLSv1" = "$VALUE" ]; then
        ox_set_property com.openexchange.imap.ssl.protocols "" $PFILE
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-imap/properties/

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
