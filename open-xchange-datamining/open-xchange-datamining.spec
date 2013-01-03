
Name:           open-xchange-datamining
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 3
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        This bundle provides a datamining tool (described at http://oxpedia.org/wiki/index.php?title=Datamining)
Requires:      open-xchange-core >= @OXVERSION@

%description
This bundle provides a datamining tool

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

%post
. /opt/open-xchange/lib/oxfunctions.sh

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*.properties
%config(noreplace) /opt/open-xchange/etc/meta/*
%config(noreplace) /opt/open-xchange/etc/contextSets/*

/opt/open-xchange/lib
/opt/open-xchange/sbin

%changelog
* Fri Dec 21 2012 Karsten Will <karsten.will@open-xchange.com>
First release candidate for 7.0.0
