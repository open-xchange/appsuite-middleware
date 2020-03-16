%define __jar_repack %{nil}

Name:          open-xchange-drive-client-windows
BuildArch:     noarch
BuildRequires: open-xchange-admin
BuildRequires: open-xchange-core >= @OXVERSION@, open-xchange-client-onboarding >= @OXVERSION@, open-xchange-drive >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       New updater for windows drive clients
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@, open-xchange-client-onboarding >= @OXVERSION@, open-xchange-drive >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Requires:      open-xchange-drive-client-windows-files

%description
This package offers windows drive clients the possibility to update themselves without the use of the ox-updater.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/
(cd %{buildroot}/opt/open-xchange/sbin/ && ln -s reloaddriveclients listdriveclients)

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
/usr/share
%doc /usr/share/doc/open-xchange-drive-client-windows/properties/

%changelog
* Mon Jun 17 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
prepare for 7.10.2 release
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
* Wed Mar 30 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.8.1 release
* Tue Nov 10 2015 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Initial release
