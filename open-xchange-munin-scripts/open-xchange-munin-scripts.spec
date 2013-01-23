
Name:           open-xchange-munin-scripts
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  java-devel >= 1.6.0
Version:	@OXVERSION@
%define         ox_release 5
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open-Xchange Munin scripts
Requires:	open-xchange-core >= @OXVERSION@
Requires:       munin-node

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
* Tue Sep 20 2011 - holger.achtziger@open-xchange.com
 - Fixed cache statistics
* Tue May 24 2011 - steffen.templin@open-xchange.com
 - Repaired autoconf function of most scripts.
 - Corrected wrong parameter for showruntimestats call in java heap scripts 
* Fri Apr 29 2011 - wolfgang.rosenauer@open-xchange.com
 - RPM %post script calls munin-node-configure with explicit libdir path
 - Improved RPM requirements
* Wed Jan 19 2011 - steffen.templin@open-xchange.com
 - Added munin plugin for all other threadpool stats.
* Mon Jan 17 2011 - steffen.templin@open-xchange.com
 - Added munin plugin for threadpool task stats.
* Tue Nov 23 2010 - marcus.klein@open-xchange.com
 - Bugfix #17525: Total number of database connections is monitored successfully again.
* Fri Nov 19 2010 - marcus.klein@open-xchange.com
 - Bugfix #17548: AJP requests are monitored successfully again.
* Fri Oct 08 2010 - holger.achtziger@open-xchange.com
 - initial version
