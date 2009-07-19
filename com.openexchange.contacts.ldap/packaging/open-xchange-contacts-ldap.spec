
# norootforbuild

Name:           open-xchange-contacts-ldap
BuildArch:	    noarch
#!BuildIgnore:  post-build-checks
BuildRequires:  ant open-xchange-common open-xchange-global open-xchange-server
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
%define		ox_release 6
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        This bundle provides a global LDAP address book
Requires:       open-xchange-common open-xchange-global open-xchange-server
%description
This bundle provides a global LDAP address book

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -Ddestdir=%{buildroot} -Dprefix=/opt/open-xchange install


%clean
%{__rm} -rf %{buildroot}



%files
%defattr(-,root,root)
%dir /opt/open-xchange/etc/groupware/osgi/bundle.d/
/opt/open-xchange/etc/groupware/osgi/bundle.d/*
/opt/open-xchange/bundles/com.openexchange.contacts.ldap.jar
%dir /opt/open-xchange/etc/groupware/contacts-ldap
/opt/open-xchange/etc/groupware/contacts-ldap/*
%changelog
* Mon Jul 13 2009 - dennis.sieben@open-xchange.com
  - Bugfix #14151 Contacts-ldap currently concatenates multi-value attributes
    this must be changed
    - Removed concatenation - now taking the first value
* Fri Jul 10 2009 - dennis.sieben@open-xchange.com
  - Bugfix #14148 contact list is not sorted by name in contacts-ldap
    - Distributionlist now have a sur_name
* Thu Jul 09 2009 - dennis.sieben@open-xchange.com
  - Bugfix #14137 contacts-ldap must provide an option to deal with referrals
    - Added new property value to set referrals behaviour
  - Bugfix #14138 Fix for groups without members on ADS with contacts-ldap
    - Added catch to ignore this exceptions
* Mon Jun 22 2009 - dennis.sieben@open-xchange.com
  - Bugfix #13920 Unable to get public LDAP folders to Outlook
    - Now returning a SearchIterator in getDeletedContactsInFolder
* Thu Jun 18 2009 - dennis.sieben@open-xchange.com
  - Bugfix #13926 gal bundle: java.lang.Exception: The given value for authtype
    "%s" is not a possible one
    - Changed text to "The directory "%s" is not a context identifier."
* Tue Jun 16 2009 - dennis.sieben@open-xchange.com
  - Bugfix #13892 contacts-ldap bundle contains documentation as odt format at
   the sources
   - Removed documentation as it is now contained in the Installation and
     Administrator documentation
* Tue Jun 16 2009 - dennis.sieben@open-xchange.com
  - Bugfix #13909 NPE when contacts-ldap is enabled to access distribution
    lists (ADS)
    - Surrounded code segment with if
* Mon May 11 2009 - dennis.sieben@open-xchange.com
  - Implemented distributionlist
* Thu Apr 23 2009 - dennis.sieben@open-xchange.com
  - Bugfix #13539 Search field in global LDAP contact folder does not work
