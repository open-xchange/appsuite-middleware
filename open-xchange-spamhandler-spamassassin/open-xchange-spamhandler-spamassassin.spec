%define __jar_repack %{nil}

Name:           open-xchange-spamhandler-spamassassin
BuildArch:      noarch
Version:	@OXVERSION@
%define        ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL_2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange SpamAssassin Handler
Autoreqprov:   no
Requires:       open-xchange-core
Provides:	open-xchange-spamhandler

%description
The Open-Xchange SpamAssassin Handler

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

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-spamhandler-spamassassin/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
