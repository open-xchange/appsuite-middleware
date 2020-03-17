%define __jar_repack %{nil}

Name:          open-xchange-osgi
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       3rd party OSGi bundles used by the Open-Xchange backend
Autoreqprov:   no
Provides:      open-xchange-common = %{version}
Obsoletes:     open-xchange-common < %{version}
Provides:      open-xchange-activation = %{version}
Obsoletes:     open-xchange-activation < %{version}
%if 0%{?rhel_version}
Requires:      java-1.8.0-openjdk-headless
%else
Requires:      java-1_8_0-openjdk-headless
%endif
# No ibm java on RHEL and on SLE, please
Conflicts:     java-ibm

%description
This package installes 3rd party OSGi bundles for the Open-Xchange backend. This includes the Equinox OSGi framework and the servlet API.
Furthermore libraries from the Apache Commons project are installed: CLI, Lang, Logging and some Apache service mix bundles.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /usr/share/doc/open-xchange-osgi/
%dir /usr/share/doc/open-xchange-osgi/docs/
%dir /usr/share/doc/open-xchange-osgi/docs/3rd_party_licenses/
/usr/share/doc/open-xchange-osgi/docs/3rd_party_licenses/*

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
* Thu Oct 18 2018 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.2 release
