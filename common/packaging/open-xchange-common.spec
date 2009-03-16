
# norootforbuild


Name:           open-xchange-common
BuildArch: 	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant
%if 0%{?suse_version}
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
Version:	6.8.1
Release:	3
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        open-xchange common jar files
#

%description
jar files and OSGi bundles commonly used by all open-xchange packages

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install

export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -Ddestdir=%{buildroot} install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc
%dir /opt/open-xchange/etc/*/osgi/bundle.d
/opt/open-xchange/etc/*/osgi/bundle.d/*
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/oxfunctions.sh
%doc ChangeLog

%changelog
* Mon Mar 16 2009 - choeger@open-xchange.com
 - Bugfix ID#13385: allpluginsloaded does not work with SP5U1RC1
   added missing @start to commons logging
* Tue Feb 03 2009 - choeger@open-xchange.com
 - Bugfix ID#12762 "A fragment bundle cannot be started" messages at
   admin/groupware osgi log
* Fri Nov 07 2008 - choeger@open-xchange.com
 - Bugfix ID#12461 Import servlet does not work
   config.ini now regenerated with every restart
* Thu May 08 2008 - choeger@open-xchange.com
 - Bugfix ID#11245 config.ini shoudn't be only created if the number of bundle
 in bundles.d directory are changed
* Wed Apr 30 2008 - choeger@open-xchange.com
 - Bugfix ID#11236 ox common functions shell script does not run on debian sarge
* Wed Jan 09 2008 - thorben.betten@open-xchange.com
 - Bugfix #10214: Using "Java port of Mozilla charset detector" to guess
   proper charset for uploaded files
* Tue Sep 25 2007 - dennis.sieben@open-xchange.com
  - Removed Jars for usage of commons-configuration.
  - Removed sources and javadoc for commons-collections
* Mon Sep 10 2007 - choeger@open-xchange.com
 - Bugfix ID#8265 open-xchange-common package shows description "openexchange"
* Fri Aug 31 2007 - thorben.betten@open-xchange.com
 - Bugfix #7862/#9125: Adding library with additional charsets to Java VM
   to solve encoding problems with messages which uses java-foreign charset
   encodings
* Wed Aug 29 2007 - dennis.sieben@open-xchange.com
  - Added Jars for usage of commons-configuration.
  - Added sources and javadoc for commons-collections
* Mon Jul 23 2007 - marcus.klein@open-xchange.com
 - Removed Suns BASE64 en/decoder.
* Thu Jun 28 2007 - dennis.sieben@open-xchange.com
  - updated ldb-client
* Mon Jun 25 2007 - choeger@open-xchange.com
  - Bugfix ID#7972 Fresh install - admin cannot login
    replace official org.eclipse.osgi with own build containing fix from
    https://bugs.eclipse.org/bugs/show_bug.cgi?id=121737
* Tue Jun 19 2007 - dennis.sieben@open-xchange.com
  - added ant.jar for convertion methods used in license plugin
* Fri Jun 01 2007 - dennis.sieben@open-xchange.com
  - added ldb-client
* Tue May 15 2007 - marcus.klein@open-xchange.com
  - Added 3rd party licenses.
* Wed Apr 25 2007 - dennis.sieben@open-xchange.com
  - removed commons-cli
* Thu Apr 19 2007 - choeger@open-xchange.com
  - added log4j
* Thu Apr 12 2007 - thorben.betten@open-xchange.com
 - New commons-fileupload.jar library v1.2
 - Added needed jars from apache commons: commons-io.jar v1.3.1,
   commons-lang.jar v2.3
* Thu Apr 12 2007 - dennis.sieben@open-xchange.com
 - Added needed jars for admin daemon license plugin
* Thu Mar 29 2007 - choeger@open-xchange.com
  - initial ChangeLog
