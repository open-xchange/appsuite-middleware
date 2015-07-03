
Name:          open-xchange-guard-backend
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-admin
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 6
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       OX Guard backend component
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@

%description
This package adds the bundles to the backend needed to operate the OX
Guard product.

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
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Fri Jul 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 10 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Fri Feb 20 2015 Thorben Betten <thorben.betten@open-xchange.com>
initial packaging for OX Guard backend component
