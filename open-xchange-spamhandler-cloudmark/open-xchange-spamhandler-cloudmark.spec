%define __jar_repack %{nil}

Name:           open-xchange-spamhandler-cloudmark
BuildArch:      noarch
Version:	@OXVERSION@
%define        ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange Cloudmark Spamhandler
Autoreqprov:   no
Requires:       open-xchange-core
Provides:	open-xchange-spamhandler

%description
The Open-Xchange Cloudmark Spamhandler can be used in a generic way since it can just
report Spam and/or Ham messages to any configured EMail address.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%post
. /opt/open-xchange/lib/oxfunctions.sh
if [ ${1:-0} -eq 2 ]; then

    PFILE=/opt/open-xchange/etc/spamhandler_cloudmark.properties

    # SoftwareChange_Request-3039
    ox_add_property com.openexchange.spamhandler.cloudmark.wrapMessage true $PFILE
fi

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-spamhandler-cloudmark/properties/

%changelog
* Mon Jun 17 2019 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.10.3 release
