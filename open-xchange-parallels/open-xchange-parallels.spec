
Name:          open-xchange-parallels
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-admin
BuildRequires: open-xchange-spamhandler-spamassassin
BuildRequires: java-devel >= 1.6.0
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange Parallels Extensions. Authentication Plugin, Branding Plugin and SpamdProvider.
Requires:       open-xchange-admin-soap >= @OXVERSION@
Requires:       open-xchange-spamhandler-spamassassin >= @OXVERSION@

%description
The Open-Xchange Parallels Extensions. Authentication Plugin, Branding Plugin and SpamdProvider.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    if [ -e /opt/open-xchange/etc/groupware/parallels.properties ]; then
        mv /opt/open-xchange/etc/parallels.properties /opt/open-xchange/etc/parallels.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/parallels.properties /opt/open-xchange/etc/parallels.properties
    fi
    if [ -e /opt/open-xchange/etc/groupware/settings/parallels_gui.properties ]; then
        mv /opt/open-xchange/etc/settings/parallels-ui.properties /opt/open-xchange/etc/settings/parallels-ui.properties.rpmnew
        mv /opt/open-xchange/etc/groupware/settings/parallels_gui.properties /opt/open-xchange/etc/settings/parallels-ui.properties
    fi
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*.properties
%dir /opt/open-xchange/etc/settings/
%config(noreplace) /opt/open-xchange/etc/settings/parallels-ui.properties

%changelog
* Wed Jul 11 2012 - marcus.klein@open-xchange.com
 - Initial release
