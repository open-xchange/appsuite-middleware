%define __jar_repack %{nil}

Name:          open-xchange-drive-comet
BuildArch:     noarch
BuildRequires: open-xchange-drive
BuildRequires: open-xchange-grizzly
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Drive push implementation using Comet and using less system resources
Autoreqprov:   no
Requires:      open-xchange-drive >= @OXVERSION@
Requires:      open-xchange-grizzly >= @OXVERSION@

%description
This package should be installed if a real push implementation for the drive synchronization is wanted. This push implementation uses less
system resources by using the Grizzly application server which allows freeing threads although the request is still active.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
