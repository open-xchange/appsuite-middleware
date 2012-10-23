
Name:           open-xchange-spamhandler-parallels
BuildArch:      noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-spamhandler-spamassassin >= @OXVERSION@
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 1
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Spam handler for Parallels extensions
Requires:       open-xchange-parallels >= @OXVERSION@
Requires:       open-xchange-spamhandler-spamassassin >= @OXVERSION@

%description
This package contains the spam handler implementation for Parallels extension. This spam handler extends the spam handler for SpamAssassin
by configuring user specific host names, ports and user names.
Not necessarily SpamAssassin will be used with Parallels extensions. Therefore the spam handler for Parallels extension is located in its
own package to be able to replace it with every other possible spam handler implementation.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*

%changelog
