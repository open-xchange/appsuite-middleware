%define __jar_repack %{nil}

Name:          open-xchange-mail-categories
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-core >= @OXVERSION@
BuildRequires: open-xchange-mailfilter >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 30
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange Mail Categories Plugin
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-mailfilter >= @OXVERSION@

%description
This package offers the possibility to manage system and user categories for mails.

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
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/mail-categories.properties

%changelog
* Mon Apr 06 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-04-14 (5675)
* Mon Mar 16 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-03-23 (5651)
* Tue Feb 25 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-03-02 (5621)
* Mon Feb 03 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-02-10 (5570)
* Mon Jan 13 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-01-20 (5545)
* Mon Dec 02 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-12-09 (5508)
* Mon Nov 18 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-11-25 (5483)
* Wed Oct 16 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-12-02 (5454)
* Tue Oct 08 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-10-14 (5438)
* Mon Sep 23 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-09-30 (5419)
* Mon Sep 02 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-09-09 (5396)
* Mon Aug 19 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-08-26 (5373)
* Fri Aug 09 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-08-12 (5358)
* Tue Jul 23 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-07-29 (5340)
* Tue Jul 09 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-07-15 (5309)
* Mon Jun 24 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-07-01 (5290)
* Thu Jun 06 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-06-11 (5274)
* Mon May 13 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-05-14 (5247)
* Mon May 06 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-05-13 (5235)
* Wed Apr 24 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-04-29 (5211)
* Tue Mar 26 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-04-01 (5180)
* Tue Mar 12 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-03-11 (5149)
* Thu Feb 21 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-02-25 (5133)
* Thu Feb 07 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-02-11 (5108)
* Tue Jan 29 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-01-31 (5103)
* Mon Jan 21 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-01-28 (5076)
* Tue Jan 08 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2019-01-14 (5023)
* Fri Nov 23 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
RC 1 for 7.10.1 release
* Fri Nov 02 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview for 7.10.1 release
* Thu Oct 11 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.1 release
* Thu Sep 06 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.1 release
* Fri Jun 29 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.0 release
* Fri May 19 2017 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.8.2 release
* Tue Mar 15 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial release
