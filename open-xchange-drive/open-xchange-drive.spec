%define __jar_repack %{nil}

Name:           open-xchange-drive
BuildArch:      noarch
Version:        @OXVERSION@
%define         ox_release 0
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        Server module for Open-Xchange Drive file synchronization
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@

%description
Server module for Open-Xchange Drive file synchronization

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
cp -rv --preserve=all ./opt ./usr %{buildroot}/

%clean
%{__rm} -rf %{buildroot}

%post
. /opt/open-xchange/lib/oxfunctions.sh

# prevent bash from expanding, see bug 13316
GLOBIGNORE='*'

%posttrans
. /opt/open-xchange/lib/oxfunctions.sh

# SCR-392
# don't wrap in ox_scr_todo as it might have to reexecute after downgrade or
# configs will be lost
orig_pfile=/opt/open-xchange/etc/drive.properties
save_pfile=${orig_pfile}.rpmsave
if [ -e ${save_pfile} ] && [ ! -e ${orig_pfile} ]
then
  echo "Keeping ${save_pfile} as ${orig_pfile} due to modifications"
  mv ${save_pfile} ${orig_pfile}
fi

%files
%defattr(-,root,root)
/opt/open-xchange/
%dir %attr(750,open-xchange,open-xchange) /opt/open-xchange/osgi/
%config(noreplace) /opt/open-xchange/etc/contextSets/drive.yml
/usr/share
%doc /usr/share/doc/open-xchange-drive/properties/

%changelog
* Mon Jun 17 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Tobias Friedrich <tobias.friedrich@open-xchange.com>
First preview for 7.10.2 release
