
Name:          open-xchange-system
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 21
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       system integration specific infrastructure
Autoreqprov:   no
PreReq:        /usr/sbin/useradd

%description
system integration specific infrastructure

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
mkdir -p %{buildroot}/opt/open-xchange/lib

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post

%clean
%{__rm} -rf %{buildroot}

%pre
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%files
%defattr(-,root,root)
/opt/open-xchange/lib/oxfunctions.sh

%changelog
* Mon Jun 29 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 10 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Thu Apr 30 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Fri Apr 24 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Wed Apr 08 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Fri Mar 13 2015 Carsten Hoeger <choeger@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Carsten Hoeger <choeger@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Carsten Hoeger <choeger@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Carsten Hoeger <choeger@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Carsten Hoeger <choeger@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-02-25
* Wed Feb 11 2015 Carsten Hoeger <choeger@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Carsten Hoeger <choeger@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Carsten Hoeger <choeger@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-01-26
* Mon Jan 12 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2015-01-12
* Fri Dec 12 2014 Carsten Hoeger <choeger@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Carsten Hoeger <choeger@open-xchange.com>
Third candidate for 7.6.2 release
* Tue Dec 02 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Carsten Hoeger <choeger@open-xchange.com>
Second candidate for 7.6.2 release
* Tue Nov 18 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-11-17
* Fri Oct 31 2014 Carsten Hoeger <choeger@open-xchange.com>
First candidate for 7.6.2 release
* Mon Oct 27 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Carsten Hoeger <choeger@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Carsten Hoeger <choeger@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.6.1
* Wed Sep 17 2014 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.6.1
* Fri Aug 29 2014 Carsten Hoeger <choeger@open-xchange.com>
Initial release
