
Name:          open-xchange-realtime-events-json
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-realtime-json
BuildRequires: open-xchange-realtime-events
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 7
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Realtime event pubsub implementations via a JSON channel
Autoreqprov:   no
Requires:      open-xchange-realtime-events >= @OXVERSION@
Requires:      open-xchange-realtime-json >= @OXVERSION@

%description


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
* Wed Feb 11 2015 Marc Arens <marc.arens@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Marc Arens <marc.arens@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Marc Arens <marc.arens@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-26
* Mon Jan 12 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2015-01-12
* Fri Dec 12 2014 Marc Arens <marc.arens@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Marc Arens <marc.arens@open-xchange.com>
Third candidate for 7.6.2 release
* Tue Dec 02 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Marc Arens <marc.arens@open-xchange.com>
Second candidate for 7.6.2 release
* Tue Nov 18 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-11-17
* Fri Oct 31 2014 Marc Arens <marc.arens@open-xchange.com>
First candidate for 7.6.2 release
* Mon Oct 27 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Marc Arens <marc.arens@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Marc Arens <marc.arens@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Marc Arens <marc.arens@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Marc Arens <marc.arens@open-xchange.com>
Third release candidate for 7.6.1
* Wed Sep 17 2014 Marc Arens <marc.arens@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Marc Arens <marc.arens@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Marc Arens <marc.arens@open-xchange.com>
First release candidate for 7.6.1
* Thu May 30 2013 Marc Arens <marc.arens@open-xchange.com>
Initial build
