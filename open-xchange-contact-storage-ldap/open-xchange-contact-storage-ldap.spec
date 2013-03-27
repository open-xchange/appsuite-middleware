
Name:           open-xchange-contact-storage-ldap
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 6
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Contact storage provider using a LDAP server as backend
Requires:      open-xchange-core >= @OXVERSION@

%description
Contact storage provider using a LDAP server as backend

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
%dir %attr(750,root,open-xchange) /opt/open-xchange/etc/contact-storage-ldap
%attr(640,root,open-xchange) /opt/open-xchange/etc/contact-storage-ldap/*.example
%config(noreplace) /opt/open-xchange/etc/contact-storage-ldap/cache.properties

%changelog
* Tue Mar 12 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Wed Feb 27 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Thu Jan 10 2013 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.0.1
* Tue Dec 04 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.0.0 release
* Tue Nov 13 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for EDP drop #6
* Fri Oct 26 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Release build for EDP drop #5
* Tue Sep 04 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Initial release
