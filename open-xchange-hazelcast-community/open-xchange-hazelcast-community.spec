%define __jar_repack %{nil}

Name:          open-xchange-hazelcast-community
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
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
cp -rv --preserve=all ./opt %{buildroot}/

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
* Mon Jun 17 2019 Martin Schneider <martin.schneider@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Martin Schneider <martin.schneider@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Martin Schneider <martin.schneider@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Martin Schneider <martin.schneider@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Martin Schneider <martin.schneider@open-xchange.com>
prepare for 7.10.2 release
* Thu Oct 11 2018 Martin Schneider <martin.schneider@open-xchange.com>
First candidate for 7.10.1 release
* Mon Aug 13 2018 Martin Schneider <martin.schneider@open-xchange.com>
Initial release
