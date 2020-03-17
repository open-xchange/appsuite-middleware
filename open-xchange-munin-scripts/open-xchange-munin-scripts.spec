%define __jar_repack %{nil}

Name:          open-xchange-munin-scripts
%define use_systemd (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >=1210)
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GNU General Public License (GPL)
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Munin scripts
Autoreqprov:   no
Requires:      munin-node, perl-JSON, perl-libwww-perl
Requires(pre): open-xchange-system
Provides:      open-xchange-munin-scripts-jolokia = %{version}
Obsoletes:     open-xchange-munin-scripts-jolokia < %{version}

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
cp -rv --preserve=all ./etc ./usr %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
GLOBIGNORE='*'

function contains() { grep $@ >/dev/null 2>&1; return $?; }

# only when updating
if [ ${1:-0} -eq 2 ]; then
  # SoftwareChange_request-3870
  PFILE=/etc/munin/plugin-conf.d/ox
  if test -f ${PFILE} && ! contains env.oxJolokiaUrl ${PFILE}; then
    sed -i '$ a env.oxJolokiaUrl http://localhost:8009/monitoring/jolokia' ${PFILE}
  fi

  if test -f ${PFILE} && ! contains env.oxJolokiaUser ${PFILE}; then
    sed -i '$ {
      a ### oxJolokiaUser must be the same as com.openexchange.jolokia.user inside jolokia.properties
      a env.oxJolokiaUser changeMe!Now
    }' ${PFILE}
  fi

  if test -f ${PFILE} && ! contains env.oxJolokiaPassword ${PFILE}; then
    sed -i '$ {  
      a ### oxJolokiaPassword must be the same as com.openexchange.jolokia.password inside jolokia.properties
      a env.oxJolokiaPassword s3cr3t!toBeChanged
    }' ${PFILE}
  fi
fi

TMPFILE=`mktemp /tmp/munin-node.configure.XXXXXXXXXX`
munin-node-configure --libdir /usr/share/munin/plugins/ --shell > $TMPFILE || :
if [ -f $TMPFILE ] ; then
  sh < $TMPFILE
  rm -f $TMPFILE
fi
find -L /etc/munin/plugins -name 'ox_*' -type l -delete

# The admin has to actively configure and start jolokia and munin
PFILE=/opt/open-xchange/etc/jolokia.properties
jolokia_enabled=$(ox_read_property com.openexchange.jolokia.start ${PFILE})
if [[ ! ${jolokia_enabled//[[:space:]]/} = true ]]
then
  echo -e "\n\e[31mWARNING\e[0m: You have to properly configure and activate jolokia and munin for working monitoring! \n"
fi

#no common service wrapper dependency across rpm distros
%if %{use_systemd}
systemctl try-restart munin-node >/dev/null 2>&1 || :
%else
/etc/init.d/munin-node restart || :
%endif
exit 0

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /usr/
%dir /usr/share/
%dir /usr/share/munin
/usr/share/munin/plugins/
%dir /etc/
%dir /etc/munin/
%dir /etc/munin/plugin-conf.d/
%config(noreplace) /etc/munin/plugin-conf.d/ox

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
