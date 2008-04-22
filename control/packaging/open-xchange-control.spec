
# norootforbuild

Name:           open-xchange-control
BuildArch:	noarch
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-management
%if 0%{?suse_version} <= 1010
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
Release:        4
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The Open-Xchange Server Control Bundle
Requires:       open-xchange-common open-xchange-global open-xchange-management
#

%description
The Open-Xchange Server Control Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/etc/*/osgi/bundle.d/
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/*/osgi/bundle.d/*
/opt/open-xchange/sbin/*

