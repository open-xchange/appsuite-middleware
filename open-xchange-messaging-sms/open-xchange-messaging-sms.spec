
Name:          open-xchange-messaging-sms
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-messaging >= @OXVERSION@
BuildRequires: java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        This bundle provides a new messaging interface for SMS services
Requires:       open-xchange-messaging >= @OXVERSION@

%description
This bundle provides a new messaging interface for SMS services

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

ant -lib build/lib -Dbasedir=build -Dhtdoc=%{docroot} -DdestDir=%{buildroot} -DpackageName=open-xchange-messaging-sms -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/bundles/*

%changelog
* Wed May 09 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Initial release
