%define __jar_repack %{nil}

Name:          open-xchange-hostname-config-cascade
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       This extension provides a HostnameService using information from the config cascade
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-config-cascade-hostname = %{version}
Obsoletes:     open-xchange-config-cascade-hostname < %{version}

%description
Using this extension the hostname for contexts can be configured using the config cascade.

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
/usr/share
%doc /usr/share/doc/open-xchange-hostname-config-cascade/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
