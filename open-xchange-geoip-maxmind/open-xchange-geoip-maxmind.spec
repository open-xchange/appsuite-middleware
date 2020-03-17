%define __jar_repack %{nil}

Name:          open-xchange-geoip-maxmind
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange GeoIP service
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@
Obsoletes:     open-xchange-geoip < @OXVERSION@
Provides:      open-xchange-geoip = @OXVERSION@

%description
This package provides connectivity to a GeoIP service based on the MaxMind's GeoDatabase.


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

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

%files
%defattr(-,root,root)
/opt/open-xchange
/usr/share
%doc /usr/share/doc/open-xchange-geoip-maxmind/properties/

%changelog
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
