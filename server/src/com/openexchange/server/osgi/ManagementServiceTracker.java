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

package com.openexchange.server.osgi;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.monitoring.MonitorUtility.getObjectName;

import javax.management.MalformedObjectNameException;

import org.osgi.framework.BundleContext;

import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.database.Pools;
import com.openexchange.management.ManagementService;
import com.openexchange.management.ManagementServiceHolder;
import com.openexchange.server.osgiservice.BundleServiceTracker;

/**
 * {@link ManagementServiceTracker}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ManagementServiceTracker extends BundleServiceTracker<ManagementService> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ManagementServiceTracker.class);

	/**
	 * Initializes a new {@link ManagementServiceTracker}
	 * 
	 * @param context
	 *            The bundle context
	 */
	public ManagementServiceTracker(final BundleContext context, final ManagementServiceHolder msh) {
		super(context, msh, ManagementService.class);
	}

	@Override
	protected void addingServiceInternal(final ManagementService managementService) {
		try {
			/*
			 * Add all mbeans since management service is now available
			 */
			managementService.registerMBean(getObjectName(AJPv13Server.ajpv13ServerThreadsMonitor.getClass().getName(),
					true), AJPv13Server.ajpv13ServerThreadsMonitor);
			managementService.registerMBean(getObjectName(AJPv13Server.ajpv13ListenerMonitor.getClass().getName(), true),
					AJPv13Server.ajpv13ListenerMonitor);
			managementService.registerMBean(getObjectName(mailInterfaceMonitor.getClass().getName(), true),
					mailInterfaceMonitor);
			Pools.getInstance().registerMBeans();
		} catch (final MalformedObjectNameException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final NullPointerException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void removedServiceInternal(final ManagementService managementService) {
		try {
			/*
			 * Add all mbeans since management service is now available
			 */
			managementService.unregisterMBean(getObjectName(AJPv13Server.ajpv13ServerThreadsMonitor.getClass().getName(),true));
			managementService.unregisterMBean(getObjectName(AJPv13Server.ajpv13ListenerMonitor.getClass().getName(), true));
			managementService.unregisterMBean(getObjectName(mailInterfaceMonitor.getClass().getName(), true));
			Pools.getInstance().unregisterMBeans();
		} catch (final MalformedObjectNameException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final NullPointerException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
}
