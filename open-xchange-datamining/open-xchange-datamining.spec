
Name:           open-xchange-datamining
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 6
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
* Tue Apr 02 2013 Karsten Will <karsten.will@open-xchange.com>
Build for patch 2013-04-04
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
