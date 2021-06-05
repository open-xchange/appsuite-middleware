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

package com.openexchange.authorization.standard.osgi;

import com.openexchange.authorization.AuthorizationService;
import com.openexchange.authorization.standard.DefaultAuthorizationImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AuthorizationServiceActivator}
 *
 */
public class AuthorizationServiceActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { };
    }

    @Override
    public void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthorizationServiceActivator.class);
        try {
            logger.info("starting bundle: com.openexchange.authorization.standard");
            registerService(AuthorizationService.class, DefaultAuthorizationImpl.getInstance(), null);
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }
    }

}
