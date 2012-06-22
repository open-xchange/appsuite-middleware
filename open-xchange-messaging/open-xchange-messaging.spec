
Name:          open-xchange-messaging
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
BuildRequires:  open-xchange-oauth
BuildRequires:  open-xchange-xerces
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires:  java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires:  java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires:  java-1.6.0-sun-devel
%endif
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend messaging extension
Requires:       open-xchange-core >= @OXVERSION@
Requires:       open-xchange-oauth >= @OXVERSION@
Requires:       open-xchange-xerces
Provides:       open-xchange-messaging-facebook = %{version}
Provides:       open-xchange-messaging-generic = %{version}
Provides:       open-xchange-messaging-json = %{version}
Provides:       open-xchange-messaging-rss = %{version}
Provides:       open-xchange-messaging-twitter = %{version}
Provides:       open-xchange-twitter = %{version}
Obsoletes:      open-xchange-messaging-facebook <= %{version}
Obsoletes:      open-xchange-messaging-generic <= %{version}
Obsoletes:      open-xchange-messaging-json <= %{version}
Obsoletes:      open-xchange-messaging-rss <= %{version}
Obsoletes:      open-xchange-messaging-twitter <= %{version}
Obsoletes:      open-xchange-twitter <= %{version}

%description
Adds the feature to use messaging services to the backend installation.

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
    CONFFILES="facebookmessaging.properties rssmessaging.properties twittermessaging.properties twitter.properties"
    for FILE in ${CONFFILES}; do
        if [ -e /opt/open-xchange/etc/groupware/${FILE} ]; then
            mv /opt/open-xchange/etc/${FILE} /opt/open-xchange/etc/${FILE}.rpmnew
            mv /opt/open-xchange/etc/groupware/${FILE} /opt/open-xchange/etc/${FILE}
        fi
    done
fi


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
* Tue Apr 17 2012 Sonja Krause-Harder  <sonja.krause-harder@open-xchange.com>
Internal release build for EDP drop #1
