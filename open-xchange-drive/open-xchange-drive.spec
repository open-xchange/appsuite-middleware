
Name:           open-xchange-drive
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 27
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module for Open-Xchange Drive file synchronization
Requires:      open-xchange-core >= @OXVERSION@

%description
Server module for Open-Xchange Drive file synchronization

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
%config(noreplace) /opt/open-xchange/etc/drive.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Tue Jan 28 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 10 2014 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-17
* Thu Dec 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-23
* Tue Dec 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-12-12
* Mon Nov 11 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-08
* Tue Nov 05 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-30
* Tue Oct 22 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-21
* Wed Oct 09 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Build for patch 2013-10-07
* Tue Sep 24 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Ninth candidate for 7.4.0 release
* Mon Sep 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Eighth candidate for 7.4.0 release
* Tue Aug 27 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth candidate for 7.4.0 release
* Mon Aug 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 7.4.0
* Fri Aug 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 09 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Initial release
