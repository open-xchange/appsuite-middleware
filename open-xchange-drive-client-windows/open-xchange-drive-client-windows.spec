%define __jar_repack %{nil}

Name:          open-xchange-drive-client-windows
BuildArch:     noarch
BuildRequires: open-xchange-admin
BuildRequires: open-xchange-core >= @OXVERSION@, open-xchange-client-onboarding >= @OXVERSION@, open-xchange-drive >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       New updater for windows drive clients
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@, open-xchange-client-onboarding >= @OXVERSION@, open-xchange-drive >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Requires:      open-xchange-drive-client-windows-files

%description
This package offers windows drive clients the possibility to update themselves without the use of the ox-updater.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s reloaddriveclients listdriveclients)

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
/usr/share
%doc /usr/share/doc/open-xchange-drive-client-windows/properties/

%changelog
* Mon Jun 17 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.3 release
