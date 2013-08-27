
Name:          open-xchange-admin-autocontextid
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-admin
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 13
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Administrative extension to automatically create context identifiers
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-autocontextid = %{version}
Obsoletes:     open-xchange-admin-plugin-autocontextid <= %{version}
Provides:      open-xchange-admin-plugin-autocontextid-client = %{version}
Obsoletes:     open-xchange-admin-plugin-autocontextid-client <= %{version}

%description
This package adds the administrative OSGi bundle that creates for every newly created context a straight rising context identifier. Without
this extension an identifier must be given when creating a context.

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
. /opt/open-xchange/lib/oxfunctions.sh
CONFFILES="plugin/autocid.properties mysql/autocid.sql"
for FILE in $CONFFILES; do
    ox_move_config_file /opt/open-xchange/etc/admindaemon /opt/open-xchange/etc $FILE
done

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%dir /opt/open-xchange/etc/mysql
%config(noreplace) /opt/open-xchange/etc/mysql/*

%changelog
* Tue Aug 27 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-03
* Tue Jun 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-06-13
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for public patch 2013-01-15
* Wed Oct 10 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Release build for EDP drop #2
* Fri Jun 15 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial packaging
