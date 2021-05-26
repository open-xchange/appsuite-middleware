%define __jar_repack %{nil}

Name:           open-xchange-file-storage-webdav
BuildArch:      noarch
BuildRequires:  ant
BuildRequires:  open-xchange-core
BuildRequires:  java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 13
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
* Wed May 26 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-06-01 (6000)
* Fri May 21 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-05-21 (5997)
* Tue May 18 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-05-17 (5994)
* Mon Apr 26 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-05-03 (5989)
* Tue Apr 13 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-04-19 (5982)
* Tue Mar 23 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-03-29 (5976)
* Tue Mar 09 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-03-15 (5973)
* Mon Feb 22 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-02-22 (5961)
* Fri Feb 05 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third candidate for 7.10.5 release
* Mon Feb 01 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.10.5 release
* Fri Jan 15 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.5 release
* Thu Dec 17 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview of 7.10.5 release
* Fri Nov 27 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview of 7.10.5 release
* Tue Oct 06 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.5 release
* Wed Aug 05 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview of 7.10.4 release
* Tue Jan 07 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
first candidate for 7.10.4
