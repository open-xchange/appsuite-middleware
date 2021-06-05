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

package com.openexchange.admin.rest.passwordchange.history.osgi;

import com.openexchange.admin.rest.passwordchange.history.api.PasswordChangeHistoryREST;
import com.openexchange.auth.Authenticator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.user.UserService;

/**
 *
 * {@link PasswordChangeRestActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class PasswordChangeRestActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeRestActivator.class);

    /**
     * Initializes a new {@link PasswordChangeRestActivator}
     */
    public PasswordChangeRestActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { PasswordChangeRecorderRegistryService.class, Authenticator.class, UserService.class, ContextService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting PasswordChangeRest bundle");

        // Track optional service
        trackService(ConfigurationService.class);
        openTrackers();

        // Register the different services for this bundle
        registerService(PasswordChangeHistoryREST.class, new PasswordChangeHistoryREST(this));
    }
}
