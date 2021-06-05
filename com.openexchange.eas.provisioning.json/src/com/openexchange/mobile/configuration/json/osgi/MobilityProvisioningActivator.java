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

package com.openexchange.mobile.configuration.json.osgi;

import static com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry.getInstance;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class MobilityProvisioningActivator extends HousekeepingActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningActivator.class);

    private static final String SERVLET_PATH_APPENDIX = "mobilityprovisioning";

    private String alias;

	/**
	 * Initializes a new {@link MobilityProvisioningActivator}.
	 */
	public MobilityProvisioningActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
	}

	@Override
	protected void handleAvailability(Class<?> clazz) {
	    LOG.info("Re-available service: {}", clazz.getName());
		getInstance().addService(clazz, this.<Object>getService(clazz));
	}

	@Override
	protected void handleUnavailability(Class<?> clazz) {
	    LOG.warn("Absent service: {}", clazz.getName());
		getInstance().removeService(clazz);
	}

    @Override
    protected synchronized void startBundle() throws Exception {
        MobilityProvisioningServiceRegistry registry = getInstance();
        registry.clearRegistry();
        registry.clearActionServices();
        final Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            final Object service = getService(classes[i]);
            if (null != service) {
                registry.addService(classes[i], service);
            }
        }

        String alias = getService(DispatcherPrefixService.class).getPrefix() + SERVLET_PATH_APPENDIX;
        getService(HttpService.class).registerServlet(alias, new MobilityProvisioningServlet(), null, null);
        this.alias = alias;

        track(ActionService.class, new ActionServiceListener(context));
        openTrackers();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (null != service) {
            String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                HttpServices.unregister(alias, service);
            }
        }
        /*
         * Close service trackers
         */
        getInstance().clearRegistry();
        getInstance().clearActionServices();
        super.stopBundle();
    }

}
