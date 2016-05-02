%define __jar_repack %{nil}

Name:          open-xchange-sms-sipgate
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:       @OXVERSION@
%define        ox_release 11
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Send SMS messages via sipgate
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed to send SMS messages via sipgate.

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
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Mon May 02 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-05-09 (3272)
* Mon Apr 25 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-04-25 (3263)
* Fri Apr 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-04-25 (3239)
* Thu Apr 07 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-04-07 (3228)
* Wed Mar 30 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.8.1 release
* Thu Jan 14 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial release
