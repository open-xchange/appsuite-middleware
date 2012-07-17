
Name:           open-xchange-upsell-generic
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The generic Open-Xchange upsell layer bundle
Requires:       open-xchange-core >= @OXVERSION@

%description
The generic Open-Xchange upsell layer bundle

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
%dir /opt/open-xchange/etc/settings
%config(noreplace) /opt/open-xchange/etc/settings/upsell.properties

%changelog
* Tue Jun 17 2012 Marcus Klein  <marcus.klein@open-xchange.com>
 - Initial release
