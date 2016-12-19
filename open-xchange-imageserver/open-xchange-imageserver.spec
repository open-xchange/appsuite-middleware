
Name:          open-xchange-imageserver
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-grizzly
BuildRequires: open-xchange-admin
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
BuildRequires: systemd-rpm-macros
%endif
Version:       @OXVERSION@
%define        ox_release 12
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Source1:       open-xchange-imageserver.init
Source2:       open-xchange-imageserver.service
Summary:       The Open-Xchange backend webservice for ImageServer
AutoReqProv:   no
Requires:      open-xchange-grizzly
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-imageserver = %{version}

%description
This package contains the backend components for the imageserver web service

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build
mkdir -p %{buildroot}/var/log/open-xchange/imageserver
mkdir -p %{buildroot}/var/spool/open-xchange/imageserver
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
%__install -D -m 444 %{SOURCE2} %{buildroot}/usr/lib/systemd/system/open-xchange-imageserver.service
%else
mkdir -p %{buildroot}/etc/init.d
install -m 755 %{SOURCE1} %{buildroot}/etc/init.d/open-xchange-imageserver
%endif

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/imageserver/bundles/
/opt/open-xchange/imageserver/bundles/*
%dir %attr(750, open-xchange, root) /opt/open-xchange/imageserver/osgi/
/opt/open-xchange/imageserver/osgi/config.ini.template
%dir /opt/open-xchange/imageserver/osgi/bundle.d/
/opt/open-xchange/imageserver/osgi/bundle.d/*
%dir /opt/open-xchange/imageserver/etc/
%config(noreplace) /opt/open-xchange/imageserver/etc/*
%dir /opt/open-xchange/imageserver/sbin/
/opt/open-xchange/imageserver/sbin/open-xchange-imageserver
%dir %attr(750, open-xchange, root) /var/log/open-xchange/imageserver
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/imageserver
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
/usr/lib/systemd/system/open-xchange-imageserver.service
%else
/etc/init.d/open-xchange-imageserver
%endif

%changelog
* Wed Dec 14 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Build for patch 2016-12-19 (3814)
* Tue Dec 13 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Build for patch 2016-12-14 (3806)
* Tue Dec 06 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Build for patch 2016-12-12 (3775)
* Fri Nov 25 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
First release candidate for 7.8.3 release
* Tue Nov 15 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
First preview 7.8.3 release
* Thu Jul 28 2016 Kai Ahrens <kai.ahrens@open-xchange.com>
initial packaging for open-xchange-imageserver
