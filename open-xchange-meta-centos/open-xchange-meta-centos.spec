
Name:           open-xchange-meta-centos
BuildArch:	noarch
#!BuildIgnore: post-build-checks
Version:	@OXVERSION@
%define		ox_release 8
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange Meta package to install OX on CentOS5
%if 0%{?centos_version}
Requires:       java-1.6.0-openjdk
Provides:       java-sun
%endif

%description
The Open-Xchange Meta package to install OX on CentOS5

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

%clean
%{__rm} -rf %{buildroot}


%files
%defattr(-,root,root)
%doc README.TXT

%changelog
* Mon Mar 04 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2013-01-15
* Wed Oct 10 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.0
* Thu Sep 06 2012 Carsten Hoeger <choeger@open-xchange.com>
Initial release
