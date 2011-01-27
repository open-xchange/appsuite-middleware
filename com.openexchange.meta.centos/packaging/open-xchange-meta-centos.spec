
# norootforbuild
Name:           open-xchange-meta-centos
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  java-1.6.0-openjdk-devel
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:	The Open-Xchange Meta package to install OX on CentOS
%if 0%{?centos_version}
Requires:	java-1.6.0-openjdk
Provides:	java-sun
%endif

%description
The Open-Xchange Meta package to install OX on CentOS

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

%clean
%{__rm} -rf %{buildroot}


%files
%defattr(-,root,root)
%doc README.TXT

%changelog
