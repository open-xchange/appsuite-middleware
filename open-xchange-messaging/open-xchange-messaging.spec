%define        configfiles     configfiles.list

Name:           open-xchange-messaging
BuildArch:      noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
BuildRequires:  ant-nodeps
BuildRequires:  open-xchange-core
BuildRequires:  open-xchange-oauth
BuildRequires:  open-xchange-xerces
BuildRequires:  java-devel >= 1.6.0
Version:        @OXVERSION@
%define         ox_release 13
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
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(twitter)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    CONFFILES="facebookmessaging.properties rssmessaging.properties twittermessaging.properties twitter.properties"
    for FILE in ${CONFFILES}; do
	ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
    done
    ox_update_permissions "/opt/open-xchange/etc/twitter.properties" root:open-xchange 640
fi


%clean
%{__rm} -rf %{buildroot}

%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/

%changelog
* Thu May 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Tue Feb 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Mon Nov 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Mon Apr 16 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
* Wed Apr 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #0
* Tue Feb 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
