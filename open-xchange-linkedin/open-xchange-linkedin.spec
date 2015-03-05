
Name:          open-xchange-linkedin
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires:  ant
%else
BuildRequires:  ant-nodeps
%endif
BuildRequires: open-xchange-oauth
BuildRequires: open-xchange-halo
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Open-Xchange integration with LinkedIn
AutoReqProv:   no
Requires:      open-xchange-oauth >= @OXVERSION@
Requires:      open-xchange-halo >= @OXVERSION@

%description
This package installs the bundles necessary for integrating Open-Xchange with LinkedIn. Special keys from LinkedIn are required to gain
access to the relevant API on LinkedIn.

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
if [ ${1:-0} -eq 2 ]; then
    # only when updating

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PROTECT="linkedinoauth.properties"
    for FILE in $PROTECT; do
        ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
    done

    # SoftwareChange_Request-1494
    PFILE=/opt/open-xchange/etc/linkedinoauth.properties
    if ! ox_exists_property com.openexchange.oauth.linkedin $PFILE; then
       if grep -E '^com.openexchange.*REPLACE_THIS_WITH_THE_KEY_FROM' $PFILE > /dev/null; then
           ox_set_property com.openexchange.oauth.linkedin false $PFILE
       else
           ox_set_property com.openexchange.oauth.linkedin true $PFILE
       fi
    fi

    # SoftwareChange_Request-1501
    # updated by SoftwareChange_Request-1710
    ox_add_property com.openexchange.subscribe.socialplugin.linkedin.autorunInterval 1d /opt/open-xchange/etc/linkedinsubscribe.properties

    # SoftwareChange_Request-2410
    PFILE=/opt/open-xchange/etc/linkedinoauth.properties
    OLDNAMES=( com.openexchange.socialplugin.linkedin.apikey com.openexchange.socialplugin.linkedin.apisecret )
    NEWNAMES=( com.openexchange.oauth.linkedin.apiKey com.openexchange.oauth.linkedin.apiSecret )
    for I in $(seq 1 ${#OLDNAMES[@]}); do
        VALUE=$(ox_read_property ${OLDNAMES[$I-1]} $PFILE)
        if [ "" != "$VALUE" ]; then
            ox_add_property ${NEWNAMES[$I-1]} "$VALUE" $PFILE
            ox_remove_property ${OLDNAMES[$I-1]} $PFILE
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
%config(noreplace) /opt/open-xchange/etc/halo-linkedin.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/linkedinoauth.properties
%config(noreplace) /opt/open-xchange/etc/linkedinsubscribe.properties

%changelog
* Tue Feb 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.6.2 release
* Thu Feb 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Extracting LinkedIn integration into its own package due to special API access grants are necessary
