
# norootforbuild

Name:           open-xchange-admin-plugin-hosting
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-admin-lib >= @OXVERSION@ open-xchange-server >= @OXVERSION@
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
%define		ox_release 0
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin Hosting Plugin
Requires:       open-xchange-admin-plugin-hosting-lib >= @OXVERSION@
Requires:       open-xchange-admin-plugin-hosting-client >= @OXVERSION@
Requires:       open-xchange-admin >= @OXVERSION@
Conflicts:	open-xchange-admin-plugin-context-light
#

%package -n     open-xchange-admin-plugin-hosting-client
Group:          Applications/Productivity
Summary:        The Open Xchange Admin Hosting RMI client library
Requires:       open-xchange-admin-client >= @OXVERSION@


%description -n open-xchange-admin-plugin-hosting-client
The Open Xchange Admin Hosting RMI client library

Authors:
--------
    Open-Xchange

%package -n     open-xchange-admin-plugin-hosting-lib
Group:          Applications/Productivity
Summary:        The Open Xchange Admin Hosting Bundle client library
Requires:       open-xchange-admin-lib >= @OXVERSION@


%description -n open-xchange-admin-plugin-hosting-lib
The Open Xchange Admin Hosting Bundle client library

Authors:
--------
    Open-Xchange

%package -n     open-xchange-admin-plugin-hosting-doc
Group:          Applications/Productivity
Summary:        Documentation for the Open Xchange RMI client library.
Requires:       open-xchange-admin-doc >= @OXVERSION@


%description -n open-xchange-admin-plugin-hosting-doc
Documentation for the Open Xchange RMI client library.

Authors:
--------
    Open-Xchange

%description
Open Xchange Admin Hosting Plugin

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

ant -Dadmin.classpath=%{oxprefix}/bundles/%{adminbundle} \
    -Ddestdir=%{buildroot} -Dprefix=%{oxprefix} \
    -Ddoccorelink=/usr/share/doc/packages/open-xchange-admin-doc/javadoc/doc \
    doc install install-client install-bundle
mv doc javadoc

%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/admindaemon/osgi/bundle.d
%dir /opt/open-xchange/etc/admindaemon/plugin
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
%config(noreplace) /opt/open-xchange/etc/admindaemon/plugin/*

%files -n open-xchange-admin-plugin-hosting-client
%defattr(-,root,root)
%dir /opt/open-xchange/lib/
%dir /opt/open-xchange/sbin
/opt/open-xchange/lib/*
/opt/open-xchange/sbin/*

%files -n open-xchange-admin-plugin-hosting-lib
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*

%files -n open-xchange-admin-plugin-hosting-doc
%defattr(-,root,root)
%doc javadoc
%changelog
* Tue Jun 23 2009 - marcus.klein@open-xchange.com
 - Bugfix #13852: Adding OSGi services for creating and removing genconf, publish and subscribe tables to admin.
* Mon Jun 22 2009 - marcus.klein@open-xchange.com
 - Bugfix #12983: Added check if destination filestore has enough space for another context on moving context.
* Mon Jun 15 2009 - marcus.klein@open-xchange.com
 - Bugfix #13386: Improved exception message if maximum number of context for every database is reached.
* Fri Jun 12 2009 - marcus.klein@open-xchange.com
 - Bugfix #6692: Renamed group 0 to "All users" and group 1 to "Standard group". An update task fixes values in the database.
* Mon Feb 23 2009 - choeger@open-xchange.com
 - Bugfix #13252: oxinstaller broken when system has no FQDN
   give proper error message
* Tue Jan 20 2009 - marcus.klein@open-xchange.com
 - Bugfix #13006: Removed stripping of file:/ protocol in URI of filestore.
* Mon Jan 19 2009 - marcus.klein@open-xchange.com
 - Bugfix #12984: Improved code and exception handling when loading a filestore and its information.
* Fri Jan 09 2009 - marcus.klein@open-xchange.com
 - Bugfix #12871: Improved exception message to give administrator a hint what must be done.
* Mon Jan 05 2009 - marcus.klein@open-xchange.com
 - Bugfix #12873: Changed documentation for the --maxunit option.
 - Bugfix #12872: Closing not close SQL statements.
* Wed Dec 17 2008 - francisco.laguna@open-xchange.com
 - Bugfix #12236: Readable error message when duplicate login mapping is used.
* Mon Oct 13 2008 - marcus.klein@open-xchange.com
 - Bugfix #12234: Closing statements correctly.
* Tue Sep 09 2008 - choeger@open-xchange.com
 - Bugfix ID#9972: Checking if context already exists in target database on context move operation
* Fri Aug 22 2008 - choeger@open-xchange.com
 - Bugfix ID#12040 Context-related command line tools do not honor the
  --access-edit-password/group/resource
* Tue Jul 29 2008 - marcus.klein@open-xchange.com
 - Bugfix #11681: Removed a lot of .toString() in debug messages to prevent
   NullPointerExceptions.
* Mon Jul 28 2008 - holgi@open-xchange.com
  - Bugfix ID#11715 showruntimestats -d not usable for com.openexchange.caching
* Thu Jul 17 2008 - choeger@open-xchange.com
  - Bugfix ID#11572 CLT jmx tools do not work any more when jmx auth is enabled
* Mon Jul 07 2008 - choeger@open-xchange.com
  - Bugfix ID#11500 JMX error when starting admindaemon
    do not set contextclassloader
* Mon Jul 07 2008 - holger.achtziger@open-xchange.com
  - Bugfix ID#11575 OX installer fails if configjump.properties does not exist
* Thu Jul 03 2008 - manuel.kraft@open-xchange.com
  - Bugfix ID#11539 oxreport does not run any more: "java.lang.NoClassDefFoundError: com/openexchange/admin/console/ReportingTool"
* Wed Jul 02 2008 - manuel.kraft@open-xchange.com
  - Bugfix ID#11539 oxreport does not run any more: "java.lang.NoClassDefFoundError: com/openexchange/admin/console/ReportingTool"
* Fri Jun 27 2008 - holgi@open-xchange.com
  - Bugfix ID#11533 added separate cache ports for admin and groupware
* Tue Jun 24 2008 - manuel.kraft@open-xchange.com
  - Bugfix ID#11490 unable to create context using --access-combination-name on commandline
* Mon Jun 09 2008 - dennis.sieben@open-xchange.com
  - Bugfix ID#11358 [L3] Movecontextfilestore doesn't move filestore if context filestore isn't available
* Tue Apr 29 2008 - choeger@open-xchange.com
  - Bugfix ID#11194 getaccesscombinationnameforuser throws NoClassDefFoundError
* Wed Mar 05 2008 - choeger@open-xchange.com
  - Bugfix ID#10414 oxinstaller sets read db connection to the wrong server for master/slave setups
* Tue Feb 12 2008 - dennis.sieben@open-xchange.com
  - Bugfix ID#10894 AJP and general monitoring does not work anymore with showRuntimeStats
* Tue Dec 11 2007 - choeger@open-xchange.com
  - Bugfix ID#10223
    [L3] file plugin/hosting.properties gets overwritten on package update
* Mon Dec 10 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10592 [HEAD] Filestorage leftovers for deleted contexts
  - Bugfix ID#10603 [HEAD] double push back on db connection causes warning log
* Fri Dec 07 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#10577 [HEAD ]admin does breake Database replication
* Mon Oct 29 2007 - choeger@open-xchange.com
  - Bugfix ID#9986 Admin should update schema automatically
* Mon Oct 29 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9974 [HEAD] context deletion not to use any server api calls
* Thu Oct 25 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9949 L3: Filestore directory layout not physically deleted when context
    is removed, only contained files
  - Bugfix ID#9948 No rollback when deleting a context
* Wed Sep 26 2007 - choeger@open-xchange.com
  - Bugfix ID#9614 initconfigdb "mysqladmin: connect to server at 'localhost'
  failed" when database not local
* Tue Sep 25 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9569 showruntimestats shows "statistictools" as default usage
* Mon Sep 10 2007 - choeger@open-xchange.com
- Bugfix ID#8949 Unable to deinstall admin-plugin-hosting package when removing depending
  package (the fix from 2007-08-20 does not really work)
* Wed Sep 05 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9254 showruntimestats gives NullPointerException
* Wed Aug 22 2007 - choeger@open-xchange.com
  - Bugfix ID#8989 generatempasswd no newline in output
  - Bugfix ID#8991 initconfigdb, return code is always 0
  - Bugfix ID#8853 'listcontexts' searchpattern only works for context id
  - Bugfix ID#9026 listcontextsbyfilestore does not print a error when the given fs does not exist
* Wed Aug 22 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#9023 createcontext name should be added to lmappings
* Tue Aug 21 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8543 rename of movedatabasecontext and movefilestorecontext
  - Bugfix ID#9004 unregisterserver operation by name missing
  - Bugfix ID#8993 clts should all be singular
  - Bugfix ID#9007 Context login mappings not validated
  - Bugfix ID#8994 oxinstaller --master-pass should not be needed if --disableauth is in use
* Mon Aug 20 2007 - choeger@open-xchange.com
  - Bugfix ID#8949 Unable to deinstall admin-plugin-hosting package when removing depending
  package
* Thu Aug 16 2007 - choeger@open-xchange.com
  - Bugfix ID#8915 Classpath problems with CLTs
* Thu Aug 16 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8917 CLT 'showruntimestats' does not give any output
* Tue Aug 14 2007 - choeger@open-xchange.com
  - Bugfix ID#8822 'generatepassword' creates curious output
* Thu Aug 09 2007 - choeger@open-xchange.com
  - Bugfix ID#8623 oxinstaller: switch needed to turn on/of context authentication
* Tue Aug 07 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8629 movefilestorecontext StringIndexOutOfBoundsException
  - Bugfix ID#8593 Operations by name not possible
* Mon Aug 06 2007 - choeger@open-xchange.com
  - Bugfix ID#8642 "listusers" command line tool output is limited to three digits
    as a side effect of now dynamically determining the widest row, this is also fixed
* Thu Aug 02 2007 - choeger@open-xchange.com
  - Bugfix ID#8651 no access must be the default when not specifying access on commandline
    using lowest set of access options as default (webmail)
* Tue Jul 31 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8597 [DEV] searchContextByFilestoreId,searchContextByFilestore and
    searchContextByDatabase must only return contexts bound to specific SERVER_NAME
* Mon Jul 30 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8575 CLT createcontext: It should be possible to create a mapping between contextID
  and context name during createcontext and not only on changecontext
* Mon Jul 30 2007 - choeger@open-xchange.com
  - Bugfix ID#8592 Misleading server response if "listuser" doesn't find any match
* Thu Jul 26 2007 - dennis.sieben@open-xchange.com
  - Bugfix ID#8553 CLT: After running CLT no reasonable message on console appear for user
* Wed Jul 25 2007 - choeger@open-xchange.com
  - Bugfix ID#8550 generatempasswd not developed to be used by humans
  made generatempasswd usable by humans... :-)
* Wed Jul 11 2007 - manuel.kraft@open-xchange.com
  -  Bugfix ID#8379 listcontext, lmapping not in csv output
* Mon Jul 09 2007 - manuel.kraft@open-xchange.com
  -  Bugfix ID#7302 changecontext is missing
* Fri Jun 29 2007 - dennis.sieben@open-xchange.com
  -  Bugfix ID#8171 need for a tool that does reset the jmx max values
* Thu Jun 21 2007 - manuel.kraft@open-xchange.com
  -  Bugfix ID#7675 LTs to manage users can not deal with modules
* Tue Jun 12 2007 - dennis.sieben@open-xchange.com
  -  Bugfix ID#7657 console clients check extension errors in the wrong place
* Tue May 29 2007 - choeger@open-xchange.com
  - Bugfix ID#7595 Groups member in several contexts are deleted on context delete
