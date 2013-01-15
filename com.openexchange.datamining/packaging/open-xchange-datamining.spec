%define	       configfiles     configfiles.list
# norootforbuild

Name:           open-xchange-datamining
BuildArch:	noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: java-devel >= 1.6.0
BuildRequires: open-xchange-osgi >= @OXVERSION@
BuildRequires: open-xchange-core >= @OXVERSION@
Version:       @OXVERSION@
%define        ox_release 3
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0 
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/            
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The Open-Xchange Datamining Tool
Requires:      open-xchange-osgi >= @OXVERSION@
Requires:      open-xchange-core >= @OXVERSION@


%description
The Open-Xchange datamining tool

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build


%install
export NO_BRP_CHECK_BYTECODE_VERSION=true

ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}


%clean
%{__rm} -rf %{buildroot}

%post

%files -f %{configfiles}

%defattr(-,root,root)
%dir /opt/open-xchange/lib
%dir /opt/open-xchange/sbin

/opt/open-xchange/lib
/opt/open-xchange/sbin

%changelog
