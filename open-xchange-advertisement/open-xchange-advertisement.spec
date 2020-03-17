%define __jar_repack %{nil}

Name:          open-xchange-advertisement
BuildArch:     noarch
BuildRequires: open-xchange-core >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange Advertisement
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package offers the possibility to manage configurations for advertisements.
 
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
/usr/share/
%doc /usr/share/doc/open-xchange-advertisement/properties/

%changelog
* Mon Jun 17 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.3 release
