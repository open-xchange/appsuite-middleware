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

package com.openexchange.ajax.noop.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.ajax.noop.NoopServlet;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;

/**
 * {@link NoopActivator} - Activator for NOOP servlet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class NoopActivator extends HousekeepingActivator{

    private volatile String alias;

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{HttpService.class, DispatcherPrefixService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		final String alias = getService(DispatcherPrefixService.class).getPrefix() + "noop";
        service.registerServlet(alias, new NoopServlet(), null, null);
        this.alias = alias;
	}

	@Override
	protected void stopBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		if (null != service) {
            final String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                HttpServices.unregister(alias, service);
            }
        }
        super.stopBundle();
	}

}
