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
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordchange.service.PasswordChange;

/**
 * {@link PasswordChangeCustomizer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class PasswordChangeCustomizer implements ServiceTrackerCustomizer<PasswordChangeService,PasswordChangeService> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeCustomizer.class);

	private final BundleContext context;

	/**
	 * Initializes a new {@link PasswordChangeCustomizer}
	 */
	public PasswordChangeCustomizer(final BundleContext context) {
		super();
		this.context = context;
	}

	@Override
    public PasswordChangeService addingService(final ServiceReference<PasswordChangeService> reference) {
		final PasswordChangeService addedService = context.getService(reference);
		if (null == addedService) {
			LOG.warn("Added service is null!", new Throwable());
		}
		if (PasswordChange.getService() == null) {
			PasswordChange.setService(addedService);
		} else {
			LOG.error("Several password change services found. Remove all except one!", new Throwable());
		}
		return addedService;
	}

	@Override
    public void modifiedService(final ServiceReference<PasswordChangeService> reference, final PasswordChangeService service) {
		LOG.trace("UserPasswordChangeCustomizer.modifiedService()");
	}

	@Override
    public void removedService(final ServiceReference<PasswordChangeService> reference, final PasswordChangeService service) {
		try {
			if (PasswordChange.getService() == service) {
				PasswordChange.setService(null);
			}
		} finally {
			context.ungetService(reference);
		}
	}

}
