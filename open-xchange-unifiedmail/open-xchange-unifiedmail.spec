%define __jar_repack %{nil}

Name:          open-xchange-unifiedmail
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Extension to combine all mail storage accounts into a virtual single one
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Obsoletes:     open-xchange-unifiedinbox < @OXVERSION@
Provides:      open-xchange-unifiedinbox = @OXVERSION@

%description
This installs the backend extension for Unified Mail. It combines the standard folders of every mail storage account - or mostly every IMAP
account - into a single mail box. E.g. all mails of every INBOX folder of all your mail storage accounts are shown in the INBOX of the
Unified Mail box. Beside the Unified Mail account all your other mail accounts are still shown separately.
You can select which of your mail storage accounts should be combined into Unified Mail.
Within Unified Mail every email gets an additional tag showing you in which mail account this email is located.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
