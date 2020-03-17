%define __jar_repack %{nil}

Name:          open-xchange-pns-impl
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The implementation bundle for Push Notification Service
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
The implementation bundle for Push Notification Service

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
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-apns_http2-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-apns-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-gcm-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-wns-options.yml
/usr/share
%doc /usr/share/doc/open-xchange-pns-impl/properties/

%changelog
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
