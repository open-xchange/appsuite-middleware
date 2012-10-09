
Name:           open-xchange-spamsettings-generic
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core >= @OXVERSION@
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 4
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        This bundle provides a generic interface for spam settings
Requires:       open-xchange-messaging >= @OXVERSION@

%description
This bundle provides a generic interface for spam settings

Authors:
--------
    Open-Xchange

%package        gui
Group:          Applications/Productivity
Summary:        Generic spam settings GUI Bundle
Requires:       open-xchange-spamsettings-generic-gui-theme >= @OXVERSION@
Requires:       open-xchange-gui

%description    gui
Generic spam settings GUI Bundle

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define docroot /var/www/html
%else
%define docroot /srv/www/htdocs
%endif

ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build
ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=%{name} -DbuildTarget=installGui -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/bundles/*

%files gui
%defattr(-,root,root)
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.spamsettings.generic
%{docroot}/ox6/plugins/com.openexchange.spamsettings.generic/*
%doc com.openexchange.spamsettings.generic/ChangeLog

%changelog
* Tue Oct 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Carsten Hoeger <choeger@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Carsten Hoeger <choeger@open-xchange.com>
Internal release build for EDP drop #2
* Thu May 10 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.21.0
