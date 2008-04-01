
# norootforbuild

Name:           open-xchange-admin
BuildArch:	noarch
BuildRequires:  ant open-xchange-common open-xchange-server
%if 0%{?suse_version}
BuildRequires:  java-1_5_0-sun-devel
%endif
%if 0%{?rhel_version}
# libgcj seems to be installed whether we want or not and libgcj needs cairo
BuildRequires:  cairo java-1.5.0-sun-devel
%endif
%if 0%{?fedora_version}
BuildRequires:  java-devel-icedtea
%endif
Version:        6.5.0
Release:        1
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Admin Daemon
Requires:       open-xchange-common open-xchange-server
%if 0%{?suse_version}
Requires:  mysql-client >= 5.0.0
%if 0%{?suse_version} <= 1010
# SLES10
Requires:  java-1_5_0-ibm java-1_5_0-ibm-alsa update-alternatives
%endif
%if 0%{?suse_version} >= 1020
Requires:  java-1_5_0-sun
%endif
%endif
%if 0%{?fedora_version}
Requires:  jre-icedtea, mysql >= 5.0.0
%endif
#

%package -n	open-xchange-admin-client
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Daemon RMI client library


%description -n open-xchange-admin-client
The Open Xchange Admin Daemon RMI client library

Authors:
--------
    Open-Xchange


%description
Open Xchange Admin Daemon containing commandline tools and provisioning
interface to manage users, groups, resources and Open Xchange database and
storage related setup information.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
mkdir -p %{buildroot}/sbin

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb install
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb install-client

ln -sf ../etc/init.d/open-xchange-admin %{buildroot}/sbin/rcopen-xchange-admin


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/admindaemon
%dir /opt/open-xchange/etc/admindaemon/osgi
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/log4j
/opt/open-xchange/sbin/*
/etc/init.d/*
/opt/open-xchange/bundles/*
/sbin/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/*.properties
%config(noreplace) /opt/open-xchange/etc/admindaemon/*.ccf
%config(noreplace) /opt/open-xchange/etc/admindaemon/mpasswd
%config(noreplace) /opt/open-xchange/etc/admindaemon/log4j/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/ox-admin-scriptconf.sh
/opt/open-xchange/etc/admindaemon/mysql
/opt/open-xchange/etc/admindaemon/osgi/config.ini.template
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*


%files -n open-xchange-admin-client
%defattr(-,root,root)
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
