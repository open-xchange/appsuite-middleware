
Name:          open-xchange-push-dovecot
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-imap
BuildRequires: open-xchange-rest
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 47
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange Dovecot Push Bundle
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@, open-xchange-imap >= @OXVERSION@, open-xchange-rest >= @OXVERSION@

%description
Open-Xchange Mail Push Bundle

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
%config(noreplace) /opt/open-xchange/etc/*
%dir /opt/open-xchange/etc/hazelcast/
%config(noreplace) /opt/open-xchange/etc/hazelcast/*

%changelog
* Mon Mar 11 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-03-12 (5165)
* Fri Feb 01 2019 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2019-02-11 (5104)
* Mon Nov 12 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-11-19 (4895)
* Wed Aug 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-30 (4876)
* Tue Aug 14 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-20 (4860)
* Thu Aug 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-08-08 (4856)
* Thu Jun 21 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-06-25 (4789)
* Fri May 11 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-05-04 (4695)
* Fri Apr 20 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-04-23 (4667)
* Tue Jan 30 2018 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2018-02-05 (4552)
* Fri Dec 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-12-11 (4470)
* Thu Nov 16 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-11-30 (4438)
* Mon Oct 23 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-10-30 (4423)
* Fri Oct 13 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for Patch 2017-10-16 (4391)
* Mon Aug 14 2017 Thorben Betten <thorben.betten@open-xchange.com>
2017-08-21 (4315)
* Wed Aug 02 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-08-01 (4308)
* Mon Jul 03 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-07-10 (4254)
* Mon May 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-05-15 (4133)
* Tue Apr 18 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-21 (4079)
* Fri Mar 31 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-04-03 (4047)
* Fri Feb 24 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-24 (3991)
* Wed Feb 08 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-02-20 (3949)
* Thu Jan 26 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-26 (3922)
* Thu Jan 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-23 (3875)
* Tue Jan 03 2017 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2017-01-06 (3833)
* Fri Nov 11 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-21 (3728)
* Fri Nov 04 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-11-10 (3712)
* Thu Oct 13 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-10-24 (3627)
* Tue Sep 20 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-26 (3569)
* Thu Sep 01 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-09-07 (3527)
* Fri Aug 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-29 (3519)
* Thu Jul 21 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-08-01 (3464)
* Thu Jun 30 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-07-04 (3358)
* Wed Jun 01 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-06-06 (3315)
* Tue May 03 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-05-09 (3270)
* Tue Apr 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-04-25 (3237)
* Mon Mar 21 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-03-29 (3187)
* Tue Mar 08 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-03-14 (3147)
* Mon Feb 22 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-02-29 (3120)
* Wed Feb 03 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-02-08 (3072)
* Tue Jan 19 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-01-25 (3030)
* Fri Jan 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-01-14 (3023)
* Thu Jan 07 2016 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2016-01-13 (2972)
* Tue Dec 01 2015 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.6.3 release
* Mon Oct 26 2015 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.6.3 release
* Tue Oct 20 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Wed Sep 30 2015 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Tue Sep 29 2015 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
