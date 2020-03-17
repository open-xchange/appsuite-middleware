%define __jar_repack %{nil}

Name:           open-xchange-mobile-config
BuildArch: 	    noarch
Version:	      @OXVERSION@
%define         ox_release 0
Release:	      %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        Creative Commons Attribution-Noncommercial-Share Alike 2.5 Generic
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Config files for the Open-Xchange Mobile UI
Autoreqprov:    no
Requires(pre):  open-xchange-system
Requires:       open-xchange-core >= @OXVERSION@

%description
 This package needs to be installed on the backend hosts of a cluster installation. It adds configuration files to the backend allowing the
 administrator to define some defaults for the mobile web app. Additionally it adds configuration paths on the backend for the Mobile Web Interface
 that allows to store end user preferences.

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
%config(noreplace) /opt/open-xchange/etc/settings/*
%config(noreplace) /opt/open-xchange/etc/meta/*

%changelog
* Mon Jun 17 2019 Marcus Klein <jenkins@hudson-slave-1.netline.de>
prepare for 7.10.3 release
