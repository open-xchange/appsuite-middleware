
Name:          open-xchange-push-mailnotify
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 1
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Mail Push Bundle
Requires:      open-xchange-core >= @OXVERSION@

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

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc push_mailnotify.properties


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
* Tue Sep 04 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.0
* Thu Jul 12 2012 Carsten Hoeger <choeger@open-xchange.com>
Initial release
