%define __jar_repack %{nil}

Name:          open-xchange-oauth
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange OAuth implementation
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Provides:      open-xchange-http-deferrer = %{version}
Obsoletes:     open-xchange-http-deferrer < %{version}
Provides:      open-xchange-oauth-json = %{version}
Obsoletes:     open-xchange-oauth-json < %{version}
Provides:      open-xchange-oauth-linkedin = %{version}
Obsoletes:     open-xchange-oauth-linkedin < %{version}
Provides:      open-xchange-oauth-msn = %{version}
Obsoletes:     open-xchange-oauth-msn < %{version}
Provides:      open-xchange-oauth-twitter = %{version}
Obsoletes:     open-xchange-oauth-twitter < %{version}
Provides:      open-xchange-oauth-yahoo = %{version}
Obsoletes:     open-xchange-oauth-yahoo < %{version}

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
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PROTECT="yahoooauth.properties xingoauth.properties settings/flickroauth.properties settings/tumblroauth.properties"
    for FILE in $PROTECT; do
        ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
    done

    # SoftwareChange_Request-2410
    if [ -e /opt/open-xchange/etc/twitter.properties ]; then
        VALUE=$(ox_read_property com.openexchange.twitter.consumerKey /opt/open-xchange/etc/twitter.properties)
        if [ "" != "$VALUE" ]; then
            ox_add_property com.openexchange.oauth.twitter.apiKey "$VALUE" /opt/open-xchange/etc/twitteroauth.properties
            ox_remove_property com.openexchange.twitter.consumerKey /opt/open-xchange/etc/twitter.properties
        fi
        VALUE=$(ox_read_property com.openexchange.twitter.consumerSecret /opt/open-xchange/etc/twitter.properties)
        if [ "" != "$VALUE" ]; then
            ox_add_property com.openexchange.oauth.twitter.apiSecret "$VALUE" /opt/open-xchange/etc/twitteroauth.properties
            ox_remove_property com.openexchange.twitter.consumerSecret /opt/open-xchange/etc/twitter.properties
        fi
    else
        ox_add_property com.openexchange.oauth.twitter.apiKey INSERT_YOUR_CONSUMER_KEY_HERE /opt/open-xchange/etc/twitteroauth.properties
        ox_add_property com.openexchange.oauth.twitter.apiSecret INSERT_YOUR_CONSUMER_SECRET_HERE /opt/open-xchange/etc/twitteroauth.properties
    fi

    # SoftwareChange_Request-2532
    VALUE=$(ox_read_property com.openexchange.oauth.google.redirectUrl /opt/open-xchange/etc/googleoauth.properties)
    ox_set_property com.openexchange.oauth.google.redirectUrl "$VALUE" /opt/open-xchange/etc/googleoauth.properties

    # SoftwareChange_Request-3506
    ox_add_property com.openexchange.oauth.dropbox.redirectUrl REPLACE_WITH_REDIRECT_URL /opt/open-xchange/etc/dropboxoauth.properties
    ox_add_property com.openexchange.oauth.dropbox.productName REPLACE_WITH_YOUR_REGISTERED_DROPBOX_APP /opt/open-xchange/etc/dropboxoauth.properties

    # SoftwareChange_Request-3556
    ox_add_property com.openexchange.oauth.yahoo.redirectUrl REPLACE_WITH_REDIRECT_URL /opt/open-xchange/etc/yahoooauth.properties
    ox_add_property com.openexchange.oauth.yahoo.productName REPLACE_WITH_YOUR_REGISTERED_YAHOO_APP /opt/open-xchange/etc/yahoooauth.properties

    SCR=SCR-316
    ox_scr_todo ${SCR} && {
      old_prefix=com.openexchange.oauth.msliveconnect
      new_prefix=com.openexchange.oauth.microsoft.graph
      old_pfile=/opt/open-xchange/etc/msliveconnectoauth.properties
      new_pfile=/opt/open-xchange/etc/microsoftgraphoauth.properties
      if [ -e $old_pfile ]
      then
        redirect=$(ox_read_property ${old_prefix}.redirectUrl ${old_pfile})
        if [[ ( -n "${redirect}") && (! "${redirect}" = REPLACE_THIS*) ]]
        then
          ox_set_property ${new_prefix}.redirectUrl ${redirect} ${new_pfile}
        fi
        enabled=$(ox_read_property ${old_prefix} ${old_pfile})
        if [[ "${enabled}" = true ]]
        then
          ox_set_property ${new_prefix} ${enabled} ${new_pfile}
        fi
      fi
      ox_scr_done ${SCR}
    }
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/boxcomoauth.properties
%config(noreplace) /opt/open-xchange/etc/deferrer.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/dropboxoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/googleoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/microsoftgraphoauth.properties
%config(noreplace) /opt/open-xchange/etc/oauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/twitteroauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/xingoauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/yahoooauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/settings/flickroauth.properties
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/settings/tumblroauth.properties
/usr/share
%doc /usr/share/doc/open-xchange-oauth/properties/

%changelog
* Mon Jun 17 2019 Steffen Templin <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Steffen Templin <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Steffen Templin <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Steffen Templin <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Steffen Templin <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
