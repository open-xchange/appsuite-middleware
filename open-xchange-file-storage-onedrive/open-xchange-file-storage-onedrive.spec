
Name:           open-xchange-file-storage-onedrive
BuildArch:      noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires:  ant
%else
BuildRequires:  ant-nodeps
%endif
BuildRequires:  open-xchange-core
BuildRequires:  open-xchange-oauth
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend Microsoft OneDrive file storage extension
Autoreqprov:   no
Requires:       open-xchange-core >= @OXVERSION@
Requires:       open-xchange-oauth >= @OXVERSION@
Provides:       open-xchange-file-storage-onedrive = %{version}

%description
Adds Microsoft OneDrive file storage service to the backend installation.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DjavaVersion=1.7 -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build


%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
* Wed Nov 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.0 release
* Wed Nov 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.0 release
* Fri Oct 31 2014 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.6.2 release
* Thu Oct 30 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.6.2 release
* Mon Oct 27 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Thorben Betten <thorben.betten@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Thorben Betten <thorben.betten@open-xchange.com>
Third release candidate for 7.6.1
* Wed Sep 17 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.6.1
* Wed Aug 20 2014 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.6.1
