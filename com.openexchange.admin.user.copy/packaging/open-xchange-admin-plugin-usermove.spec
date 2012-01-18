
# norootforbuild

Name:           open-xchange-admin-plugin-user-copy
#!BuildIgnore: post-build-checks
BuildArch:	noarch
BuildRequires:  ant open-xchange-admin-plugin-hosting-lib >= 6.20.0.0 open-xchange-admin-soap >= 6.20.0.0 open-xchange-user-copy >= 6.20.0.0
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
%define		ox_release 2
Release:	%{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GNU General Public License (GPL)
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#URL:            
Source:         %{name}_%{version}.orig.tar.gz
Summary:        Open Xchange Admin Usercopy Plugin
Requires:       open-xchange-admin-client >= 6.20.0.0
Requires:       open-xchange-admin-plugin-hosting >= 6.20.0.0
Requires:       open-xchange-user-copy >= 6.20.0.0
#
%package -n	open-xchange-admin-plugin-user-copy-soap
Group:          Applications/Productivity
Summary:	The Open Xchange Admin Usercopy SOAP implementation
Requires:       open-xchange-admin-plugin-user-copy-client
Requires:       open-xchange-admin-soap
#
%package -n open-xchange-admin-plugin-user-copy-client
Group:          Applications/Productivity
Summary:        The Open Xchange Admin Usercopy client
Requires:       open-xchange-admin-plugin-user-copy


%description
Open Xchange Admin User Copy Bundle

Authors:
--------
    Open-Xchange

%description -n open-xchange-admin-plugin-user-copy-soap
The Open Xchange Admin Usercopy SOAP implementation

Authors:
--------
    Open-Xchange

%description -n open-xchange-admin-plugin-usercopy-client
The Open Xchange Admin Usercopy client

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
/opt/open-xchange/bundles/*
/opt/open-xchange/etc/admindaemon/osgi/bundle.d/*
/opt/open-xchange/sbin/*
%doc javadoc

%files -n open-xchange-admin-plugin-user-copy-soap
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/com.openexchange.axis2/services
/opt/open-xchange/bundles/com.openexchange.axis2/services/*

%files -n open-xchange-admin-plugin-user-copy-client
%defattr(-,root,root)
%dir /opt/open-xchange/sbin
%dir /opt/open-xchange/lib
/opt/open-xchange/sbin/*
/opt/open-xchange/lib/*

%changelog
* Wed Jan 18 2012 - steffen.templin@open-xchange.com
 - Renamed packages to -user-copy-
* Fri Oct 07 2011 - dennis.sieben@open-xchange.com
 - initial version
