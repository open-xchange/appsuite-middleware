
Name:          open-xchange-oauth
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange OAuth implementation
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-http-deferrer = %{version}
Obsoletes:     open-xchange-http-deferrer <= %{version}
Provides:      open-xchange-oauth-facebook = %{version}
Obsoletes:     open-xchange-oauth-facebook <= %{version}
Provides:      open-xchange-oauth-json = %{version}
Obsoletes:     open-xchange-oauth-json <= %{version}
Provides:      open-xchange-oauth-linkedin = %{version}
Obsoletes:     open-xchange-oauth-linkedin <= %{version}
Provides:      open-xchange-oauth-msn = %{version}
Obsoletes:     open-xchange-oauth-msn <= %{version}
Provides:      open-xchange-oauth-twitter = %{version}
Obsoletes:     open-xchange-oauth-twitter <= %{version}
Provides:      open-xchange-oauth-yahoo = %{version}
Obsoletes:     open-xchange-oauth-yahoo <= %{version}


%description
The Open-Xchange OAuth implementation.

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
    CONFFILES="deferrer.properties oauth.properties facebookoauth.properties linkedinoauth.properties msnoauth.properties twitteroauth.properties yahoooauth.properties"
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
