/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.control.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class GeneralControl implements GeneralControlMBean, MBeanRegistration {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GeneralControl.class);

	private MBeanServer server;

	private BundleContext bundleContext;

	public GeneralControl(final BundleContext bundleContext) {
		super();
		this.bundleContext = bundleContext;
	}

	public List<Map<String, String>> list() {
		final List<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
		final Bundle[] bundles = bundleContext.getBundles();
		for (int a = 0; a < bundles.length; a++) {
			final Map<String, String> map = new HashMap<String, String>();
			map.put(bundles[a].getSymbolicName(), resolvState(bundles[a].getState()));
			arrayList.add(map);
		}

		return arrayList;
	}

	public void start(final String name) {
		final Bundle bundle = getBundleByName(name, bundleContext.getBundles());
		try {
			bundle.start();
		} catch (BundleException exc) {
			LOG.error("cannot start bundle: " + name, exc);
		}
		LOG.info("control command: start package " + name);
	}

	public void stop(final String name) {
		final Bundle bundle = getBundleByName(name, bundleContext.getBundles());
		try {
			bundle.stop();
		} catch (BundleException exc) {
			LOG.error("cannot stop bundle: " + name, exc);
		}
		LOG.info("control command: stop package " + name);
	}

	public void restart(final String name) {
		stop(name);
		start(name);
	}

	public void install(final String location) {
		try {
			bundleContext.installBundle(location);
		} catch (BundleException exc) {
			LOG.error("cannot install bundle: " + location, exc);
		}
		LOG.info("install package: " + location);
	}

	public void uninstall(final String name) {
		final Bundle bundle = getBundleByName(name, bundleContext.getBundles());
		try {
			bundle.uninstall();
		} catch (BundleException exc) {
			LOG.error("cannot uninstall bundle: " + name, exc);
		}
		LOG.info("uninstall package");
	}

	public void update(final String name, final boolean autofresh) {
		final Bundle bundle = getBundleByName(name, bundleContext.getBundles());
		try {
			bundle.update();
			if (autofresh) {
				freshPackages(bundleContext);
			}
		} catch (BundleException exc) {
			LOG.error("cannot update bundle: " + name, exc);
		}
		LOG.info("control command: update package: " + name);
	}

	public void refresh() {
		freshPackages(bundleContext);
		LOG.info("refreshing packages");
	}

	public List services() {
		final List serviceList = new ArrayList();

		ServiceReference[] services;
		try {
			services = bundleContext.getServiceReferences(null, null);
			if (services != null) {
				int size = services.length;
				if (size > 0) {
					for (int j = 0; j < size; j++) {
						final HashMap<String, Object> hashMap = new HashMap<String, Object>();

						ServiceReference service = services[j];

						hashMap.put("service", service.toString());
						hashMap.put("registered_bundle", service.getBundle().toString());

						Bundle[] usedByBundles = (Bundle[]) service.getUsingBundles();
						final List<Bundle> bundleList = new ArrayList();
						if (usedByBundles != null) {
							for (int a = 0; a < usedByBundles.length; a++) {
								bundleList.add(usedByBundles[a]);
							}
						}

						hashMap.put("bundles", bundleList.toString());

						serviceList.add(hashMap);
					}
				}
			}
		} catch (InvalidSyntaxException exc) {
			exc.printStackTrace();
		}

		return serviceList;
	}

	private Bundle getBundleByName(final String name, final Bundle[] bundle) {
		for (int a = 0; a < bundle.length; a++) {
			if (bundle[a].getSymbolicName().equals(name)) {
				return bundle[a];
			}
		}
		return null;
	}

	public ObjectName preRegister(final MBeanServer server, final ObjectName nameArg)
			throws Exception {
		ObjectName name = nameArg;
		if (name == null) {
			name = new ObjectName(new StringBuilder(server.getDefaultDomain()).append(":name=")
					.append(this.getClass().getName()).toString());
		}
		this.server = server;
		return name;
	}

	public void postRegister(final Boolean registrationDone) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(new StringBuilder("postRegister() with ").append(registrationDone));
		}
	}

	public void preDeregister() throws Exception {
		if (LOG.isTraceEnabled()) {
			LOG.trace("preDeregister()");
		}
	}

	public void postDeregister() {
		if (LOG.isTraceEnabled()) {
			LOG.trace("postDeregister()");
		}
	}

	public Integer getNbObjects() {
		try {
			return Integer.valueOf((server.queryMBeans(new ObjectName("*:*"), null)).size());
		} catch (Exception e) {
			return Integer.valueOf(-1);
		}
	}

	private String resolvState(final int state) {
		// TODO: add all states
		switch (state) {
		case Bundle.ACTIVE:
			return "ACTIVE";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		default:
			return "UNKNOWN";
		}
	}

	protected void freshPackages(BundleContext bundleContext) {
		ServiceReference serviceReference = bundleContext
				.getServiceReference("org.osgi.service.packageadmin.PackageAdmin");
		PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(serviceReference);
		packageAdmin.refreshPackages(null);
	}
}
