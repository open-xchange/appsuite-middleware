%define __jar_repack %{nil}

Name:          open-xchange-system
BuildArch:     noarch
BuildRequires: coreutils
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       System integration specific infrastructure
Autoreqprov:   no
PreReq:        /usr/sbin/useradd
%if 0%{?suse_version} && 0%{?suse_version} <= 1210
Requires:      util-linux
%else
Requires:      which
%endif
Requires:      sed
Requires:      coreutils
Requires:      perl
Conflicts:     open-xchange-core < 7.10.0
%description
System integration specific infrastructure

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
# for %ghost file
mkdir -p %{buildroot}/opt/open-xchange/etc
touch %{buildroot}/opt/open-xchange/etc/scr_db
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%pre
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%files
%defattr(-,root,root)
/opt/open-xchange/
%ghost /opt/open-xchange/etc/scr_db

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
