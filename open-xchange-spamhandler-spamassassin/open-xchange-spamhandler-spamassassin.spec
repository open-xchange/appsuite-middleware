
Name:          open-xchange-spamhandler-spamassassin
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires: java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL_2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange SpamAssassin Handler
Requires:       open-xchange-core
Provides:	open-xchange-spamhandler
Conflicts:      open-xchange-spamhandler-default

%description
The Open-Xchange SpamAssassin Handler

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
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Mon May 07 2012 Marcus Klein  <marcus.klein@open-xchange.com>
Initial release
