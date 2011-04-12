
# norootforbuild
Name:           open-xchange-meta
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
%if 0%{?suse_version} && 0%{?sles_version} < 11
%if %{?suse_version} <= 1010
# SLES10
BuildRequires:  java-1_5_0-ibm >= 1.5.0_sr9
BuildRequires:  java-1_5_0-ibm-devel >= 1.5.0_sr9
BuildRequires:  java-1_5_0-ibm-alsa >= 1.5.0_sr9
BuildRequires:  update-alternatives
%endif
%if %{?suse_version} >= 1100
BuildRequires:  java-sdk-openjdk
%endif
%if %{?suse_version} > 1010 && %{?suse_version} < 1100
BuildRequires:  java-sdk-1.5.0-sun
%endif
%endif
%if 0%{?sles_version} >= 11
# SLES11 or higher
BuildRequires:  java-1_6_0-ibm-devel
%endif

%if 0%{?rhel_version}
# libgcj seems to be installed whether we want or not and libgcj needs cairo
BuildRequires:  java-sdk-1.5.0-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%if %{?fedora_version} <= 8
BuildRequires:  java-devel-icedtea saxon
%endif
%endif
Version:	@OXVERSION@
%define		ox_release 13
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Meta packages

%define oxcommon open-xchange, open-xchange-contactcollector, open-xchange-conversion, open-xchange-conversion-engine, open-xchange-conversion-servlet, open-xchange-crypto, open-xchange-data-conversion-ical4j, open-xchange-dataretention, open-xchange-genconf, open-xchange-genconf-mysql, open-xchange-imap, open-xchange-management, open-xchange-monitoring, open-xchange-pop3, open-xchange-push-udp, open-xchange-server, open-xchange-settings-extensions, open-xchange-smtp, open-xchange-sql, open-xchange-templating, open-xchange-threadpool, open-xchange-charset, open-xchange-control, open-xchange-i18n, open-xchange-jcharset, open-xchange-sessiond, open-xchange-calendar-printing, open-xchange-user-json, open-xchange-gui-wizard-plugin, open-xchange-report-client, open-xchange-secret

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-server
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX Backend
Requires:	%{oxcommon}
Requires:       open-xchange-authentication, open-xchange-spamhandler

%description -n open-xchange-meta-server
The Open-Xchange Meta package for OX Backend

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-admin
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for User/Group Provisioning
Requires:	open-xchange-admin-client, open-xchange-admin-lib, open-xchange-admin-plugin-hosting, open-xchange-admin-plugin-hosting-client, open-xchange-admin-plugin-hosting-lib, open-xchange-admin-doc, open-xchange-admin-plugin-hosting-doc

%description -n open-xchange-meta-admin
The Open-Xchange Meta package for User/Group Provisioning

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-pubsub
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Publish and Subscribe
Requires:	open-xchange-publish, open-xchange-publish-basic, open-xchange-publish-infostore-online, open-xchange-publish-json, open-xchange-publish-microformats, open-xchange-subscribe, open-xchange-subscribe-json, open-xchange-subscribe-microformats, open-xchange-subscribe-crawler, open-xchange-templating


%description -n open-xchange-meta-pubsub
The Open-Xchange Meta package for Publish and Subscribe

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-messaging
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Messaging
Requires:	open-xchange-unifiedinbox, open-xchange-messaging-twitter, open-xchange-messaging-rss, open-xchange-messaging-json, open-xchange-messaging-facebook


%description -n open-xchange-meta-messaging
The Open-Xchange Meta package for Messaging

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-gui
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX GUI
Requires:	open-xchange-gui, open-xchange-gui-wizard-plugin-gui, open-xchange-online-help-de, open-xchange-online-help-en, open-xchange-online-help-fr


%description -n open-xchange-meta-gui
The Open-Xchange Meta package for OX Backend

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-singleserver
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX on a single server
Requires:	open-xchange-meta-server, open-xchange-meta-gui, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging


%description -n open-xchange-meta-singleserver
The Open-Xchange Meta package for OX on a single server

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-databaseonly
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX managed via database only
Requires:	open-xchange-passwordchange-database, open-xchange-passwordchange-servlet, open-xchange-resource-managerequest, open-xchange-group-managerequest


%description -n open-xchange-meta-databaseonly
The Open-Xchange Meta package for OX managed via database only

Authors:
--------
    Open-Xchange


# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-mobility
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Business Mobility
Requires:	open-xchange-eas, open-xchange-usm, open-xchange-help-usm-eas, open-xchange-mobile-configuration-generator, open-xchange-mobile-configuration-json, open-xchange-mobile-configuration-json-action-email, open-xchange-mobile-configuration-gui


%description -n open-xchange-meta-mobility
The Open-Xchange Meta package for Business Mobility

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-plesk
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX into Plesk integration
Requires:	%{oxcommon}
Requires:	open-xchange-spamhandler-default, open-xchange-admin-soap, open-xchange-easylogin, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui, open-xchange-lang-es-es, open-xchange-lang-nl-nl, open-xchange-gui-lang-es-es, open-xchange-gui-lang-es-es, open-xchange-online-help-es, open-xchange-online-help-nl, open-xchange-upsell-generic

%description -n open-xchange-meta-plesk
The Open-Xchange Meta package for OX into Plesk integration

Authors:
--------
    Open-Xchange


# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-parallels
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX into Parallels integration
Requires:	%{oxcommon}
Requires:	open-xchange-custom-parallels, open-xchange-custom-parallels-gui, open-xchange-spamhandler-spamassassin, open-xchange-admin-soap, open-xchange-easylogin, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui, open-xchange-lang-es-es, open-xchange-lang-nl-nl, open-xchange-gui-lang-es-es, open-xchange-gui-lang-es-es, open-xchange-online-help-es, open-xchange-online-help-nl

%description -n open-xchange-meta-parallels
The Open-Xchange Meta package for OX into Parallels integration

Authors:
--------
    Open-Xchange


# ----------------------------------------------------------------------------------------------------

%description
Open-Xchange Meta packages

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

%clean
%{__rm} -rf %{buildroot}


%files
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-server
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-databaseonly
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-gui
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-admin
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-pubsub
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-messaging
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-singleserver
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-plesk
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-parallels
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-mobility
%defattr(-,root,root)
%doc README.TXT

%changelog
* Thu Jun 24 2010 - choeger@open-xchange.com
 - Bugfix #16354 - strange package dependencies for open-xchange-meta-singleserver
 - Bugfix #16000 - Add open-xchange-mail-pushnotify to package dependency list
   in open-xchange-meta-oxucs.
