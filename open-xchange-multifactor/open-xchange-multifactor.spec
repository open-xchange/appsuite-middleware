%define __jar_repack %{nil}
%define manlist manfiles.list

Name:          open-xchange-multifactor
BuildArch:     noarch
BuildRequires: ant
BuildRequires: open-xchange-core
BuildRequires: java-1.8.0-openjdk-devel
BuildRequires: pandoc >= 2.0.0
Version:       @OXVERSION@
%define        ox_release 11
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
* Tue Oct 06 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-10-12 (5879)
* Wed Sep 23 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-09-29 (5869)
* Fri Sep 11 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-09-14 (5857)
* Mon Aug 24 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Build for patch 2020-08-24 (5842)
* Wed Aug 05 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
First preview of 7.10.4 release
* Thu Jan 16 2020 Benjamin Gruedelbach <benjamin.gruedelbach@open-xchange.com>
prepare for 7.10.4 release
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
