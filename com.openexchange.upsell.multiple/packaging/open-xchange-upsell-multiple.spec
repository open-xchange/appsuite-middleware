
# norootforbuild

Name:           open-xchange-upsell-multiple
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant ant-nodeps open-xchange-gui open-xchange-server open-xchange-admin-client open-xchange-admin-plugin-hosting-client  open-xchange-config-cascade
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
Version:        @OXVERSION@
%define         ox_release 1
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        The multiple Open-Xchange upsell multiple bundle
Requires:       open-xchange-gui
#

%description
The multiple Open-Xchange upsell layer bundle

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

ant -Dguiprefix=%{docroot}/ox6 -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/groupware/settings/upsell-multiple-gui.properties
%config(noreplace) /opt/open-xchange/etc/groupware/upsell.properties
%config(noreplace) /opt/open-xchange/etc/groupware/upsell_mail_body_ox_enduser.tmpl
%config(noreplace) /opt/open-xchange/etc/groupware/upsell_mail_body_ox_enduser.tmpl_de_DE
%config(noreplace) /opt/open-xchange/etc/groupware/upsell_mail_subject_ox_enduser.tmpl
%config(noreplace) /opt/open-xchange/etc/groupware/upsell_mail_subject_ox_enduser.tmpl_de_DE
%changelog
* Mon Sep 12 2011 - choeger@open-xchange.com
  - Bugfix #20290 - upsell-multiple-gui.properties in wrong package
* Wed May 11 2011 - dennis.sieben@open-xchange.com
  - Bugfix #19191 - NPE in upsell multiple if method is set to direct
* Wed Sep 22 2010 - manuel@open-xchange.com
Initial
