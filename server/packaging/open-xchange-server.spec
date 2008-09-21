
# norootforbuild
%define		configfiles	configfiles.list

Name:           open-xchange-server
BuildArch:	noarch
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-configread open-xchange-monitoring open-xchange-cache
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
BuildRequires:  java-1_5_0-ibm java-1_5_0-ibm-devel java-1_5_0-ibm-alsa update-alternatives
%endif
%if %{?suse_version} >= 1100
BuildRequires:  java-sdk-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
BuildRequires:  java-sdk-1.5.0-sun
%endif
%endif
%if 0%{?rhel_version}
# libgcj seems to be installed whether we want or not and libgcj needs cairo
BuildRequires:  java-sdk-1.5.0-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%if %{?fedora_version} <= 8
BuildRequires:  java-devel-icedtea saxon
%endif
%endif
Version:	6.6.0
Release:	9
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Server Bundle
Requires:       open-xchange-global open-xchange-configread open-xchange-global open-xchange-monitoring open-xchange-management open-xchange-cache
%if 0%{?suse_version}
%if %{?suse_version} <= 1010
# SLES10
Requires:  java-1_5_0-ibm update-alternatives
%endif
%if %{?suse_version} >= 1100
BuildRequires:  java-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
Requires:  java-1_5_0-sun
%endif
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
Requires:  java-1.6.0-openjdk
%endif
%if %{?fedora_version} <= 8
Requires:  java-icedtea
%endif
%endif
%if 0%{?rhel_version}
Requires:  java-1.5.0-sun
%endif
#

%package -n	open-xchange
Group:          Applications/Productivity
Summary:	Open-Xchange server scripts and configuration
Prereq:		/usr/sbin/useradd
Requires:	open-xchange-authentication open-xchange-cache open-xchange-charset open-xchange-common open-xchange-configjump open-xchange-configread open-xchange-global open-xchange-i18n open-xchange-mailstore open-xchange-jcharset open-xchange-management open-xchange-monitoring open-xchange-push-udp open-xchange-server open-xchange-sessiond open-xchange-smtp open-xchange-spamhandler, mysql >= 5.0.0


%description -n open-xchange
Open-Xchange server scripts and configuration

Authors:
--------
    Open-Xchange

%description
The Open-Xchange Server Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
mkdir -p %{buildroot}/sbin
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%if 0%{?suse_version} == 1030
# without setting this option, build fails on openSUSE10.3 in obs
export ANT_OPTS=-Xmx80m
%endif

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Ddistribution=lsb install

mkdir -p %{buildroot}/var/log/open-xchange

# generate list of config files for config package
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc/groupware -maxdepth 1 -type f \
	-not -name oxfunctions.sh \
	-printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}

ln -sf ../etc/init.d/open-xchange-groupware %{buildroot}/sbin/rcopen-xchange-groupware

%clean
%{__rm} -rf %{buildroot}


%pre -n open-xchange
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :


%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/bundles/*

%files -n open-xchange -f %{configfiles}
%defattr(-,root,root)
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/etc/groupware/osgi
%dir /opt/open-xchange/sbin
/sbin/*
/opt/open-xchange/etc/groupware/osgi/config.ini.template
/opt/open-xchange/sbin/*
%dir %attr(750,open-xchange,open-xchange) /var/log/open-xchange
/etc/init.d/open-xchange-groupware
%dir /opt/open-xchange/etc/groupware/servletmappings
%dir /opt/open-xchange/etc/groupware
/opt/open-xchange/etc/groupware/servletmappings/*
%changelog
