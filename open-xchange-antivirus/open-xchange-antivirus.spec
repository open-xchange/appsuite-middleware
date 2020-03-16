%define __jar_repack %{nil}

Name:          open-xchange-antivirus
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Anti-Virus
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package contains the ICAP client and the Anti-Virus service implementation

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Mon Jun 17 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
First preview for 7.10.2 release
* Tue Dec 11 2018 Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
Initial packaging for the Anti-Virus service
