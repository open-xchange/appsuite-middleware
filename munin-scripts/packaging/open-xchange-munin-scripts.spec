
# norootforbuild

Name:           open-xchange-munin-scripts
BuildArch:	noarch
Version:	0.1
Release:	6
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Munin scripts
Requires:	open-xchange-common 
PreReq:         munin-node
#

%description
Munin is a highly flexible and powerful solution used to create graphs of
virtually everything imaginable throughout your network, while still
maintaining a rattling ease of installation and configuration.

This package contains Open-Xchange plugins for the Munin node.

Munin is written in Perl, and relies heavily on Tobi Oetiker's excellent
RRDtool. To see a real example of Munin in action, you can follow a link
from <http://munin.projects.linpro.no/> to a live installation.


%prep
%setup -q

%build


%install
%__mkdir_p %{buildroot}/usr/share/munin/plugins/
%__mkdir_p %{buildroot}/etc/munin/plugin-conf.d/
%__cp ox_munin_scripts/* $RPM_BUILD_ROOT/usr/share/munin/plugins/
%__cp plugin-conf.d/* $RPM_BUILD_ROOT/etc/munin/plugin-conf.d/
chmod a+x $RPM_BUILD_ROOT/usr/share/munin/plugins/*

%post
TMPFILE=`mktemp /tmp/munin-node.configure.XXXXXXXXXX`
munin-node-configure --libdir /usr/share/munin/plugins/ --shell > $TMPFILE || true 
if [ -f $TMPFILE ] ; then
  sh < $TMPFILE
fi
rm -f $TMPFILE
/etc/init.d/munin-node restart
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
