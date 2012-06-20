
Name:          open-xchange
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires: java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Source1:       open-xchange.init
Summary:       Open-Xchange Backend
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-authentication
Requires:      open-xchange-authorization
Requires:      open-xchange-mailstore
Requires:      open-xchange-httpservice
Requires:      open-xchange-smtp >= @OXVERSION@

%description
This package only contains the dependencies to install a working Open-Xchange 7 backend system.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

mkdir -p %{buildroot}/etc/init.d
mkdir -p %{buildroot}/sbin

install -m 755 %{SOURCE1} %{buildroot}/etc/init.d/open-xchange
ln -sf ../etc/init.d/open-xchange %{buildroot}/sbin/rcopen-xchange

mkdir -p %{buildroot}/var/log/open-xchange
mkdir -m 750 -p %{buildroot}/var/spool/open-xchange/uploads

%post
if [ ${1:-0} -eq 2 ]; then
    COMMONCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
    for FILE in ${COMMONCONFFILES}; do
	if [ -e /opt/open-xchange/etc/common/${FILE} ]; then
	    mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
	    mv /opt/open-xchange/etc/common/${FILE} /opt/open-xchange/etc/${FILE}
	fi
    done

    GWCONFFILES="ajp.properties attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties imap.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mailjsoncache.properties mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties TidyConfiguration.properties TidyMessages.properties user.properties whitelist.properties"
    for FILE in ${GWCONFFILES}; do
	if [ -e /opt/open-xchange/etc/groupware/${FILE} ]; then
	    mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
	    mv /opt/open-xchange/etc/groupware/${FILE} /opt/open-xchange/etc/${FILE}
	fi
    done
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir %attr(750,open-xchange,open-xchange) /var/log/open-xchange
%dir %attr(750,open-xchange,root) /var/spool/open-xchange/uploads
/etc/init.d/open-xchange
/sbin/rcopen-xchange

%changelog
