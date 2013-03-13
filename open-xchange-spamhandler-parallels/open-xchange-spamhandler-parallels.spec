
Name:           open-xchange-spamhandler-parallels
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-spamhandler-spamassassin >= @OXVERSION@
BuildRequires:  open-xchange-parallels >= @OXVERSION@
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 6
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Spam handler for Parallels extensions
Requires:       open-xchange-parallels >= @OXVERSION@
Requires:       open-xchange-spamhandler-spamassassin >= @OXVERSION@

%description
This package contains the spam handler implementation for Parallels extension. This spam handler extends the spam handler for SpamAssassin
by configuring user specific host names, ports and user names.
Not necessarily SpamAssassin will be used with Parallels extensions. Therefore the spam handler for Parallels extension is located in its
own package to be able to replace it with every other possible spam handler implementation.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*

%changelog
* Tue Mar 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Tue Jan 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.1
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Tue Oct 23 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.23.0
