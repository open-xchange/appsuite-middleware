
# norootforbuild

Name:           open-xchange-report-client
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires:  ant open-xchange-common >= @OXVERSION@, open-xchange-server >= @OXVERSION@, open-xchange-admin-lib >= @OXVERSION@, open-xchange-admin-plugin-hosting-lib >= @OXVERSION@
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
%define         ox_release 11
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open-Xchange reporting client.
Requires:       open-xchange-common >= @OXVERSION@, open-xchange-server >= @OXVERSION@, open-xchange-admin >= @OXVERSION@, open-xchange-admin-plugin-hosting >= @OXVERSION@
#

%description
Open-Xchange user action tracking.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -Dlib.dir=/opt/open-xchange/lib -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%config(noreplace) /opt/open-xchange/etc/groupware/reportclient.properties
%dir /opt/open-xchange/etc/groupware/
%dir /opt/open-xchange/bundles/
%dir /opt/open-xchange/sbin/
/opt/open-xchange/etc/groupware/*
/opt/open-xchange/bundles/*
/opt/open-xchange/sbin/*
%changelog
* Tue May 24 2011 - marcus.klein@open-xchange.com
 - Bugfix #17858: Writing now a stack trace if a runtime exception occurs.
* Thu Mar 10 2011 - benjamin.otterbach@open-xchange.com
 - Bugfix #18588: Report client requests wrong values for OLOX2 reporting
   - Changed request value from regex to wildcard
* Tue Dec 21 2010 - benjamin.otterbach@open-xchange.com
  - Changes for bugfix #17859
* Fri May 28 2010 - benjamin.otterbach@open-xchange.com
  - Bugfix #15190: Report Server does not add a separator to multiple license keys
    - Added missing seperator to JSON string
* Thu Apr 01 2010 - benjamin.otterbach@open-xchange.com
  - Bugfix #15754: Report Client does not send reports with latest HEAD
    - Added missing Apache library to report script
* Wed Mar 31 2010 - benjamin.otterbach@open-xchange.com
  - Bugfix #15616: Report client does not work through a HTTP proxy which requires authentication
    - Fixed broken HTTPS proxy handling
* Thu Mar 04 2010 - benjamin.otterbach@open-xchange.com
  - Bugfix #15546: Broken start script in report client package
    - Fixed the wrong property directory value
* Thu Feb 18 2010 - benjamin.otterbach@open-xchange.com
  - Added support for using a proxy server
* Thu Dec 03 2009 - dennis.sieben@open-xchange.com
  - Bugfix #15016: Open-Xchange6 report client fails with java exception
    - Added missing jar for common-cli 
