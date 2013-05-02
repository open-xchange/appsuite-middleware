
Name:          open-xchange-configjump-generic
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 11
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange generic ConfigJump
Requires:      open-xchange-core >= @OXVERSION@

%description
The Open-Xchange generic ConfigJump

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
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc configjump.properties

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
* Thu May 02 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Carsten Hoeger <choegeropen-xchange.com>
Build for public patch 2013-01-15
* Wed Oct 10 2012 Carsten Hoeger <choegeropen-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Carsten Hoeger <choegeropen-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choegeropen-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choegeropen-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Carsten Hoeger <choegeropen-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choegeropen-xchange.com>
prepare for 6.22.0
* Thu Jul 19 2012 Carsten Hoeger <choegeropen-xchange.com>
initial release
