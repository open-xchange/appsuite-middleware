
Name:          open-xchange
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
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

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir %attr(750,open-xchange,root) /var/log/open-xchange
%dir %attr(750,open-xchange,root) /var/spool/open-xchange/uploads
/etc/init.d/open-xchange
/sbin/rcopen-xchange

%changelog
