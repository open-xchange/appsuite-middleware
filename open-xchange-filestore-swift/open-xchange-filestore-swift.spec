%define __jar_repack %{nil}

Name:          open-xchange-filestore-swift
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 24
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Filestore implementation storing files in a Swift storage
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for storing files in a Swift storage.

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
ox_update_permissions /opt/open-xchange/etc/filestore-swift.properties root:open-xchange 640
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'
    PFILE=/opt/open-xchange/etc/filestore-swift.properties

fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-swift.properties
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Wed Nov 23 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-28 (3758)
* Fri Nov 11 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-21 (3730)
* Fri Oct 28 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-07 (3677)
* Mon Oct 17 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-10-24 (3629)
* Fri Sep 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-10-10 (3596)
* Tue Sep 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-26 (3571)
* Mon Sep 05 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-12 (3546)
* Fri Aug 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-29 (3521)
* Mon Aug 08 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-15 (3489)
* Thu Jul 28 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-01 (3466)
* Thu Jul 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-07-18 (3433)
* Thu Jun 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-07-04 (3400)
* Wed Jun 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-06-20 (3347)
* Fri Jun 03 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-06-06 (3317)
* Fri May 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-05-23 (3294)
* Thu May 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-05-19 (3305)
* Fri May 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-05-09 (3272)
* Mon Apr 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-04-25 (3263)
* Fri Apr 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-04-25 (3239)
* Thu Apr 07 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-04-07 (3228)
* Wed Mar 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Thorben Betten <thorben.betten@open-xchange.com>
Fourth preview for 7.8.1 release
* Mon Feb 22 2016 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
