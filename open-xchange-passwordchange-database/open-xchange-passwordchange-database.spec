
Name:          open-xchange-passwordchange-database
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 6
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange Password Change Database Bundle
Requires:      open-xchange-core >= @OXVERSION@
Conflicts:     open-xchange-passwordchange-script

%description
The Open-Xchange Password Change Database Bundle

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
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-07
* Tue Feb 19 2013 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.0.1
* Fri Feb 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Carsten Hoeger <choeger@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Carsten Hoeger <choeger@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.0
* Wed Jul 11 2012 Carsten Hoeger <choeger@open-xchange.com>
Initial release
