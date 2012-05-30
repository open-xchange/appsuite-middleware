
# norootforbuild

Name:           open-xchange-admin-plugin-reseller
#!BuildIgnore: post-build-checks
BuildArch:	noarch
BuildRequires:  ant open-xchange-admin-plugin-hosting-lib >= 6.20.0.0 open-xchange-admin-soap >= 6.20.0.0 open-xchange-admin-plugin-autocontextid >= 6.20.0.0
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
Summary:        Open Xchange Admin Reseller Plugin
Requires:       open-xchange-admin-client >= 6.20.0.0
Requires:       open-xchange-admin-plugin-hosting >= 6.20.0.0
Requires:       open-xchange-admin-plugin-autocontextid >= 6.20.0.0 
Requires:       open-xchange-admin-plugin-autocontextid-client >= 6.20.0.0 
#
%package -n	open-xchange-admin-plugin-reseller-soap
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Reseller SOAP server
Requires:       open-xchange-admin-plugin-reseller
Requires:       open-xchange-admin-soap

%description
Open Xchange Admin Reseller Plugin

Authors:
--------
    Open-Xchange

%description -n open-xchange-admin-plugin-reseller-soap
Open Xchange Admin Reseller Plugin SOAP server parts

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

%define adminbundle	com.openexchange.admin.jar
%define oxprefix	/opt/open-xchange
%define adminhostingbundle com.openexchange.admin.plugin.hosting.jar

ant -Dadmin.classpath=%{oxprefix}/bundles/%{adminbundle} \
    -Ddestdir=%{buildroot} -Dprefix=%{oxprefix} \
    -Dadminhosting.classpath=%{oxprefix}/bundles/%{adminhostingbundle} \
    -Ddoccorelink=/usr/share/doc/packages/open-xchange-admin-doc/javadoc/doc \
    doc install install-client install-soap
mv doc javadoc


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/mysql
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/lib
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/etc/admindaemon/mysql/*
/opt/open-xchange/sbin/*
/opt/open-xchange/lib/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/*
%doc javadoc

%files -n open-xchange-admin-plugin-reseller-soap
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
/opt/open-xchange/bundles/com.openexchange.axis2/services/*

%changelog
* Wed Oct 19 2011 - choeger@open-xchange.com
 - Use the new stabilized SOAP API from SoftwareChange_Request-860
   This change does NOT introduce any changes to users using the API with perl or php.
   However, users using the WSDL to generate code must regenerate the code!
* Wed Sep 07 2011 - choeger@open-xchange.com
 - RMI API Change: Restrictions will now be exchanged as Java array and no longer as HashSet
 - New Feature: Subadmin can create further subadmins (extra permission required, maxdepth: 1) 
 - add new restrictions using new updaterestrictions commandline
* Wed Sep 07 2011 - thorben.betten@open-xchange.com
 - Fix for bug #20044: Fixed regular expression to look-up database name
* Mon Aug 08 2011 - choeger@open-xchange.com
 - Bugfix #20032 - SOAP api breaks when userAttributesForSOAP are set within user object
* Mon May 16 2011 - dennis.sieben@open-xchange.com
 - Bugfix #19253 - com.openexchange.admin.reseller.console.extensionimpl.ContextConsoleChangeImpl
                   ignores RMI_HOSTNAME environment
* Tue May 10 2011 - choeger@open-xchange.com
 - Bugfix #19102: reseller bundle: unable to change restrictions on commandline
 - Bugfix #19135: Reseller Plugin: Exception occurred while trying to invoke service
   method createModuleAccessByName
* Thu Apr 07 2011 - dennis.sieben@open-xchange.com
 - Bugfix #18883 - changecontext does not work when reseller package is installed
* Thu Apr 07 2011 - choeger@open-xchange.com
 - Bugfix #18881: listcontext for masteradmin does not work when reseller plugin installed
* Tue Mar 01 2011 - marcus.klein@open-xchange.com
 - Bugfix #18465: Compiling sources everywhere to Java5 compatible class files.
* Mon Nov 02 2009 - marcus.klein@open-xchange.com
 - Bugfix #14510: Refusing start of administration daemon if master credentials are not configured properly.
