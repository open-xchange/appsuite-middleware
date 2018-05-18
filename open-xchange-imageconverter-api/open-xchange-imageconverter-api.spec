%define __jar_repack %{nil}

Name:          open-xchange-imageconverter-api
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core >= @OXVERSION@
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 6
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The ImageConverter API Bundle
Requires:      open-xchange-core >= @OXVERSION@

%description
The ImageConverter API Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Fri May 18 2018 Kai Ahrens <kai.ahrens@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Kai Ahrens <kai.ahrens@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Kai Ahrens <kai.ahrens@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Kai Ahrens <kai.ahrens@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Kai Ahrens <kai.ahrens@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Kai Ahrens <kai.ahrens@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Kai Ahrens <kai.ahrens@open-xchange.com>
prepare for 7.10.0 release
* Wed Aug 09 2017 Kai Ahrens <kai.ahrens@open-xchange.com>
Initial release
