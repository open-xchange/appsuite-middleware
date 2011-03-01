
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
BuildRequires:  java-sdk-sun cairo
%endif
%if 0%{?fedora_version}
%if %{?fedora_version} > 8
BuildRequires:  java-1.6.0-openjdk-devel saxon
%endif
%endif
%if 0%{?centos_version}
BuildRequires:  java-1.6.0-openjdk-devel
%endif
Version:	@OXVERSION@
%define		ox_release 8
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange Meta packages

%define oxcommon open-xchange, open-xchange-crypto, open-xchange-data-conversion-ical4j, open-xchange-genconf-mysql, open-xchange-imap, open-xchange-pop3, open-xchange-smtp, open-xchange-sql, open-xchange-templating, open-xchange-control, open-xchange-calendar-printing, open-xchange-gui-wizard-plugin, open-xchange-report-client

%define alllang open-xchange-gui-lang-es-es, open-xchange-gui-lang-it-it, open-xchange-gui-lang-ja-jp, open-xchange-gui-lang-nl-nl, open-xchange-gui-lang-pl-pl, open-xchange-lang-es-es, open-xchange-lang-it-it, open-xchange-lang-ja-jp, open-xchange-lang-nl-nl, open-xchange-lang-pl-pl, open-xchange-online-help-es-es, open-xchange-online-help-nl-nl, open-xchange-online-help-pl-pl, open-xchange-online-help-ja-jp

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
Requires:	open-xchange-publish, open-xchange-publish-basic, open-xchange-publish-infostore-online, open-xchange-publish-json, open-xchange-publish-microformats, open-xchange-subscribe, open-xchange-subscribe-json, open-xchange-subscribe-microformats, open-xchange-subscribe-crawler, open-xchange-templating, open-xchange-subscribe-linkedin, open-xchange-subscribe-facebook


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
Requires:	open-xchange-gui, open-xchange-gui-wizard-plugin-gui, open-xchange-online-help-de-de, open-xchange-online-help-en-us, open-xchange-online-help-fr-fr


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
Requires:	%{alllang}
Requires:	open-xchange-spamhandler-default, open-xchange-admin-soap, open-xchange-easylogin, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui, open-xchange-upsell-generic

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
Requires:	%{alllang}
Requires:	open-xchange-custom-parallels, open-xchange-custom-parallels-gui, open-xchange-spamhandler-spamassassin, open-xchange-admin-soap, open-xchange-easylogin, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui

%description -n open-xchange-meta-parallels
The Open-Xchange Meta package for OX into Parallels integration

Authors:
--------
    Open-Xchange


# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-outlook
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Outlook OXtender
Requires:	open-xchange-outlook-updater, open-xchange-outlook-updater-oxtender2, open-xchange-usm, open-xchange-folder-json

%description -n open-xchange-meta-outlook
The Open-Xchange Meta package for Outlook OXtender

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

%files -n open-xchange-meta-outlook
%defattr(-,root,root)
%doc README.TXT

%changelog
* Wed Feb 09 2011 - choeger@open-xchange.com
 - Bugfix #18276 - new outlook oxtender 2 is not installed
* Wed Nov 24 2010 - choeger@open-xchange.com
 - Bugfix #17556 - Installation on RHEL5 fails, no java installed
   only provide sun-java on centos
* Fri Oct 29 2010 - choeger@open-xchange.com
 - Bugfix #17347 - Outlook OXtender repository not configured correctly
 - Bugfix #17348 - Wrong defaultpackage for component "oxmobility"
* Thu Jun 24 2010 - choeger@open-xchange.com
 - Bugfix #16354 - strange package dependencies for open-xchange-meta-singleserver
 - Bugfix #16000 - Add open-xchange-mail-pushnotify to package dependency list
   in open-xchange-meta-oxucs.
