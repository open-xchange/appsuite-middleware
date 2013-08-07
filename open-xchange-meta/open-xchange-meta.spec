
Name:           open-xchange-meta
BuildArch:	noarch
#!BuildIgnore: post-build-checks
Version:	@OXVERSION@
%define		ox_release 3
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Open-Xchange Meta packages

%define ox6_common open-xchange, open-xchange-core, open-xchange-imap, open-xchange-pop3, open-xchange-smtp, open-xchange-calendar-printing, open-xchange-gui-wizard-plugin, open-xchange-report-client

%define oxcommon open-xchange, open-xchange-core, open-xchange-imap, open-xchange-pop3, open-xchange-smtp, open-xchange-report-client

%define all_lang_ui_ox6 open-xchange-gui-l10n-de-de, open-xchange-gui-l10n-cs-cz, open-xchange-gui-l10n-es-es, open-xchange-gui-l10n-hu-hu, open-xchange-gui-l10n-it-it, open-xchange-gui-l10n-ja-jp, open-xchange-gui-l10n-lv-lv, open-xchange-gui-l10n-nl-nl, open-xchange-gui-l10n-pl-pl, open-xchange-gui-l10n-sk-sk, open-xchange-gui-l10n-zh-cn, open-xchange-gui-l10n-zh-tw, open-xchange-online-help-de-de, open-xchange-online-help-en-us, open-xchange-online-help-es-es, open-xchange-online-help-fr-fr, open-xchange-online-help-it-it, open-xchange-online-help-ja-jp, open-xchange-online-help-nl-nl, open-xchange-online-help-pl-pl, open-xchange-online-help-zh-cn, open-xchange-online-help-zh-tw

%define all_lang_ui_appsuite open-xchange-appsuite-help-de-de, open-xchange-appsuite-help-en-us, open-xchange-appsuite-l10n-cs-cz, open-xchange-appsuite-l10n-de-de, open-xchange-appsuite-l10n-en-us, open-xchange-appsuite-l10n-es-es, open-xchange-appsuite-l10n-es-mx, open-xchange-appsuite-l10n-fr-ca, open-xchange-appsuite-l10n-fr-fr, open-xchange-appsuite-l10n-hu-hu, open-xchange-appsuite-l10n-it-it, open-xchange-appsuite-l10n-ja-jp, open-xchange-appsuite-l10n-lv-lv, open-xchange-appsuite-l10n-nl-nl, open-xchange-appsuite-l10n-pl-pl, open-xchange-appsuite-l10n-ro-ro, open-xchange-appsuite-l10n-sk-sk, open-xchange-appsuite-l10n-zh-cn, open-xchange-appsuite-l10n-zh-tw

%define all_lang_backend open-xchange-l10n-de-de, open-xchange-l10n-cs-cz, open-xchange-l10n-es-es, open-xchange-l10n-hu-hu, open-xchange-l10n-it-it, open-xchange-l10n-ja-jp, open-xchange-l10n-lv-lv, open-xchange-l10n-nl-nl, open-xchange-l10n-pl-pl, open-xchange-l10n-sk-sk, open-xchange-l10n-zh-cn, open-xchange-l10n-zh-tw

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-server
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX Backend
Requires:	open-xchange-meta-backend
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
Requires:	open-xchange-eas, open-xchange-usm, open-xchange-eas-provisioning-mail


%description -n open-xchange-meta-mobility
The Open-Xchange Meta package for Business Mobility

Authors:
--------
    Open-Xchange

%package -n open-xchange-meta-mobility-ui-ox6
Group:      Applications/Productivity
Summary:    Helper package for Open-Xchange meta package for business mobility
Requires:   open-xchange-help-usm-eas, open-xchange-eas-provisioning-gui
Provides:   open-xchange-meta-mobility-ui

%description -n open-xchange-meta-mobility-ui-ox6
Helper package for Open-Xchange meta package for business mobility

Authors:
--------
    Open-Xchange

%package -n open-xchange-meta-mobility-ui-appsuite
Group:      Applications/Productivity
Summary:    Helper package for Open-Xchange meta package for business mobility
Requires:   open-xchange-meta-ui-appsuite
Provides:   open-xchange-meta-mobility-ui

%description -n open-xchange-meta-mobility-ui-appsuite
Helper package for Open-Xchange meta package for business mobility

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
# not used atm
#%package -n	open-xchange-meta-plesk
#Group:          Applications/Productivity
#Summary:	The Open-Xchange Meta package for OX into Plesk integration
#Requires:	%{oxcommon}
#Requires:	%{alllang}
#Requires:	open-xchange-spamhandler-default, open-xchange-admin-soap, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-meta-gui
#
#%description -n open-xchange-meta-plesk
#The Open-Xchange Meta package for OX into Plesk integration
#
#Authors:
#--------
#    Open-Xchange
#

# ----------------------------------------------------------------------------------------------------
%package -n open-xchange-meta-parallels
Group:      Applications/Productivity
Summary:    The Open-Xchange Meta package for OX into Parallels integration
Requires:   open-xchange-meta-backend
Requires:   open-xchange-meta-gui
Requires:   %{all_lang_backend}
Requires:   open-xchange-parallels, open-xchange-meta-parallels-ui, open-xchange-spamhandler-spamassassin, open-xchange-admin-soap, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-manage-group-resource
Conflicts:  open-xchange-admin-plugin-autocontextid, open-xchange-admin-plugin-reseller

%description -n open-xchange-meta-parallels
The Open-Xchange Meta package for OX into Parallels integration

Authors:
--------
    Open-Xchange

%package -n open-xchange-meta-parallels-ui-ox6
Group:      Applications/Productivity
Summary:    Helper package for Open-Xchange Meta package for Parallels integration
Requires:   open-xchange-parallels-gui
Provides:   open-xchange-meta-parallels-ui

%description -n open-xchange-meta-parallels-ui-ox6
Helper package for Open-Xchange Meta package for Parallels integration

Authors:
--------
    Open-Xchange

%package -n open-xchange-meta-parallels-ui-appsuite
Group:      Applications/Productivity
Summary:    Helper package for Open-Xchange Meta package for Parallels integration
Requires:   open-xchange-meta-ui-appsuite
Provides:   open-xchange-meta-parallels-ui

%description -n open-xchange-meta-parallels-ui-appsuite
Helper package for Open-Xchange Meta package for Parallels integration

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
Requires:	open-xchange-meta-backend
Requires:	open-xchange-meta-gui
Requires:	%{all_lang_backend}
Requires:	open-xchange-spamhandler-spamassassin, open-xchange-admin-soap-reseller, open-xchange-authentication-imap, open-xchange-meta-admin, open-xchange-meta-pubsub, open-xchange-meta-messaging, open-xchange-manage-group-resource


%description -n open-xchange-meta-cpanel
The Open-Xchange Meta package for OX into cPanel integration

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-ui-ox6
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for the OX6 UI
Provides:	open-xchange-meta-gui
Requires:	%{all_lang_ui_ox6}, open-xchange-gui, open-xchange-gui-wizard-plugin-gui


%description -n open-xchange-meta-ui-ox6
The Open-Xchange Meta package for the OX6 UI

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-ui-appsuite
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for the OX App Suite UI
Provides:	open-xchange-meta-gui
Requires:	%{all_lang_ui_appsuite}, open-xchange-appsuite, open-xchange-appsuite-backend, open-xchange-appsuite-manifest


%description -n open-xchange-meta-ui-appsuite
The Open-Xchange Meta package for the OX App Suite UI

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-backend-ox6
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX6 backend packages
Provides:	open-xchange-meta-backend
Requires:	%{ox6_common}


%description -n open-xchange-meta-backend-ox6
The Open-Xchange Meta package for OX6 backend packages

Authors:
--------
    Open-Xchange

# ----------------------------------------------------------------------------------------------------
%package -n	open-xchange-meta-backend-appsuite
Group:          Applications/Productivity
Summary:	The Open-Xchange Meta package for OX App Suite backend packages
Provides:	open-xchange-meta-backend
Requires:	%{oxcommon}


%description -n open-xchange-meta-backend-appsuite
The Open-Xchange Meta package for OX App Suite backend packages

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

%files -n open-xchange-meta-backend-appsuite
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-ui-ox6
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-ui-appsuite
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

%files -n open-xchange-meta-outlook
%defattr(-,root,root)
%doc README.TXT

%files -n open-xchange-meta-cpanel
%defattr(-,root,root)
%doc README.TXT

%changelog
* Tue Aug 06 2013 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Second build for patch  2013-07-18
* Mon Jul 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Carsten Hoeger <choeger@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Carsten Hoeger <choeger@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Carsten Hoeger <choeger@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Carsten Hoeger <choeger@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Carsten Hoeger <choeger@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Carsten Hoeger <choeger@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Carsten Hoeger <choeger@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Carsten Hoeger <choeger@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Carsten Hoeger <choeger@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Carsten Hoeger <choeger@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Carsten Hoeger <choeger@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Carsten Hoeger <choeger@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Carsten Hoeger <choeger@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Carsten Hoeger <choeger@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Carsten Hoeger <choeger@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Carsten Hoeger <choeger@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Carsten Hoeger <choeger@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Carsten Hoeger <choeger@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Carsten Hoeger <choeger@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Carsten Hoeger <choeger@open-xchange.com>
prepare for 6.22.0
* Thu Aug 09 2012 Carsten Hoeger <choeger@open-xchange.com>
Initial release
