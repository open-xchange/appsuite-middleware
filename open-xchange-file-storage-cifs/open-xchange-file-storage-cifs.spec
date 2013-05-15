
Name:           open-xchange-file-storage-cifs
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
Summary:        The Open Xchange backend CIFS file storage extension
Requires:       open-xchange-core >= @OXVERSION@
Provides:       open-xchange-file-storage-cifs = %{version}

%description
Adds CIFS file storage service to the backend installation.

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
* Wed May 15 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Thorben Betten <thorben.betten@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Thorben Betten <thorben.betten@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Thorben Betten <thorben.betten@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Thorben Betten <thorben.betten@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 19 2013 Thorben Betten <thorben.betten@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Thorben Betten <thorben.betten@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.0.2 release
* Thu Feb 14 2013 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.0.1
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
