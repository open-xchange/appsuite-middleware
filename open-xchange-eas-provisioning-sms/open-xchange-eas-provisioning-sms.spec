%define __jar_repack %{nil}

Name:          open-xchange-eas-provisioning-sms
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       EAS provisioning extension to sent provisioning to mobile phones via SMS
Autoreqprov:   no
Requires(post): open-xchange-system >= @OXVERSION@
Requires:      open-xchange-eas-provisioning-core >= @OXVERSION@
Provides:      open-xchange-eas-provisioning-action
Provides:      open-xchange-mobile-configuration-json-action-sms = %{version}
Obsoletes:     open-xchange-mobile-configuration-json-action-sms < %{version}

%description
EAS provisioning extension to sent provisioning to mobile phones via SMS


Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions /opt/open-xchange/etc/eas-provisioning-sms.properties root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/eas-provisioning-sms.properties

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
