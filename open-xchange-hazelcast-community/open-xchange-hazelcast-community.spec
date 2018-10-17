%define __jar_repack %{nil}

Name:          open-xchange-hazelcast-community
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-osgi >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 1
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Community implementation of open-xchange-hazelcast
Autoreqprov:   no
Requires:      open-xchange-osgi >= @OXVERSION@
Provides:      open-xchange-hazelcast
Conflicts:     open-xchange-hazelcast-enterprise
Conflicts:     open-xchange-core < 7.10.1

%description
This package installs the community version of Hazelcast. The implementation uses the freely available version of Hazelcast with a
limited set of features (e. g. encrypted transport is missing).
This Hazelcast module is mutually exclusive with the enterprise version open-xchange-hazelcast-enterprise

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
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/bundles/com.hazelcast
/opt/open-xchange/bundles/com.hazelcast/*
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/com.hazelcast.ini

%changelog
* Thu Oct 11 2018 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.1 release
* Mon Aug 13 2018 Martin Schneider <martin.schneider@open-xchange.com>
Initial release
