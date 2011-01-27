
# norootforbuild
Name:           open-xchange-meta-centos
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  java-1.6.0-openjdk-devel
Version:	@OXVERSION@
%define		ox_release 1
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:	The Open-Xchange Meta package to install OX on CentOS
Provides:	java-sun

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
