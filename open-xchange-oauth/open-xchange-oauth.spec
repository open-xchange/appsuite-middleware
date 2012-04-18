Name:           open-xchange-oauth
BuildArch:      noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant ant-nodeps open-xchange-core
%if 0%{?suse_version}  && !0%{?sles_version}
BuildRequires:  java-sdk-openjdk
%endif
%if 0%{?sles_version} == 11
# SLES 11
BuildRequires:  java-1_6_0-ibm-devel
%endif
%if 0%{?rhel_version} || 0%{?fedora_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open-Xchange OAuth implementation
Requires:	open-xchange-core >= @OXVERSION@
Provides:       open-xchange-http-deferrer = @OXVERSION@ open-xchange-oauth-facebook = @OXVERSION@ open-xchange-oauth-json = @OXVERSION@ open-xchange-oauth-linkedin = @OXVERSION@ open-xchange-oauth-msn = @OXVERSION@ open-xchange-oauth-twitter = @OXVERSION@ open-xchange-oauth-yahoo = @OXVERSION@
Obsoletes:      open-xchange-http-deferrer <= @OXVERSION@ open-xchange-oauth-facebook <= @OXVERSION@ open-xchange-oauth-json <= @OXVERSION@ open-xchange-oauth-linkedin <= @OXVERSION@ open-xchange-oauth-msn <= @OXVERSION@ open-xchange-oauth-twitter <= @OXVERSION@ open-xchange-oauth-yahoo <= @OXVERSION@
#

%description

The Open-Xchange OAuth implementation

Authors:
--------
    Open-Xchange
    
%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=open-xchange-oauth -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    GWCONFFILES="deferrer.properties oauth.properties facebookoauth.properties linkedinoauth.properties msnoauth.properties twitteroauth.properties yahoooauth.properties"
    for FILE in ${GWCONFFILES}; do
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
