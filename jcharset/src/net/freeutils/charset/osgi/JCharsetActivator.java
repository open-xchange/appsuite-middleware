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

package net.freeutils.charset.osgi;

import java.nio.charset.spi.CharsetProvider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link JCharsetActivator} - Activator for <a
 * href="http://www.freeutils.net/source/jcharset/">JCharset</a>'s charset
 * provider
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class JCharsetActivator implements BundleActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(JCharsetActivator.class);

	private ServiceRegistration serviceRegistration;

	private final CharsetProvider charsetProvider;

	/**
	 * Default constructor
	 */
	public JCharsetActivator() {
		super();
		charsetProvider = new net.freeutils.charset.CharsetProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		LOG.info("starting bundle: net.freeutils.jcharset");
		
		try {
			/*
			 * Register jcharset's charset provider
			 */
			serviceRegistration = context.registerService(CharsetProvider.class.getName(), charsetProvider, null);
			if (LOG.isInfoEnabled()) {
				LOG.info("JCharset charset providers registered");
			}
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		LOG.info("stopping bundle: net.freeutils.jcharset");
		
		try {
			/*
			 * Unregister jcharset's charset provider
			 */
			serviceRegistration.unregister();
			if (LOG.isInfoEnabled()) {
				LOG.info("JCharset charset providers unregistered");
			}
		} catch (final Throwable t) {
			LOG.error(t.getLocalizedMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

}
