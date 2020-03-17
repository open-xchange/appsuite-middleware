%define __jar_repack %{nil}

Name:          open-xchange-pop3
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange POP3 Plugin
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@

%description
Open Xchange POP3 Plugin

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

    PFILE=/opt/open-xchange/etc/pop3.properties

    # SoftwareChange_Request-3636
    VALUE=$(ox_read_property com.openexchange.pop3.ssl.protocols $PFILE)
    if [ "SSLv3 TLSv1" = "$VALUE" ]; then
        ox_set_property com.openexchange.pop3.ssl.protocols "" $PFILE
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-pop3/properties/


%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
