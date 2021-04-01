%define __jar_repack %{nil}

Name:           open-xchange-switchboard
BuildArch:      noarch
BuildRequires:  ant
BuildRequires:  open-xchange-core
BuildRequires:  java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 8
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Switchboard bundle.
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Switchboard bundle.

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
/opt/open-xchange/bundles/*
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Tue Mar 23 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
Build for patch 2021-03-29 (5976)
* Tue Mar 09 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
Build for patch 2021-03-15 (5973)
* Mon Feb 22 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
Build for patch 2021-02-22 (5961)
* Fri Feb 05 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
Third candidate for 7.10.5 release
* Mon Feb 01 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
Second candidate for 7.10.5 release
* Fri Jan 15 2021 Martin Herfurth <martin.herfurth@open-xchange.com>
First candidate for 7.10.5 release
* Thu Dec 17 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
Second preview of 7.10.5 release
* Fri Nov 27 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
First preview of 7.10.5 release
* Tue Oct 06 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
prepare for 7.10.5 release
* Wed Aug 05 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Martin Herfurth <martin.herfurth@open-xchange.com>
First candidate for 7.10.4 release
