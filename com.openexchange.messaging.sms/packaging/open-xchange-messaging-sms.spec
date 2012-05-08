
# norootforbuild

Name:           open-xchange-messaging-sms
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@ open-xchange-global >= @OXVERSION@ open-xchange-server >= @OXVERSION@ open-xchange-messaging >= @OXVERSION@ open-xchange-messaging-generic >= @OXVERSION@
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
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        This bundle provides a new messaging interface for SMS services
Requires:       open-xchange-common >= @OXVERSION@
Requires:       open-xchange-global >= @OXVERSION@
Requires:       open-xchange >= @OXVERSION@
Requires:       open-xchange-messaging >= @OXVERSION@

%package -n     open-xchange-messaging-sms-gui
Group:          Applications/Productivity
Summary:        General Messaging SMS GUI Bundle
Requires:       open-xchange-messaging-sms-gui-theme >= @OXVERSION@ open-xchange-gui >= @OXVERSION@


%description -n open-xchange-messaging-sms-gui
General Messaging SMS GUI Bundle.

Authors:
--------
    Open-Xchange

%package -n     open-xchange-messaging-sms-gui-theme-default
Group:          Applications/Productivity
Summary:        General Messaging SMS GUI Themes Bundle
Requires:       open-xchange-gui >= @OXVERSION@
Requires:       open-xchange-messaging-sms-gui >= @OXVERSION@
Provides:       open-xchange-messaging-sms-gui-theme

%description -n open-xchange-messaging-sms-gui-theme-default
General Messaging SMS GUI Themes Bundle.

Authors:
--------
    Open-Xchange


%description
This bundle provides a new messaging interface for SMS services

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%if 0%{?rhel_version} || 0%{?fedora_version}
%define docroot /var/www/html
%else
%define docroot /srv/www/htdocs
%endif

#export ANT_OPTS="-Dfile.encoding=UTF-8 -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl"
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange -Dguiprefix=%{docroot}/ox6 install installGui installGuiTheme


%post

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/bundles/com.openexchange.messaging.sms.jar

%files -n open-xchange-messaging-sms-gui
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.messaging.sms
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/lang/*
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/*.js

%files -n open-xchange-messaging-sms-gui-theme-default
%defattr(-,root,root)
%dir %{docroot}/ox6/plugins/com.openexchange.messaging.sms/images
%{docroot}/ox6/plugins/com.openexchange.messaging.sms/images/*

%changelog
* Wed Apr 11 2012 - stefan.preuss@open-xchange.com
  - Fixed bug #21897 : [L3] SMS receiver number not checked
* Tue Mar 27 2012 - viktor.pracht@open-xchange.com
  - Implemented story 27072265 : Feature drop-down in SMS dialog's From field
* Mon Mar 26 2012 - viktor.pracht@open-xchange.com
  - Fixed bug #21846 : [L3] sending SMS not possible
* Wed Mar 21 2012 - viktor.pracht@open-xchange.com
  - Fixed bug #21817 : [L3] sms textinput area can be resized without limit
* Tue Mar 20 2012 - choeger@open-xchange.com
  - Fixed bug #21816 : [L3] smslimit feature does not work
* Wed Feb 29 2012 - choeger@open-xchange.com
  - SCR973: optionally limit number of sms and/or recipients
* Tue Feb 28 2012 - viktor.pracht@open-xchange.com
  - Implemented story 24694921 : extensions of the recipient field
  - Implemented story 24695101 : layout extensions
* Tue Feb 28 2012 - choeger@open-xchange.com
  - SCR970: optionally disable the signature option
* Fri Jan 13 2012 - viktor.pracht@open-xchange.com
  - Fixed bug #20965 : [L3] sms: pull down available even if there are no choices
* Tue Dec 20 2011 - viktor.pracht@open-xchange.com
  - Fixed bug #21032 : [L3] sms text aera not limited to max allowed chars
* Fri Oct 14 2011 - stefan.preuss@open-xchange.com
  - Fixed bug #20589 : [IE8][L3] messaging sms not working if https is in use and captcha enabled
* Fri Sep 23 2011 - dennis.sieben@open-xchange.com
  - Fixed bug #20451 : Wrong RPM tags for open-xchange-messaging-sms-gui-theme-default
* Mon Jul 25 2011 - viktor.pracht@open-xchange.com
  - Fixed stale character count when opening the SMS window multiple times
* Thu Jul 21 2011 - dennis.sieben@open-xchange.com
  - Package split to support different icons
* Wed Jul 20 2011 - viktor.pracht@open-xchange.com
  - TA7478 for US6645 : Add MMS functionality to the SMS plugin
* Tue Jul 12 2011 - viktor.pracht@open-xchange.com
  - Added support for built-in reCAPTCHA languages
* Wed Jun 29 2011 - viktor.pracht@open-xchange.com
  - Added SMS icons
* Tue Jun 28 2011 - viktor.pracht@open-xchange.com
  - Added reCAPTCHA support
* Tue May 17 2011 - viktor.pracht@open-xchange.com
  - Fixed bug #19244 : IE7: Login hangs at 90%
* Tue May 10 2011 - viktor.pracht@open-xchange.com
  - Fixed bug #19177 : SMS Plugin should not show "sender dropdown" when empty
* Mon Mar 14 2011 - benjamin.otterbach@open-xchange.com
  - Initial
