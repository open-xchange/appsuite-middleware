%define __jar_repack %{nil}

Name:          open-xchange-authentication-ucs-common
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-core
Version:       @OXVERSION@
%define        ox_release 18
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Common Module for authenticating users on a Univention Corporate Server installation
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundle implementing the common helper service that uses Univention
Corporate Server to authenticate login requests.

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

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

if [ ${1:-0} -eq 2 ]; then # only when updating

    # SoftwareChange_Request-2523
    ox_add_property com.openexchange.authentication.ucs.passwordChangeURL "" /opt/open-xchange/etc/authplugin.properties

    # SoftwareChange_Request-3944
    pfile=/opt/open-xchange/etc/authplugin.properties
    if ox_exists_property LDAP_HOST $pfile; then
       ohost=$(ox_read_property LDAP_HOST $pfile)
       oport=$(ox_read_property LDAP_PORT $pfile)
       ox_set_property com.openexchange.authentication.ucs.ldapUrl "ldap://$ohost:$oport" $pfile
       ox_remove_property LDAP_HOST $pfile
       ox_remove_property LDAP_PORT $pfile
    fi
    if ox_exists_property LDAP_BASE $pfile; then
       oval=$(ox_read_property LDAP_BASE $pfile)
       ox_set_property com.openexchange.authentication.ucs.baseDn "$oval" $pfile
       ox_remove_property LDAP_BASE $pfile
    fi
    if ox_exists_property LDAP_SEARCH $pfile; then
       oval=$(ox_read_property LDAP_SEARCH $pfile | sed 's;@USER@;%s;g')
       ox_set_property com.openexchange.authentication.ucs.searchFilter "$oval" $pfile
       ox_remove_property LDAP_SEARCH $pfile
    fi
    if ox_exists_property LDAP_ATTRIBUTE $pfile; then
       oval=$(ox_read_property LDAP_ATTRIBUTE $pfile)
       ox_set_property com.openexchange.authentication.ucs.mailAttribute "$oval" $pfile
       ox_remove_property LDAP_ATTRIBUTE $pfile
    fi
    if ox_exists_property USE_POOL $pfile; then
       oval=$(ox_read_property USE_POOL $pfile)
       ox_set_property com.openexchange.authentication.ucs.useLdapPool "$oval" $pfile
       ox_remove_property USE_POOL $pfile
    fi
    ox_add_property com.openexchange.authentication.ucs.loginAttribute "uid" $pfile
    ox_add_property com.openexchange.authentication.ucs.contextIdAttribute "" $pfile
    ox_add_property com.openexchange.authentication.ucs.bindDn "" $pfile
    ox_add_property com.openexchange.authentication.ucs.bindPassword "" $pfile
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/bundles/com.openexchange.authentication.ucs.common.jar
/opt/open-xchange/osgi/bundle.d/com.openexchange.authentication.ucs.common.ini
%config(noreplace) /opt/open-xchange/etc/authplugin.properties

%changelog
* Wed Jul 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-17 (5819)
* Thu Jul 09 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-13 (5804)
* Fri Jun 26 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-02 (5792)
* Wed Jun 24 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-06-30 (5781)
* Mon Jun 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-06-15 (5765)
* Fri May 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-05-26 (5742)
* Mon May 04 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-05-11 (5720)
* Thu Apr 23 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-30 (5702)
* Fri Apr 17 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-02 (5692)
* Mon Apr 06 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-14 (5677)
* Thu Mar 19 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-03-23 (5653)
* Fri Feb 28 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Felix Marx <felix.marx@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.2 release
* Mon Oct 08 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.1 release
