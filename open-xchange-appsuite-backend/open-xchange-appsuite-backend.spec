%define __jar_repack %{nil}

Name:          open-xchange-appsuite-backend
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Backend extensions to serve OX App Suite frontend
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-halo
Requires:      open-xchange-appsuite-manifest

%description
This package installs the OSGi bundles and configuration files that are necessary to use the OX App Suite frontend. If the Open-Xchange 6
frontend is used, this package has no effect at all.
The available Apps and the access permissions in the OX App Suite frontend are configured by this package. Additionally this package contains
dependencies to all necessary other extensions of the backend.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    # SoftwareChange_Request-2880
    ox_add_property io.ox/core//pdf/enableRangeRequests true /opt/open-xchange/etc/settings/appsuite.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/
%config(noreplace) /opt/open-xchange/etc/as-config.yml
%config(noreplace) /opt/open-xchange/etc/manifests.properties
%config(noreplace) /opt/open-xchange/etc/meta/appsuite.yaml
%config(noreplace) /opt/open-xchange/etc/settings/appsuite.properties
%config(noreplace) /opt/open-xchange/etc/settings/upsell-appsuite.properties
/usr/share/
%doc /usr/share/doc/open-xchange-appsuite-backend/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
