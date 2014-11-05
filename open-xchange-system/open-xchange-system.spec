
Name:          open-xchange-system
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       system integration specific infrastructure
Autoreqprov:   no
PreReq:        /usr/sbin/useradd

%description
system integration specific infrastructure

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
mkdir -p %{buildroot}/opt/open-xchange/lib

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post

%clean
%{__rm} -rf %{buildroot}

%pre
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%files
%defattr(-,root,root)
/opt/open-xchange/lib/oxfunctions.sh

%changelog
* Wed Nov 05 2014 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.8.0 release
* Fri Oct 31 2014 Carsten Hoeger <choeger@open-xchange.com>
First candidate for 7.6.2 release
* Mon Oct 27 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 17 2014 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Carsten Hoeger <choeger@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Carsten Hoeger <choeger@open-xchange.com>
Fourth candidate for 7.6.1 release
* Thu Oct 02 2014 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.6.1
* Wed Sep 17 2014 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.6.1
* Fri Sep 05 2014 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.6.1
* Fri Aug 29 2014 Carsten Hoeger <choeger@open-xchange.com>
Initial release
