%define __jar_repack %{nil}

Name:          open-xchange-mailfilter
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Mailfilter Plugin
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
Open-Xchange Mailfilter Plugin.

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

    ox_update_permissions "/opt/open-xchange/etc/mailfilter.properties" root:open-xchange 640

    PFILE=/opt/open-xchange/etc/mailfilter.properties

    # SoftwareChange_Request-3843
    prefer_gssapi=$(ox_read_property com.openexchange.mail.filter.preferGSSAPI ${PFILE})
    ox_remove_property com.openexchange.mail.filter.preferGSSAPI $PFILE
    if [ "${prefer_gssapi}" = "true" ]
    then
      ox_add_property com.openexchange.mail.filter.preferredSaslMech GSSAPI $PFILE
    else
      ox_add_property com.openexchange.mail.filter.preferredSaslMech "" $PFILE
    fi

    # SoftwareChange_Request-3987
    OLDNAMES=( SIEVE_LOGIN_TYPE SIEVE_CREDSRC SIEVE_SERVER SIEVE_PORT SCRIPT_NAME SIEVE_AUTH_ENC NON_RFC_COMPLIANT_TLS_REGEX TLS VACATION_DOMAINS )
    NEWNAMES=( com.openexchange.mail.filter.loginType com.openexchange.mail.filter.credentialSource com.openexchange.mail.filter.server com.openexchange.mail.filter.port com.openexchange.mail.filter.scriptName com.openexchange.mail.filter.authenticationEncoding com.openexchange.mail.filter.nonRFCCompliantTLSRegex com.openexchange.mail.filter.tls com.openexchange.mail.filter.vacationDomains )
    for I in $(seq 1 ${#OLDNAMES[@]})
    do
        OLDNAME=${OLDNAMES[$I-1]}
        NEWNAME=${NEWNAMES[$I-1]}
        VALUE=$(ox_read_property $OLDNAME $PFILE)
        ox_add_property $NEWNAME "$VALUE" $PFILE
        ox_remove_property $OLDNAME $PFILE
    done

    # SoftwareChange_Request-4007
    ox_add_property com.openexchange.mail.filter.authTimeout 6000 $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/mailfilter.properties
/usr/share
%doc /usr/share/doc/open-xchange-mailfilter/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
