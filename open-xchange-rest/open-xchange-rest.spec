
Name:          open-xchange-rest
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 1
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Micro services REST API
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package adds the bundles to the backend that provide the internal REST
API for the micro services architecture. Currently it contains the REST API
HTTP interface and some core services. These are services for accessing the
configuration, the database and for mapping email addresses to specific users
of certain contexts.

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
* Mon May 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.0
* Fri May 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
initial packaging for internal REST services API
