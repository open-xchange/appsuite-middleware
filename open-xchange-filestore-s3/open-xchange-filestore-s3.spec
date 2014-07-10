
Name:          open-xchange-filestore-s3
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 9
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Filestore implementation storing files in a S3 API compatible storage
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for storing files in a S3 API compatible storage.

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
ox_update_permissions /opt/open-xchange/etc/filestore-s3.properties root:open-xchange 640

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-s3.properties
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Thu Jul 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-15
* Tue Jul 01 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-07
* Mon Jun 23 2014 Markus Wagner <markus.wagner@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Sixth release candidate for 7.6.0
* Fri Jun 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-26
* Wed Feb 12 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth release candidate for 7.4.2
* Tue Feb 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth release candidate for 7.4.2
* Wed Dec 18 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.2
* Thu Oct 10 2013 Markus Wagner <markus.wagner@open-xchange.com>
First sprint increment for 7.4.0 release
* Mon Oct 07 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.1
* Tue Jul 16 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.0
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Mar 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.2.0
* Tue Sep 11 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial release
