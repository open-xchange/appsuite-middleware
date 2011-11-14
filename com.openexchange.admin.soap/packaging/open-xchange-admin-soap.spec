
# norootforbuild

Name:           open-xchange-admin-soap
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-admin-client >= @OXVERSION@ open-xchange-admin-plugin-hosting-client >= @OXVERSION@ open-xchange-common >= @OXVERSION@ perl
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
%define		ox_release 31
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin SOAP API
Requires:       open-xchange-admin-client >= @OXVERSION@
Requires:	open-xchange-admin-plugin-hosting-client >= @OXVERSION@
Requires:	open-xchange-axis2 >= @OXVERSION@

%description
Open Xchange Admin SOAP API

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -Ddestdir=%{buildroot} \
	-Ddoccorelink=/usr/share/doc/packages/open-xchange-admin-doc/javadoc/doc \
	-Ddochostinglink=/usr/share/doc/packages/open-xchange-admin-plugin-hosting-doc/javadoc/doc \
	install doc


%clean
%{__rm} -rf %{buildroot}

%post

if [ ${1:-0} -eq 2 ]; then
   # only when updating
   . /opt/open-xchange/etc/oxfunctions.sh

   # prevent bash from expanding, see bug 13316
   GLOBIGNORE='*'

   # SoftwareChange_Request-540
   # -----------------------------------------------------------------------
   pfile=/opt/open-xchange/etc/admindaemon/plugin/open-xchange-admin-soap.properties
   if ! ox_exists_property LOCK_WAIT_TIME $pfile; then
      ox_set_property LOCK_WAIT_TIME 10 $pfile
   fi
fi

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
%dir /opt/open-xchange/etc/admindaemon/plugin
%dir /opt/open-xchange/lib
/opt/open-xchange/bundles/com.openexchange.axis2/services/*
/opt/open-xchange/lib/soaprmimapper.jar
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/open-xchange-admin-soap.properties
%doc docs
%changelog
* Tue Oct 18 2011 - choeger@open-xchange.com
 - SoftwareChange_Request-860: Stabilized SOAP API
   This change does NOT introduce any changes to users using the API with perl or php.
   However, users using the WSDL to generate code must regenerate the code!
* Wed Oct 05 2011 - choeger@open-xchange.com
 - Bugfix #20456 - [L3] SOAP interface broken (all ModuleAccess functions)
* Mon Aug 08 2011 - choeger@open-xchange.com
 - Bugfix #20032 - SOAP api breaks when userAttributesForSOAP are set within user object
* Fri Mar 25 2011 - choeger@open-xchange.com
 - Bugfix #18761 - [L3] SOAP interface broken (most User and Context operations)
* Tue Mar 01 2011 - marcus.klein@open-xchange.com
 - Bugfix #18465: Compiling sources everywhere to Java5 compatible class files.
* Wed Dec 15 2010 - dennis.sieben@open-xchange.com
 - Bugfix #17824: [L3] Operation with two or more SOAP Client leads to race condition errors
   - Added config option to define lock wait time
* Thu Nov 12 2009 - dennis.sieben@open-xchange.com
 - Bugfix #14846: deletecontext call via soap sends "reconnect to rmi service" failed error
   - removed throw line
* Mon Jul 27 2009 - marcus.klein@open-xchange.com
 - Bugfix #14213: Setting configuration file permissions to reduce readability to OX processes.
* Tue Jun 02 2009 - dennis.sieben@open-xchange.com
 - Bugfix #13796: soap interface throws error "reconnect to rmi service" on first call .
     Implemented a second call after the reconnect
