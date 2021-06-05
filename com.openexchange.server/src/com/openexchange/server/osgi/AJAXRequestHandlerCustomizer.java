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
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.server.services.ServerRequestHandlerRegistry;

/**
 * {@link AJAXRequestHandlerCustomizer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AJAXRequestHandlerCustomizer implements ServiceTrackerCustomizer<AJAXRequestHandler, AJAXRequestHandler> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AJAXRequestHandlerCustomizer.class);

	private final BundleContext context;

	/**
	 * Initializes a new {@link AJAXRequestHandlerCustomizer}
	 *
	 * @param context
	 *            The bundle context
	 */
	public AJAXRequestHandlerCustomizer(final BundleContext context) {
		super();
		this.context = context;
	}

	@Override
    public AJAXRequestHandler addingService(final ServiceReference<AJAXRequestHandler> reference) {
		final AJAXRequestHandler addedService = context.getService(reference);
		ServerRequestHandlerRegistry.getInstance().addHandler(addedService);
		return addedService;
	}

	@Override
    public void modifiedService(final ServiceReference<AJAXRequestHandler> reference, final AJAXRequestHandler service) {
		LOG.trace("AJAXRequestHandlerCustomizer.modifiedService()");
	}

	@Override
    public void removedService(final ServiceReference<AJAXRequestHandler> reference, final AJAXRequestHandler service) {
		try {
			ServerRequestHandlerRegistry.getInstance().removeHandler(service);
		} finally {
			context.ungetService(reference);
		}
	}

}
