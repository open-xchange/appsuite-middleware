
Name:           open-xchange-meta
BuildArch:	noarch
#!BuildIgnore: post-build-checks
Version:	@OXVERSION@
%define		ox_release 15
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open-Xchange Meta packages

%define oxcommon open-xchange, open-xchange-core, open-xchange-imap, open-xchange-pop3, open-xchange-smtp, open-xchange-calendar-printing, open-xchange-gui-wizard-plugin, open-xchange-report-client

%define alllang open-xchange-gui-l10n-cs-cz, open-xchange-gui-l10n-es-es, open-xchange-gui-l10n-hu-hu, open-xchange-gui-l10n-it-it, open-xchange-gui-l10n-ja-jp, open-xchange-gui-l10n-lv-lv, open-xchange-gui-l10n-nl-nl, open-xchange-gui-l10n-pl-pl, open-xchange-gui-l10n-sk-sk, open-xchange-gui-l10n-zh-cn, open-xchange-gui-l10n-zh-tw, open-xchange-l10n-cs-cz, open-xchange-l10n-es-es, open-xchange-l10n-hu-hu, open-xchange-l10n-it-it, open-xchange-l10n-ja-jp, open-xchange-l10n-lv-lv, open-xchange-l10n-nl-nl, open-xchange-l10n-pl-pl, open-xchange-l10n-sk-sk, open-xchange-l10n-zh-cn, open-xchange-l10n-zh-tw, open-xchange-online-help-de-de, open-xchange-online-help-en-us, open-xchange-online-help-es-es, open-xchange-online-help-fr-fr, open-xchange-online-help-it-it, open-xchange-online-help-ja-jp, open-xchange-online-help-nl-nl, open-xchange-online-help-pl-pl, open-xchange-online-help-zh-cn, open-xchange-online-help-zh-tw

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-server
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX Backend
Requires:	%{oxcommon}
Requires:       open-xchange, open-xchange-spamhandler

%description -n open-xchange-meta-server
The Open-Xchange Meta package for OX Backend

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-admin
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for User/Group Provisioning
Requires:	open-xchange-admin

%description -n open-xchange-meta-admin
The Open-Xchange Meta package for User/Group Provisioning

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-pubsub
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Publish and Subscribe
Requires:	open-xchange-publish, open-xchange-subscribe


%description -n open-xchange-meta-pubsub
The Open-Xchange Meta package for Publish and Subscribe

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-messaging
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Messaging
Requires:	open-xchange-unifiedmail, open-xchange-messaging


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
Requires:	open-xchange-passwordchange-database, open-xchange-manage-group-resource


%description -n open-xchange-meta-databaseonly
The Open-Xchange Meta package for OX managed via database only

Authors:
--------
    Open-Xchange


# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-mobility
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for Business Mobility
Requires:	open-xchange-eas, open-xchange-usm, open-xchange-help-usm-eas, open-xchange-eas-provisioning-mail, open-xchange-eas-provisioning-gui


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
Requires:	open-xchange-spamhandler-default, open-xchange-admin-soap, open-xchange-easylogin, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui

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
Requires:	open-xchange-parallels, open-xchange-custom-parallels-gui, open-xchange-spamhandler-spamassassin, open-xchange-admin-soap, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui
Conflicts:	open-xchange-admin-plugin-autocontextid, open-xchange-admin-plugin-reseller


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
%package -n	open-xchange-meta-cpanel
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX into cPanel integration
Requires:	%{oxcommon}
Requires:	%{alllang}
Requires:	open-xchange-spamhandler-spamassassin, open-xchange-admin-soap-reseller, open-xchange-authentication-imap, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui, open-xchange-manage-group-resource, open-xchange-gui-wizard-plugin


%description -n open-xchange-meta-cpanel
The Open-Xchange Meta package for OX into cPanel integration

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

%files -n open-xchange-meta-cpanel
%defattr(-,root,root)
%doc README.TXT

%changelog
* Thu Apr 18 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Fri Mar 01 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-07
* Mon Feb 25 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-22
* Fri Feb 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-13
* Tue Jan 29 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-24
* Thu Jan 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-31
* Wed Dec 12 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-04
* Mon Nov 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Carsten Hoeger <choeger@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 06 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.1
* Wed Oct 10 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.0
* Tue Aug 21 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.0
* Thu Aug 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Initial release
