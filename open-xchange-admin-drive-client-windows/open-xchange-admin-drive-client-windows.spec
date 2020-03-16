%define __jar_repack %{nil}

Name:          open-xchange-admin-drive-client-windows
BuildArch:     noarch
res: java-1.8.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The windows drive client branding configuration CLT
Autoreqprov:   no
Requires:      open-xchange-drive-client-windows >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@

%description
This package adds the branding configuration command-line tools for the windows drive client updater.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/

%changelog
* Tue Jan 26 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.1 release
* Tue Nov 17 2015 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial packaging
