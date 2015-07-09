
Name:           open-xchange-mobile-push
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module for Open-Xchange Mobile Push implementation
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Server module for Open-Xchange Mobile Push functionality
This package installes the server module for the mobile push functionality.
The service used for receiving push messages is Google Cloud Messaging for Android (GCM).
Furthermore a watchdog is installed, which looks for valid push listener every five minutes.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/mobilepushevent.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Initial mobile push release.
