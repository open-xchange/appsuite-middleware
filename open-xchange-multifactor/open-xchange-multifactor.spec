%define __jar_repack %{nil}
%define manlist manfiles.list

Name:          open-xchange-multifactor
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: pandoc >= 2.0.0
Version:       @OXVERSION@
%define        ox_release 21
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Multi-factor authentication
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This packages contains OSGi bundles required for multifactor authentication.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build
rm -f %{manlist} && touch %{manlist}
test -d %{buildroot}%{_mandir} && find %{buildroot}%{_mandir}/man1 -type f -printf "%%%doc %p.*\n" >> %{manlist}
sed -i -e 's;%{buildroot};;' %{manlist}

%clean
%{__rm} -rf %{buildroot}

%files -f %{manlist}
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/deletemultifactordevice
/opt/open-xchange/sbin/listmultifactordevice
/opt/open-xchange/lib/com.openexchange.multifactor.clt.jar

%changelog
* Tue Aug 18 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-08-24 (5847)
* Tue Aug 04 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-08-10 (5833)
* Tue Jul 21 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-07-27 (5821)
* Wed Jul 15 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-07-17 (5819)
* Thu Jul 09 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-07-13 (5804)
* Fri Jun 26 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-07-02 (5792)
* Wed Jun 24 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-06-30 (5781)
* Mon Jun 15 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-06-15 (5765)
* Fri May 15 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-05-26 (5742)
* Mon May 04 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-05-11 (5720)
* Thu Apr 23 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-04-30 (5702)
* Fri Apr 17 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-04-02 (5692)
* Mon Apr 06 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-04-14 (5677)
* Thu Mar 19 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-03-23 (5653)
* Fri Feb 28 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First preview for 7.10.2 release
* Thu Mar 07 2019 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Initial release
