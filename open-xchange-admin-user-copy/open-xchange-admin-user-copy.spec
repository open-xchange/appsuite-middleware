%define __jar_repack %{nil}

Name:          open-xchange-admin-user-copy
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Extension to copy users into other contexts
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-user-copy = %{version}
Obsoletes:     open-xchange-admin-plugin-user-copy < %{version}
Provides:      open-xchange-admin-plugin-user-copy-client = %{version}
Obsoletes:     open-xchange-admin-plugin-user-copy-client < %{version}
Provides:      open-xchange-user-copy = %{version}
Obsoletes:     open-xchange-user-copy < %{version}

%description
This package installs administrative OSGi bundles that provide the extension to copy a user into another context. This is mainly used to
combine several users into the same context. To complete the move of a user, the user can be deleted in the source context after copying
it to the destination context.
This extension only copies all the private data of a user. All public information in a context does not belong to any user and therefore it is
not copied at all. The sharing information of the private data of a user needs to be removed.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/
(cd %{buildroot}/opt/open-xchange/lib/ && ln -s ../bundles/com.openexchange.admin.user.copy.rmi.jar)

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*


%changelog
* Mon Jun 17 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.10.3 release
