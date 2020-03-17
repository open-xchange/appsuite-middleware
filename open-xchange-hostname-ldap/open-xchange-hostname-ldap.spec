%define __jar_repack %{nil}

Name:          open-xchange-hostname-ldap
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       This bundle provides a service which reads hostnames for the contexts from an LDAP
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This bundle provides a service which reads hostnames for the contexts from an LDAP

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PFILE=/opt/open-xchange/etc/ldaphostname.properties

    # SoftwareChange_Request-2721
    ox_add_property com.openexchange.hostname.ldap.guest_result_attribute oxGuestDomain $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
