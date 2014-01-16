
Name:          open-xchange-munin-scripts-jolokia
BuildArch:	   noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:	   @OXVERSION@
%define        ox_release 15
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GNU General Public License (GPL)
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Munin scripts
Requires:      munin-node
Conflicts:     open-xchange-munin-scripts

%description
Munin is a highly flexible and powerful solution used to create graphs of
virtually everything imaginable throughout your network, while still
maintaining a rattling ease of installation and configuration.

This package contains Open-Xchange plugins for the Munin node.

Munin is written in Perl, and relies heavily on Tobi Oetiker's excellent
RRDtool. To see a real example of Munin in action, you can follow a link
from <http://munin.projects.linpro.no/> to a live installation.

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
TMPFILE=`mktemp /tmp/munin-node.configure.XXXXXXXXXX`
munin-node-configure --libdir /usr/share/munin/plugins/ --shell > $TMPFILE || :
if [ -f $TMPFILE ] ; then
  sh < $TMPFILE
  rm -f $TMPFILE
fi
/etc/init.d/munin-node restart || :
exit 0


%clean
%{__rm} -rf %{buildroot}


%files
%defattr(-,root,root)
%dir /usr/share/munin
/usr/share/munin/plugins/
%dir /etc/munin/
%dir /etc/munin/plugin-conf.d/
%config(noreplace) /etc/munin/plugin-conf.d/*

%changelog
* Thu Jan 16 2014 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 03 2014 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-09
* Thu Dec 19 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-23
* Tue Dec 17 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-18
* Thu Dec 12 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Felix Marx <felix.marx@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Felix Marx <felix.marx@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Felix Marx <felix.marx@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 10 2013 Felix Marx <felix.marx@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2013-10-07
* Tue Sep 24 2013 Felix Marx <felix.marx@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Felix Marx <felix.marx@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Felix Marx <felix.marx@open-xchange.com>
Ninth candidate for 7.4.0 release
* Mon Sep 02 2013 Felix Marx <felix.marx@open-xchange.com>
Eighth candidate for 7.4.0 release
* Tue Aug 27 2013 Felix Marx <felix.marx@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Felix Marx <felix.marx@open-xchange.com>
Sixth candidate for 7.4.0 release
* Mon Aug 19 2013 Felix Marx <felix.marx@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Felix Marx <felix.marx@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Felix Marx <felix.marx@open-xchange.com>
Third release candidate for 7.4.0
* Fri Aug 02 2013 Felix Marx <felix.marx@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Felix Marx <felix.marx@open-xchange.com>
First release candidate for 7.4.0
* Mon Apr 15 2013 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.4.0
