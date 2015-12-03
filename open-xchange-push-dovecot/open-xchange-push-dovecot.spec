
Name:          open-xchange-push-dovecot
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-imap
BuildRequires: open-xchange-rest
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 10
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Dovecot Push Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@, open-xchange-imap >= @OXVERSION@, open-xchange-rest >= @OXVERSION@

%description
Open-Xchange Mail Push Bundle

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
%dir /opt/open-xchange/etc/hazelcast/
%config(noreplace) /opt/open-xchange/etc/hazelcast/*

%changelog
* Thu Dec 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-12-07 (2916)
* Thu Dec 03 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-12-02 (2930)
* Tue Nov 17 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-11-23 (2883)
* Wed Nov 11 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-11-16 (2862)
* Fri Nov 06 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-11-09 (2841)
* Fri Oct 30 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-11-03 (2851)
* Wed Oct 28 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-11-03 (2827)
* Tue Oct 20 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Wed Sep 30 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Tue Sep 29 2015 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
