
# norootforbuild

Name:           open-xchange-munin-scripts
BuildArch:	noarch
Version:	0.1
Release:	1
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Munin scripts
Requires:	open-xchange-common perl munin-node
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
%__cp ox_munin_scripts/* $RPM_BUILD_ROOT/usr/share/munin/plugins/


%post
TMPFILE=`mktemp /tmp/munin-node.configure.XXXXXXXXXX`
munin-node-configure --shell > $TMPFILE || rm -f $TMPFILE
if [ -f $TMPFILE ] ; then
  sh < $TMPFILE
fi
rm -f $TMPFILE
exit 0


%clean
%{__rm} -rf %{buildroot}


%files
%defattr(-,root,root)
%dir /usr/share/munin
/usr/share/munin/plugins/


%changelog
* Fri Oct 08 2010 - holger.achtziger@open-xchange.com
  - suggest aus configdb scripten entfernt
* Wed Oct 06 2010 - holger.achtziger@open-xchange.com
  - Initial Version
