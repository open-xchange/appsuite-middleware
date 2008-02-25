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

package com.openexchange.smtp.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.config.Configuration;
import com.openexchange.config.services.ConfigurationServiceHolder;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.smtp.SMTPProvider;
import com.openexchange.smtp.config.SMTPProperties;

/**
 * {@link SMTPActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPActivator implements BundleActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SMTPActivator.class);

	private final Dictionary<String, String> dictionary;

	private ServiceRegistration smtpServiceRegistration;

	private ServiceTracker tracker;

	/**
	 * Initializes a new {@link SMTPActivator}
	 */
	public SMTPActivator() {
		super();
		dictionary = new Hashtable<String, String>();
		dictionary.put("protocol", SMTPProvider.PROTOCOL_SMTP.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		try {
			tracker = new ServiceTracker(context, Configuration.class.getName(), new ServiceTrackerCustomizer() {

				private final ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();

				public Object addingService(final ServiceReference reference) {
					final Object addedService = context.getService(reference);
					if (addedService instanceof Configuration) {
						SMTPProperties.getInstance().setConfigurationServiceHolder(csh);
						try {
							csh.setService((Configuration) addedService);
						} catch (final Exception e) {
							LOG.error(e.getMessage(), e);
						}
						smtpServiceRegistration = context.registerService(TransportProvider.class.getName(),
								new SMTPProvider(), dictionary);
					}
					return addedService;
				}

				public void modifiedService(ServiceReference reference, Object service) {
				}

				public void removedService(ServiceReference reference, Object service) {
					// TODO Unregister SMTP bundle if config down???
					try {
						csh.removeService();
					} catch (final Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			});
			tracker.open();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		try {
			if (null != smtpServiceRegistration) {
				smtpServiceRegistration.unregister();
				smtpServiceRegistration = null;
			}
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		} finally {
			if (null != tracker) {
				tracker.close();
				tracker = null;
			}
		}
	}

}
