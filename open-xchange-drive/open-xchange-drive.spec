
Name:           open-xchange-drive
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 6
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module for Open-Xchange Drive file synchronization
Requires:      open-xchange-core >= @OXVERSION@

%description
Server module for Open-Xchange Drive file synchronization

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
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/drive.properties
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Fri Aug 23 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth candidate for 7.4.0 release
* Mon Aug 19 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 7.4.0
* Fri Aug 02 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.4.0
* Mon Apr 09 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Initial release
