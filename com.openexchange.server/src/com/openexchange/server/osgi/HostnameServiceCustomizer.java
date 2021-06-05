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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link HostnameServiceCustomizer} - The {@link ServiceTrackerCustomizer
 * service tracker customizer} for {@link HostnameService hostname service}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class HostnameServiceCustomizer implements ServiceTrackerCustomizer<HostnameService,HostnameService> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HostnameServiceCustomizer.class);

	private final BundleContext context;

	/**
	 * Initializes a new {@link HostnameServiceCustomizer}
	 */
	public HostnameServiceCustomizer(final BundleContext context) {
		super();
		this.context = context;
	}

	@Override
    public HostnameService addingService(final ServiceReference<HostnameService> reference) {
		final HostnameService addedService = context.getService(reference);
		if (null == addedService) {
			LOG.warn("Added service is null!", new Throwable());
			return addedService;
		}
		if (ServerServiceRegistry.getInstance().getService(HostnameService.class) == null) {
			ServerServiceRegistry.getInstance().addService(HostnameService.class, addedService);
		} else {
			LOG.error("Several hostname services found. Remove all except one!");
		}
		return addedService;
	}

	@Override
    public void modifiedService(final ServiceReference<HostnameService> reference, final HostnameService service) {
		LOG.trace("HostnameURLCustomizer.modifiedService()");
	}

	@Override
    public void removedService(final ServiceReference<HostnameService> reference, final HostnameService service) {
		try {
			ServerServiceRegistry.getInstance().removeService(HostnameService.class);
		} finally {
			context.ungetService(reference);
		}
	}

}
