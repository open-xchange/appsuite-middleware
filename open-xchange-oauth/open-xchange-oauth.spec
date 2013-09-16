
Name:          open-xchange-oauth
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 9
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

if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PROTECT="facebookoauth.properties linkedinoauth.properties msnoauth.properties yahoooauth.properties xingoauth.properties settings/flickroauth.properties settings/tumblroauth.properties"
    for FILE in $PROTECT; do
        ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
    done

    # SoftwareChange_Request-1494
    pfile=/opt/open-xchange/etc/msnoauth.properties
    if ! ox_exists_property com.openexchange.oauth.msn $pfile; then
       if grep -E '^com.openexchange.*REPLACE_THIS_WITH_VALUE_OBTAINED_FROM' $pfile > /dev/null; then
           ox_set_property com.openexchange.oauth.msn false $pfile
       else
           ox_set_property com.openexchange.oauth.msn true $pfile
       fi
    fi
    pfile=/opt/open-xchange/etc/yahoooauth.properties
    if ! ox_exists_property com.openexchange.oauth.yahoo $pfile; then
       if grep -E '^com.openexchange.*REPLACE_THIS_WITH_VALUE_OBTAINED_FROM' $pfile > /dev/null; then
           ox_set_property com.openexchange.oauth.yahoo false $pfile
       else
           ox_set_property com.openexchange.oauth.yahoo true $pfile
       fi
    fi
    pfile=/opt/open-xchange/etc/linkedinoauth.properties
    if ! ox_exists_property com.openexchange.oauth.linkedin $pfile; then
       if grep -E '^com.openexchange.*REPLACE_THIS_WITH_THE_KEY_FROM' $pfile > /dev/null; then
           ox_set_property com.openexchange.oauth.linkedin false $pfile
       else
           ox_set_property com.openexchange.oauth.linkedin true $pfile
       fi
    fi
    pfile=/opt/open-xchange/etc/facebookoauth.properties
    if ! ox_exists_property com.openexchange.oauth.facebook $pfile; then
       if grep -E '^com.openexchange.*INSERT_YOUR_API_KEY_HERE' $pfile > /dev/null; then
           ox_set_property com.openexchange.oauth.facebook false $pfile
       else
           ox_set_property com.openexchange.oauth.facebook true $pfile
       fi
    fi
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
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/facebookoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/linkedinoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/msnoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/yahoooauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/xingoauth.properties
%config(noreplace) /opt/open-xchange/etc/*
%dir /opt/open-xchange/etc/settings/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/settings/flickroauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/settings/tumblroauth.properties
%config(noreplace) /opt/open-xchange/etc/settings/*

%changelog
* Thu Sep 12 2013 Steffen Templin <marcus.klein@open-xchange.com>
Ninth candidate for 7.4.0 release
* Wed Sep 11 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Thu Sep 05 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-09-05
* Mon Sep 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Eighth candidate for 7.4.0 release
* Mon Sep 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-09-26
* Fri Aug 30 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-30
* Wed Aug 28 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-09-03
* Tue Aug 27 2013 Steffen Templin <marcus.klein@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Steffen Templin <marcus.klein@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Steffen Templin <marcus.klein@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Steffen Templin <marcus.klein@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Steffen Templin <marcus.klein@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Steffen Templin <marcus.klein@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Steffen Templin <marcus.klein@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Steffen Templin <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Steffen Templin <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Steffen Templin <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Mon Nov 05 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Fri Nov 02 2012 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Wed Oct 31 2012 Steffen Templin <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Fri Oct 26 2012 Steffen Templin <marcus.klein@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Steffen Templin <marcus.klein@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Steffen Templin <marcus.klein@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Steffen Templin <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Steffen Templin <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Steffen Templin <marcus.klein@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Steffen Templin <marcus.klein@open-xchange.com>
prepare for next EDP drop
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
