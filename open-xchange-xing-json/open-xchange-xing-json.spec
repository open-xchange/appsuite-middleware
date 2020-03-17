%define __jar_repack %{nil}

Name:          open-xchange-xing-json
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       This package provides API calls for XING-related actions
Autoreqprov:   no
Requires:      open-xchange-oauth >= @OXVERSION@

%description
This package provides API calls for XING-related actions.
With this package you can use XING actions like showing the newsfeed and inviting users to XING inside OX.

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
* Mon Jun 17 2019 Steffen Templin <steffen.templin@open-xchange.com>
prepare for 7.10.3 release
