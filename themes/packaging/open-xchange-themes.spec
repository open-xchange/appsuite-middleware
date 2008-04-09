
# norootforbuild

Name:           open-xchange-themes
BuildArch:	noarch
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-configread open-xchange-server
#%if 0%{?suse_version} <= 1010
# SLES10
#BuildRequires:  java-1_5_0-ibm-devel java-1_5_0-ibm-alsa update-alternatives
#%endif
#%if 0%{?suse_version} >= 1020
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
#%endif
Version:        6.5.0
Release:        1
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Themes
Requires:       open-xchange-common open-xchange-global open-xchange-configread open-xchange-server
#

%description
Open-Xchange Themes

Authors:
--------
    Open Xchange

%prep
%setup -q

%build


%install

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/themes.properties
