%define __jar_repack %{nil}

Name:          open-xchange-passwordchange-script
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange password-change bundle that utilizes a script (e.g. "/bin/changepwd.sh") to change the password
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Conflicts:     open-xchange-passwordchange-database

%description
The Open-Xchange password-change bundle that utilizes a script (e.g. "/bin/changepwd.sh") to change the password

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-passwordchange-script/properties/

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
