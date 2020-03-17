%define __jar_repack %{nil}

Name:          open-xchange-admin-reseller
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Extends the administration of the backend with the reseller level
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Requires:      open-xchange-admin-autocontextid >= @OXVERSION@
Provides:      open-xchange-admin-plugin-reseller = %{version}
Obsoletes:     open-xchange-admin-plugin-reseller < %{version}

%description
This extension adds the reseller administration level to the administrative RMI interface. The master administrator can now create reseller
administators which are the allowed to manage contexts on their own. All reseller administrators are completely isolated in the cluster
installation. For every reseller it looks like he is working with his own cluster installation and he is not able to see contexts of other
resellers.

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
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%dir /usr/share/doc/open-xchange-admin-reseller/
%dir /usr/share/doc/open-xchange-admin-reseller/javadoc/
/usr/share/doc/open-xchange-admin-reseller/javadoc/*
%doc /usr/share/doc/open-xchange-admin-reseller/properties/


%changelog
* Mon Jun 17 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.10.3 release
