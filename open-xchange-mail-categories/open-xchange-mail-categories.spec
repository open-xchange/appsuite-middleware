%define __jar_repack %{nil}

Name:          open-xchange-mail-categories
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
BuildRequires: open-xchange-core >= @OXVERSION@
BuildRequires: open-xchange-mailfilter >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 12
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange Mail Categories Plugin
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-mailfilter >= @OXVERSION@

%description
This package offers the possibility to manage system and user categories for mails.

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
%config(noreplace) /opt/open-xchange/etc/mail-categories.properties

%changelog
* Fri Sep 30 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-10-10 (3597)
* Mon Sep 19 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-09-26 (3572)
* Mon Sep 19 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-09-08 (3580)
* Mon Sep 05 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-09-12 (3547)
* Mon Aug 22 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-08-29 (3522)
* Mon Aug 15 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-08-26 (3512)
* Mon Aug 08 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-08-15 (3490)
* Fri Jul 22 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2016-08-01 (3467)
* Tue Jul 12 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.8.2 release
* Tue Mar 15 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial release
