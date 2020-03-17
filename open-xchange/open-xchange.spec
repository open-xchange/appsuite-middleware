%define __jar_repack %{nil}
%define use_systemd (0%{?rhel} && 0%{?rhel} >= 7) || (0%{?suse_version} && 0%{?suse_version} >=1210)

Name:             open-xchange
BuildArch:        noarch
Version:          @OXVERSION@
%define           ox_release 0
Release:          %{ox_release}_<CI_CNT>.<B_CNT>
Group:            Applications/Productivity
License:          GPL-2.0
BuildRoot:        %{_tmppath}/%{name}-%{version}-build
URL:              http://www.open-xchange.com/
Source:           %{name}_%{version}.orig.tar.bz2
Source1:          open-xchange.init
Source2:          open-xchange.service
%define           dropin_dir /etc/systemd/system/open-xchange.service.d
%define           dropin_example limits.conf
Summary:          The Open-Xchange backend
Requires:         open-xchange-core >= @OXVERSION@
Requires:         open-xchange-hazelcast
Requires:         open-xchange-authentication
Requires:         open-xchange-authorization
Requires:         open-xchange-mailstore
Requires:         open-xchange-httpservice
Requires:         open-xchange-smtp >= @OXVERSION@
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
Requires(pre):    systemd
Requires(post):   systemd
Requires(preun):  systemd
Requires(postun): systemd
%endif
%if 0%{?rhel_version} && 0%{?rhel_version} < 700
# Bug #23216
Requires:         redhat-lsb
%endif

%description
This package provides the dependencies to install a working Open-Xchange backend system. By installing this package a minimal backend is
installed. Additionally this package provides the init script for starting the backend on system boot.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
mkdir -p %{buildroot}%{_sbindir}
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
%__install -D -m 444 %{SOURCE2} %{buildroot}/usr/lib/systemd/system/open-xchange.service
ln -sf /usr/sbin/service %{buildroot}%{_sbindir}/rcopen-xchange
%else
mkdir -p %{buildroot}/etc/init.d
install -m 755 %{SOURCE1} %{buildroot}/etc/init.d/open-xchange
ln -sf /etc/init.d/open-xchange %{buildroot}%{_sbindir}/rcopen-xchange
%endif

# On Redhat and SuSE start scripts are not automatically added to system start. This is wanted behavior and standard.

%post
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
%service_add_post open-xchange.service
%endif
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700)
if [ ! -f %{dropin_dir}/%{dropin_example} ]
then
  install -D -m 644 %{_defaultdocdir}/%{name}-%{version}/%{dropin_example} %{dropin_dir}/%{dropin_example}
fi
%endif
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
if [ ! -f %{dropin_dir}/%{dropin_example} ]
then
  install -D -m 644 %{_defaultdocdir}/%{name}/%{dropin_example} %{dropin_dir}/%{dropin_example}
fi
%endif

# SoftwareChange_Request-3859
drop_in=%{dropin_dir}/%{dropin_example}
if [ -f ${drop_in} ] && grep -q "^#LimitNOFILE=16384$" ${drop_in}
then
  sed -i 's/^#LimitNOFILE=16384$/#LimitNOFILE=65536/' ${drop_in}
fi

# SoftwareChange_Request-3882
drop_in=%{dropin_dir}/%{dropin_example}
if [ -f ${drop_in} ] && ! grep -q LimitNPROC ${drop_in}
then
  sed -i '/^\[Service\]$/a #LimitNPROC=65536' ${drop_in}
fi

# Trigger a service definition/config reload
%if %{use_systemd}
systemctl daemon-reload &> /dev/null || :
%endif

%preun
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
%service_del_preun open-xchange.service
%endif

%postun
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
%service_del_postun open-xchange.service
%endif

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
/usr/lib/systemd/system/open-xchange.service
/usr/sbin/rcopen-xchange
%else
/etc/init.d/open-xchange
/usr/sbin/rcopen-xchange
%endif

%if (0%{?rhel_version} && 0%{?rhel_version} >= 700) || (0%{?suse_version} && 0%{?suse_version} >= 1210)
%doc docs/%{dropin_example}
%endif

%changelog
* Mon Jun 17 2019 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Marcus Klein <marcus.klein@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.10.2 release
