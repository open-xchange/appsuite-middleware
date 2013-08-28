
Name:          open-xchange-oauth
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 14
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
. /opt/open-xchange/lib/oxfunctions.sh
CONFFILES="deferrer.properties oauth.properties facebookoauth.properties linkedinoauth.properties msnoauth.properties twitteroauth.properties yahoooauth.properties"
for FILE in ${CONFFILES}; do
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
done

PROTECT="facebookoauth.properties linkedinoauth.properties msnoauth.properties yahoooauth.properties"
for FILE in $PROTECT
do
    ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
done
exit 0

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/facebookoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/linkedinoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/msnoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/yahoooauth.properties
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Tue Jun 11 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Mon Nov 05 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Wed Oct 31 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Wed Oct 10 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Steffen Templin <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Steffen Templin <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Mon Apr 16 2012 Steffen Templin <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
* Wed Apr 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
Internal release build for EDP drop #0
* Mon Jan 30 2012 Steffen Templin <steffen.templin@open-xchange.com>
Initial release
