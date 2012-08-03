
Name:           open-xchange-gui-help-plugin
BuildArch: 	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        Creative Commons Attribution-Noncommercial-Share Alike 2.5 Generic
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open-Xchange GUI Help Server Plug-In
Requires:	open-xchange-core

%description
Open-Xchange GUI Help Plug-In

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    if [ -e /opt/open-xchange/etc/groupware/settings/open-xchange-gui-help-plugin.properties ]; then
        mv /opt/open-xchange/etc/settings/open-xchange-gui-help-plugin.properties /opt/open-xchange/etc/settings/open-xchange-gui-help-plugin.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/settings/open-xchange-gui-help-plugin.properties /opt/open-xchange/etc/settings/open-xchange-gui-help-plugin.properties
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/settings
%config(noreplace) /opt/open-xchange/etc/settings/*

%changelog
* Fri Aug 03 2012 Marcus Klein  <marcus.klein@open-xchange.com>
  - Initial package for new backend
