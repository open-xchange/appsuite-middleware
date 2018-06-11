%define __jar_repack %{nil}

Name:          open-xchange-advertisement
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
BuildRequires: open-xchange-rest >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 7
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open Xchange Advertisement
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-rest >= @OXVERSION@

%description
This package offers the possibility to manage configurations for advertisements.
 
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
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/com.openexchange.advertisement.clt.jar

%changelog
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
* Tue Aug 02 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial release
