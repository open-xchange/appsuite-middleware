Name:          open-xchange-ui7-backend
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
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
Summary:       Bundles specific to the Open-Xchange 7 web front-end
Requires:      open-xchange-core >= @OXVERSION@

%description
Bundles which are specific to the Open-Xchange 7 web front-end.
Currently, includes the following:
- apps servlet: Concatenates apps on the fly to reduce the number of HTTP
  requests. 

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
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*


%changelog
* Thu Oct 11 2012 Viktor Pracht <viktor.pracht@open-xchange.com>
Release build for EDP drop #5
* Tue Sep 11 2012 Viktor Pracht <viktor.pracht@open-xchange.com>
Initial release
