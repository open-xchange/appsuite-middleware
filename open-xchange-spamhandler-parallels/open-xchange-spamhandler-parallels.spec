%define __jar_repack %{nil}

Name:           open-xchange-spamhandler-parallels
BuildArch:      noarch
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Spam handler for Parallels extensions
Autoreqprov:   no
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
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
