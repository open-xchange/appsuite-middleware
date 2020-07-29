%define __jar_repack %{nil}

Name:           open-xchange-file-storage-webdav
BuildArch:      noarch
BuildRequires:  ant
BuildRequires:  open-xchange-core
BuildRequires:  java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 3
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend webdav file storage extension
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@
Provides:       open-xchange-file-storage-webdav = %{version}

%description
Adds webdav file storage services like ownCloud to the backend installation.

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
* Tue Jul 28 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview of 7.10.4 release
* Tue Jan 07 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
first candidate for 7.10.4
