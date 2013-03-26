
Name:           open-xchange-file-storage-cmis
BuildArch:      noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 1
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend CMIS file storage extension
Requires:       open-xchange-core >= @OXVERSION@
Provides:       open-xchange-file-storage-cmis = %{version}

%description
Adds CMIS file storage service to the backend installation.

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
* Tue Mar 26 2013 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.2.0
* Thu Jan 10 2013 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.0.1
* Tue Dec 04 2012 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.0.0 release
* Tue Nov 13 2012 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for EDP drop #6
* Fri Oct 26 2012 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 6.22.1
* Wed Oct 17 2012 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 6.23.0
