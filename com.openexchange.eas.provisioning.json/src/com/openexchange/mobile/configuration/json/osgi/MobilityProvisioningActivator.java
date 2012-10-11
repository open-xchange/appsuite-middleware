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

package com.openexchange.mobile.configuration.json.osgi;

import static com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry.getInstance;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.log.LogFactory;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class MobilityProvisioningActivator extends HousekeepingActivator {

    private static transient final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MobilityProvisioningActivator.class));
    private final static String SERVLET_PATH_APPENDIX = "mobilityprovisioning";

	public MobilityProvisioningActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class,HttpService.class, DispatcherPrefixService.class };
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
		final Object service = getService(clazz);
		if (service instanceof HttpService) {
		    final DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
            if (null != dispatcherPrefixService) {
                final String alias = dispatcherPrefixService.getPrefix() + SERVLET_PATH_APPENDIX;
                try {
                    ((HttpService) service).registerServlet(alias, new MobilityProvisioningServlet(), null, null);
                } catch (final ServletException e) {
                    LOG.error("Unable to register servlet for " + alias, e);
                } catch (final NamespaceException e) {
                    LOG.error("Unable to register servlet for " + alias, e);
                } catch (final Exception e) {
                    LOG.error("Unable to register servlet for " + alias, e);
                }
		    }
		}
        getInstance().addService(clazz, service);
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Re-available service: " + clazz.getName());
		}
		if (HttpService.class.equals(clazz)) {
		    final HttpService service = getService(HttpService.class);
		    final DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
            if (null != service && null != dispatcherPrefixService) {
                service.unregister(dispatcherPrefixService.getPrefix() + SERVLET_PATH_APPENDIX);
            }
		}
		getInstance().removeService(clazz);

	}

	@Override
	protected void startBundle() throws Exception {
		try {
			{
				final MobilityProvisioningServiceRegistry registry = getInstance();
				registry.clearRegistry();
				registry.clearActionServices();
				final Class<?>[] classes = getNeededServices();
				for (int i = 0; i < classes.length; i++) {
					final Object service = getService(classes[i]);
					if (null != service) {
						registry.addService(classes[i], service);
					}
				}
			}
 
			getService(HttpService.class).registerServlet(getService(DispatcherPrefixService.class).getPrefix() + SERVLET_PATH_APPENDIX, new MobilityProvisioningServlet(), null, null);

            track(ActionService.class, new ActionServiceListener(context));
            openTrackers();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

	@Override
	protected void stopBundle() throws Exception {
		try {
		    final HttpService service = getService(HttpService.class);
            final DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
            if (null != service && null != dispatcherPrefixService) {
                service.unregister(dispatcherPrefixService.getPrefix() + SERVLET_PATH_APPENDIX);
            }
            /*
             * Close service trackers
             */
            closeTrackers();
            getInstance().clearRegistry();
            getInstance().clearActionServices();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}
}
