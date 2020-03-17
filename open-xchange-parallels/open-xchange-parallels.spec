%define __jar_repack %{nil}

Name:           open-xchange-parallels
BuildArch:      noarch
BuildRequires:  open-xchange-admin
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Extensions for integration with Parallels
Autoreqprov:    no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:       open-xchange-admin-soap >= @OXVERSION@
Requires:       open-xchange-spamhandler
Provides:       open-xchange-authentication
Provides:       open-xchange-custom-parallels = %{version}
Obsoletes:      open-xchange-custom-parallels < %{version}
Conflicts:      open-xchange-authentication-database open-xchange-authentication-ldap open-xchange-authentication-imap open-xchange-authentication-kerberos

%description
This package contains the authentication bundle and a bundle for branding. The spam handler is installed with a separate package.

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

    # SoftwareChange_Request-2723
    ox_add_property com.openexchange.custom.parallels.branding.guestfallbackhost "" /opt/open-xchange/etc/parallels.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) /opt/open-xchange/etc/settings/parallels-ui.properties

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
