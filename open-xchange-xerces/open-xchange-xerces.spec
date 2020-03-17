%define __jar_repack %{nil}

Name:          open-xchange-xerces
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Xerces Compat for Sun Java
Autoreqprov:   no
Requires:      open-xchange-osgi >= 7.10.1
Obsoletes:     open-xchange-xerces-ibm
Obsoletes:     open-xchange-xerces-sun

%description
Xerces compatibility for OX installations on Sun JVM.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
