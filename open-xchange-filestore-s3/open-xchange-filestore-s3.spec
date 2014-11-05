
Name:          open-xchange-filestore-s3
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
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
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'
    PFILE=/opt/open-xchange/etc/filestore-s3.properties

    # SoftwareChange_Request-2061
    ox_add_property com.openexchange.filestore.s3.[filestoreID].bucketName "" $PFILE
    ox_add_property com.openexchange.filestore.s3.[filestoreID].pathStyleAccess true $PFILE
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
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-s3.properties
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Wed Nov 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.0 release
* Fri Oct 31 2014 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Markus Wagner <markus.wagner@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Markus Wagner <markus.wagner@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-30
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
