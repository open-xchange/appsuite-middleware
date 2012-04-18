
Name:          open-xchange-subscribe
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant ant-nodeps
BuildRequires: open-xchange-core open-xchange-oauth
%if 0%{?suse_version} && !0%{?sles_version}
BuildRequires: java-sdk-openjdk open-xchange-xerces-sun
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-openjdk-devel open-xchange-xerces-sun
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/ 
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange backend subscribe extension
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-oauth >= @OXVERSION@
Requires:      open-xchange-xerces
Provides:      open-xchange-subscribe-crawler = %{version}
Obsoletes:     open-xchange-subscribe-crawler <= %{version}
Provides:      open-xchange-subscribe-facebook = %{version}
Obsoletes:     open-xchange-subscribe-facebook <= %{version}
Provides:      open-xchange-subscribe-json = %{version}
Obsoletes:     open-xchange-subscribe-json <= %{version}
Provides:      open-xchange-subscribe-linkedin = %{version} 
Obsoletes:     open-xchange-subscribe-linkedin <= %{version}
Provides:      open-xchange-subscribe-microformats = %{version}
Obsoletes:     open-xchange-subscribe-microformats <= %{version}
Provides:      open-xchange-subscribe-msn = %{version}
Obsoletes:     open-xchange-subscribe-msn <= %{version}
Provides:      open-xchange-subscribe-yahoo = %{version}
Obsoletes:     open-xchange-subscribe-yahoo <= %{version}

%description
Adds the feature to subscribe to third party services or
publications to the backend installation.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

# FIXME INSTALL SCRIPTS MISSING!

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
%dir /opt/open-xchange/etc/crawlers/
%config(noreplace) /opt/open-xchange/etc/crawlers/*
%doc docs/

%changelog

