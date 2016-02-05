%define __jar_repack %{nil}

Name:          open-xchange-freebusy-ews
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
BuildRequires: open-xchange-freebusy
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 2
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Microsoft Exchange / Open-Xchange Free/Busy Interoperability
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-freebusy >= @OXVERSION@

%description
Free/Busy data provider and -publisher accessing an Microsoft Exchange server using EWS.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build

%post
. /opt/open-xchange/lib/oxfunctions.sh
PROTECT="freebusy_provider_ews.properties freebusy_publisher_ews.properties"
for FILE in $PROTECT
do
    ox_update_permissions /opt/open-xchange/etc/$FILE root:open-xchange 640
done

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/freebusy_provider_ews.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/freebusy_publisher_ews.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Feb 03 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.1 release
* Mon Oct 19 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Thu Oct 08 2015 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Markus Wagner <markus.wagner@open-xchange.com>
Sixth candidate for 7.8.0 release
* Fri Sep 25 2015 Markus Wagner <markus.wagner@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Markus Wagner <markus.wagner@open-xchange.com>
Fourth candidate for 7.8.0 release
* Mon Sep 07 2015 Markus Wagner <markus.wagner@open-xchange.com>
Third candidate for 7.8.0 release
* Fri Aug 21 2015 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.0 release
* Wed Aug 05 2015 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.8.0
* Wed Nov 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.0 release
* Thu Oct 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.2 release
* Thu Jun 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.1
* Wed Feb 12 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.0
* Wed Dec 18 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.2
* Thu Oct 10 2013 Markus Wagner <markus.wagner@open-xchange.com>
First sprint increment for 7.4.0 release
* Mon Oct 07 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.1
* Tue Jul 16 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Tue Apr 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Mar 26 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.2.0
* Thu Jan 10 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.0.1
* Tue Dec 04 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.0.0 release
* Tue Nov 13 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for EDP drop #6
* Fri Oct 26 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 6.22.1
* Thu Oct 18 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 6.23.0
