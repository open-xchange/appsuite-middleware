
# norootforbuild
%define         configfiles     configfiles.list

Name:           open-xchange-config-hosting
BuildArch:	noarch
BuildRequires:  ant
%if 0%{?suse_version} <= 1010 && ! 0%{?rhel_version}
# SLES10
BuildRequires:  java-1_5_0-ibm java-1_5_0-ibm-devel java-1_5_0-ibm-alsa update-alternatives
%endif
%if 0%{?suse_version} > 1010 || 0%{?rhel_version}
BuildRequires:  java-sdk-1.5.0-sun
%endif
%if 0%{?rhel_version}
# libgcj seems to be installed whether we want or not and libgcj needs cairo
BuildRequires:  cairo
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
Summary:        Open-Xchange server configuration for hosting systems
#

%description
Open-Xchange server configuration for hosting systems

Authors:
--------
    Open Xchange

%prep
%setup -q

%build


%install

ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange installConfig
# generate list of config files for config package
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc/groupware -maxdepth 1 -type f \
        -not -name oxfunctions.sh \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}

%clean
%{__rm} -rf %{buildroot}

%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/servletmappings
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/etc/groupware/osgi
/opt/open-xchange/etc/groupware/servletmappings/*
/opt/open-xchange/etc/groupware/osgi/*
