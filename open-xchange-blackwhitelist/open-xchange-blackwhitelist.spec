%define __jar_repack %{nil}

Name:           open-xchange-blackwhitelist
BuildArch:      noarch
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        JSON interface for some black/white list implementation
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
This package installs an OSGi bundle that adds the JSON interface for some black/white list implementation. The concrete implementation
needs to be installed additionally and is not required by this package.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define docroot /var/www/html
%else
%define docroot /srv/www/htdocs
%endif

cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/settings

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
