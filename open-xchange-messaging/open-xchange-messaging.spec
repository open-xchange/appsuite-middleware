%define __jar_repack %{nil}
%define        configfiles     configfiles.list

Name:           open-xchange-messaging
BuildArch:      noarch
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend messaging extension
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@
Requires:       open-xchange-oauth >= @OXVERSION@
Requires:       open-xchange-xerces
Provides:       open-xchange-messaging-generic = %{version}
Provides:       open-xchange-messaging-json = %{version}
Provides:       open-xchange-messaging-rss = %{version}
Provides:       open-xchange-messaging-twitter = %{version}
Provides:       open-xchange-twitter = %{version}
Obsoletes:      open-xchange-messaging-generic < %{version}
Obsoletes:      open-xchange-messaging-json < %{version}
Obsoletes:      open-xchange-messaging-rss < %{version}
Obsoletes:      open-xchange-messaging-twitter < %{version}
Obsoletes:      open-xchange-twitter < %{version}

%description
Adds the feature to use messaging services to the backend installation.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(twitter)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

%clean
%{__rm} -rf %{buildroot}

%files -f %{configfiles}
%defattr(-,root,root)
/opt/open-xchange
/usr/share
%doc /usr/share/doc/open-xchange-messaging/properties/

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
