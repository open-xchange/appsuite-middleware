/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mobile.configuration.json.action.email.osgi;

import static com.openexchange.mobile.configuration.json.action.email.osgi.ActionServiceRegistry.getServiceRegistry;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.ActionTypes;
import com.openexchange.mobile.configuration.json.action.email.impl.ActionEmail;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;

/**
 * @author Benjamin Otterbach
 */
public class ActionActivator extends HousekeepingActivator {

	private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ActionActivator.class);

	private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class } ;

	public ActionActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		LOG.warn("Absent service: {}", clazz.getName());
		getServiceRegistry().addService(clazz, getService(clazz));
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		LOG.info("Re-available service: {}", clazz.getName());
		getServiceRegistry().removeService(clazz);
	}

	@Override
	protected void startBundle() throws Exception {
		try {
            /*
             * (Re-)Initialize service registry with available services
             */
			{
				final ServiceRegistry registry = getServiceRegistry();
				registry.clearRegistry();
				final Class<?>[] classes = getNeededServices();
				for (int i = 0; i < classes.length; i++) {
					final Object service = getService(classes[i]);
					if (null != service) {
						registry.addService(classes[i], service);
					}
				}
			}

	        final Hashtable<String, ActionTypes> ht = new Hashtable<String, ActionTypes>();
	        ht.put("action", ActionTypes.EMAIL);
	        registerService(ActionService.class, new ActionEmail(), ht);
		} catch (Throwable t) {
			LOG.error("", t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

	@Override
	protected void stopBundle() throws Exception {
		try {
		    cleanUp();
            /*
             * Clear service registry
             */
			getServiceRegistry().clearRegistry();
		} catch (Throwable t) {
			LOG.error("", t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}

}
