
Name:           open-xchange-datamining
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 7
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
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*

%changelog
* Tue Aug 27 2013 Karsten Will <karsten.will@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Karsten Will <karsten.will@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Karsten Will <karsten.will@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Karsten Will <karsten.will@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Karsten Will <karsten.will@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-09
* Mon Aug 05 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Karsten Will <karsten.will@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Karsten Will <karsten.will@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Karsten Will <karsten.will@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Karsten Will <karsten.will@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Karsten Will <karsten.will@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Karsten Will <karsten.will@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Karsten Will <karsten.will@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Karsten Will <karsten.will@open-xchange.com>
Feature freeze for 7.2.2 release
* Mon Jun 10 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Karsten Will <karsten.will@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Karsten Will <karsten.will@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Karsten Will <karsten.will@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Karsten Will <karsten.will@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Karsten Will <karsten.will@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Karsten Will <karsten.will@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Karsten Will <karsten.will@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Karsten Will <karsten.will@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Karsten Will <karsten.will@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Karsten Will <karsten.will@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Karsten Will <karsten.will@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Karsten Will <karsten.will@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Karsten Will <karsten.will@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Karsten Will <karsten.will@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Karsten Will <karsten.will@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-03-07
* Fri Mar 01 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Karsten Will <karsten.will@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Mon Feb 25 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Karsten Will <karsten.will@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Karsten Will <karsten.will@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Karsten Will <karsten.will@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Karsten Will <karsten.will@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-01-28
* Tue Jan 15 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-01-23
* Tue Jan 15 2013 Karsten Will <karsten.will@open-xchange.com>
Initial import
* Thu Jan 10 2013 Karsten Will <karsten.will@open-xchange.com>
prepare for 7.0.1
