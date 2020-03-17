%define __jar_repack %{nil}

Name:          open-xchange-filestore-s3
BuildArch:     noarch
Version:       @OXVERSION@
%define        ox_release 0
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Filestore implementation storing files in a S3 API compatible storage
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for storing files in a S3 API compatible storage.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions /opt/open-xchange/etc/filestore-s3.properties root:open-xchange 640
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'
    PFILE=/opt/open-xchange/etc/filestore-s3.properties

    # SoftwareChange_Request-2581
    ox_add_property com.openexchange.filestore.s3.[filestoreID].signerOverride S3SignerType $PFILE

    # SoftwareChange_Request-3477
    ox_add_property com.openexchange.filestore.s3.[filestoreID].chunkSize 5MB $PFILE
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-s3.properties
%config(noreplace) /opt/open-xchange/etc/*
/usr/share
%doc /usr/share/doc/open-xchange-filestore-s3/properties/

%changelog
* Mon Jun 17 2019 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.10.3 release
