
Name:          open-xchange-osgi
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires: java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires: java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires: java-1.6.0-sun-devel
%endif
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       OSGi bundles commonly used by all Open-Xchange packages
PreReq:        /usr/sbin/useradd
%if 0%{?suse_version} && ! 0%{?sles_version} == 11
Requires:      java-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES11
Requires:      java-1_6_0-ibm
%endif
%if 0%{?fedora_version}
Requires:      java-1.6.0-openjdk
%endif
%if 0%{?rhel_version}
# RHEL5 removed sun-java5, but some might still use it, so just depend on sun-java
Requires:      java-sun
%endif
Provides:       open-xchange-common = %{version}
Obsoletes:      open-xchange-common <= %{version}
Provides:       open-xchange-activation = %{version}
Obsoletes:      open-xchange-activation <= %{version}

%description
OSGi bundles commonly used by all Open-Xchange packages

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

mkdir -m 750 -p %{buildroot}/opt/open-xchange/osgi

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%pre
/usr/sbin/groupadd -r open-xchange 2> /dev/null || :
/usr/sbin/useradd -r -g open-xchange -r -s /bin/false -c "open-xchange system user" -d /opt/open-xchange open-xchange 2> /dev/null || :

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*

%changelog
