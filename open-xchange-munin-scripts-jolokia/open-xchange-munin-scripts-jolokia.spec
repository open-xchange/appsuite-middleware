
Name:          open-xchange-munin-scripts-jolokia
BuildArch:	   noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
Version:	   @OXVERSION@
%define        ox_release 7
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GNU General Public License (GPL)
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Munin scripts
Requires:	   open-xchange-core >= @OXVERSION@
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
