%define __jar_repack %{nil}

Name:           open-xchange-meta
BuildArch:      noarch
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open-Xchange Meta packages

%define all_lang_ui_ox6 open-xchange-gui-l10n-de-de, open-xchange-gui-l10n-cs-cz, open-xchange-gui-l10n-es-es, open-xchange-gui-l10n-hu-hu, open-xchange-gui-l10n-it-it, open-xchange-gui-l10n-ja-jp, open-xchange-gui-l10n-lv-lv, open-xchange-gui-l10n-nl-nl, open-xchange-gui-l10n-pl-pl, open-xchange-gui-l10n-sk-sk, open-xchange-gui-l10n-zh-cn, open-xchange-gui-l10n-zh-tw, open-xchange-online-help-de-de, open-xchange-online-help-en-us, open-xchange-online-help-es-es, open-xchange-online-help-fr-fr, open-xchange-online-help-it-it, open-xchange-online-help-ja-jp, open-xchange-online-help-nl-nl, open-xchange-online-help-pl-pl, open-xchange-online-help-zh-cn, open-xchange-online-help-zh-tw

%define all_lang_ui_appsuite open-xchange-appsuite-help-de-de, open-xchange-appsuite-help-en-gb, open-xchange-appsuite-help-en-us, open-xchange-appsuite-help-es-es, open-xchange-appsuite-help-es-mx, open-xchange-appsuite-help-fr-fr, open-xchange-appsuite-help-it-it, open-xchange-appsuite-help-ja-jp, open-xchange-appsuite-help-nl-nl, open-xchange-appsuite-help-pl-pl, open-xchange-appsuite-help-zh-cn, open-xchange-appsuite-help-zh-tw, open-xchange-appsuite-l10n-ca-es, open-xchange-appsuite-l10n-cs-cz, open-xchange-appsuite-l10n-da-dk, open-xchange-appsuite-l10n-de-de, open-xchange-appsuite-l10n-en-gb, open-xchange-appsuite-l10n-en-us, open-xchange-appsuite-l10n-es-es, open-xchange-appsuite-l10n-es-mx, open-xchange-appsuite-l10n-et-ee, open-xchange-appsuite-l10n-fi-fi, open-xchange-appsuite-l10n-fr-ca, open-xchange-appsuite-l10n-fr-fr, open-xchange-appsuite-l10n-hu-hu, open-xchange-appsuite-l10n-it-it, open-xchange-appsuite-l10n-ja-jp, open-xchange-appsuite-l10n-lv-lv, open-xchange-appsuite-l10n-nl-nl, open-xchange-appsuite-l10n-pl-pl, open-xchange-appsuite-l10n-pt-br, open-xchange-appsuite-l10n-ro-ro, open-xchange-appsuite-l10n-ru-ru, open-xchange-appsuite-l10n-sk-sk, open-xchange-appsuite-l10n-sv-se, open-xchange-appsuite-l10n-tr-tr, open-xchange-appsuite-l10n-zh-cn, open-xchange-appsuite-l10n-zh-tw

%define all_lang_backend open-xchange-l10n-de-de, open-xchange-l10n-cs-cz, open-xchange-l10n-es-es, open-xchange-l10n-hu-hu, open-xchange-l10n-it-it, open-xchange-l10n-ja-jp, open-xchange-l10n-lv-lv, open-xchange-l10n-nl-nl, open-xchange-l10n-pl-pl, open-xchange-l10n-sk-sk, open-xchange-l10n-zh-cn, open-xchange-l10n-zh-tw

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-appsuite-push
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for App Suite push
Requires:       open-xchange-pns-impl
Requires:       open-xchange-pns-transport-websockets

%description -n open-xchange-meta-appsuite-push
The Open-Xchange Meta package for App Suite push

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-server
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX Backend
Requires:       open-xchange-meta-backend
Requires:       open-xchange, open-xchange-spamhandler

%description -n open-xchange-meta-server
The Open-Xchange Meta package for OX Backend

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-pubsub
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for Publish and Subscribe
Requires:       open-xchange-subscribe

%description -n open-xchange-meta-pubsub
The Open-Xchange Meta package for Publish and Subscribe

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-messaging
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for Messaging
Requires:       open-xchange-unifiedmail, open-xchange-messaging

%description -n open-xchange-meta-messaging
The Open-Xchange Meta package for Messaging

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-singleserver
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX on a single server
Requires:       open-xchange-meta-server, open-xchange-meta-gui, open-xchange-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging

%description -n open-xchange-meta-singleserver
The Open-Xchange Meta package for OX on a single server

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-databaseonly
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX managed via database only
Requires:       open-xchange-passwordchange-database, open-xchange-manage-group-resource

%description -n open-xchange-meta-databaseonly
The Open-Xchange Meta package for OX managed via database only

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-mobility
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for Business Mobility
Requires:       open-xchange-eas, open-xchange-usm, open-xchange-eas-provisioning-mail

%description -n open-xchange-meta-mobility
The Open-Xchange Meta package for Business Mobility

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-mobility-ui-ox6
Group:          Applications/Productivity
Summary:        Helper package for Open-Xchange meta package for business mobility
Requires:       open-xchange-help-usm-eas, open-xchange-eas-provisioning-gui
Provides:       open-xchange-meta-mobility-ui

%description -n open-xchange-meta-mobility-ui-ox6
Helper package for Open-Xchange meta package for business mobility

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-mobility-ui-appsuite
Group:          Applications/Productivity
Summary:        Helper package for Open-Xchange meta package for business mobility
Requires:       open-xchange-meta-ui-appsuite
Provides:       open-xchange-meta-mobility-ui

%description -n open-xchange-meta-mobility-ui-appsuite
Helper package for Open-Xchange meta package for business mobility

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-parallels
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX into Parallels integration
Requires:       open-xchange-meta-backend
Requires:       open-xchange-meta-gui
Requires:       %{all_lang_backend}
Requires:       open-xchange-parallels, open-xchange-meta-parallels-ui, open-xchange-spamhandler-spamassassin, open-xchange-admin-soap, open-xchange-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-manage-group-resource
Conflicts:      open-xchange-admin-plugin-autocontextid, open-xchange-admin-plugin-reseller

%description -n open-xchange-meta-parallels
The Open-Xchange Meta package for OX into Parallels integration

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-parallels-ui-ox6
Group:          Applications/Productivity
Summary:        Helper package for Open-Xchange Meta package for Parallels integration
Requires:       open-xchange-parallels-gui
Provides:       open-xchange-meta-parallels-ui

%description -n open-xchange-meta-parallels-ui-ox6
Helper package for Open-Xchange Meta package for Parallels integration

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-parallels-ui-appsuite
Group:          Applications/Productivity
Summary:        Helper package for Open-Xchange Meta package for Parallels integration
Requires:       open-xchange-meta-ui-appsuite
Provides:       open-xchange-meta-parallels-ui

%description -n open-xchange-meta-parallels-ui-appsuite
Helper package for Open-Xchange Meta package for Parallels integration

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-cpanel
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX into cPanel integration
Requires:       open-xchange-meta-backend
Requires:       open-xchange-meta-gui
Requires:       %{all_lang_backend}
Requires:       open-xchange-spamhandler-spamassassin, open-xchange-admin-soap-reseller, open-xchange-authentication-imap, open-xchange-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-manage-group-resource

%description -n open-xchange-meta-cpanel
The Open-Xchange Meta package for OX into cPanel integration

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-ui-ox6
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for the OX6 UI
Provides:       open-xchange-meta-gui
Requires:       %{all_lang_ui_ox6}, open-xchange-gui, open-xchange-gui-wizard-plugin-gui

%description -n open-xchange-meta-ui-ox6
The Open-Xchange Meta package for the OX6 UI

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-ui-ox6-compat
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for the OX6 UI compatible to the old 6.20 meta-gui
Provides:       open-xchange-meta-gui
Requires:       open-xchange-gui, open-xchange-gui-wizard-plugin-gui, open-xchange-gui-l10n-de-de, open-xchange-gui-l10n-en-us, open-xchange-gui-l10n-fr-fr, open-xchange-online-help-de-de, open-xchange-online-help-en-us, open-xchange-online-help-fr-fr

%description -n open-xchange-meta-ui-ox6-compat
The Open-Xchange Meta package for the OX6 UI compatible to the old 6.20 meta-gui

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-ui-appsuite
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for the OX App Suite UI
Provides:       open-xchange-meta-gui
Requires:       %{all_lang_ui_appsuite}, open-xchange-appsuite, open-xchange-appsuite-backend, open-xchange-appsuite-manifest

%description -n open-xchange-meta-ui-appsuite
The Open-Xchange Meta package for the OX App Suite UI

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-meta-backend-ox6
Group:          Applications/Productivity
Summary:        The Open-Xchange Meta package for OX6 backend packages
Provides:       open-xchange-meta-backend
Requires:       open-xchange, open-xchange-core, open-xchange-imap, open-xchange-pop3, open-xchange-smtp, open-xchange-calendar-printing, open-xchange-gui-wizard-plugin, open-xchange-report-client

%description -n open-xchange-meta-backend-ox6
The Open-Xchange Meta package for OX6 backend packages

Authors:
--------
    Open-Xchange

# --- transitional packages --------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-linkedin
Group:          Applications/Productivity
Summary:        Empty transitional package.

%description -n open-xchange-linkedin
Empty transitional package. This package can be removed as the integration of
Open-Xchange with LinkedIn was discontinued.

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------------------
%package -n     open-xchange-calendar-printing
Group:          Applications/Productivity
Summary:        Empty transitional package.

%description -n open-xchange-calendar-printing
Empty transitional package. This package can be removed as the printing logic
was moved to the client-side.

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

%files -n open-xchange-meta-backend-ox6
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-ui-ox6
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-ui-ox6-compat
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-ui-appsuite
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

#%files -n open-xchange-meta-plesk
#%defattr(-,root,root)
#%doc README.TXT

%files -n open-xchange-meta-parallels
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-parallels-ui-ox6
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-parallels-ui-appsuite
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-mobility
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-mobility-ui-ox6
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-mobility-ui-appsuite
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-cpanel
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-linkedin
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-calendar-printing
%defattr(-,root,root)
%doc README.TXT

%changelog
* Mon Jun 17 2019 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.10.3 release
