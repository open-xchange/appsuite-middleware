
Name:           open-xchange-spamhandler-cloudmark
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
BuildRequires:  java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 3
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange Cloudmark Spamhandler
Requires:       open-xchange-core
Provides:	open-xchange-spamhandler
Conflicts:      open-xchange-spamhandler-default open-xchange-spamhandler-spamassassin

%description
The Open-Xchange Cloudmark Spamhandler can be used in a generic way since it can just
report Spam and/or Ham messages to any configured EMail address.

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
if [ ${1:-0} -eq 2 ]; then
  . /opt/open-xchange/lib/oxfunctions.sh

  # SoftwareChange_Request-1452
  pfile=/opt/open-xchange/etc/spamhandler_cloudmark.properties
  if ! ox_exists_property com.openexchange.spamhandler.cloudmark.targetHamEmailAddress $pfile; then
     ox_set_property com.openexchange.spamhandler.cloudmark.targetHamEmailAddress "" $pfile
  fi
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*
%doc com.openexchange.spamhandler.cloudmark/ChangeLog

%changelog
* Mon Jun 17 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-06-13
* Fri Jun 07 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Release build for EDP drop #2
* Mon Jul 02 2012 Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
Initial release
