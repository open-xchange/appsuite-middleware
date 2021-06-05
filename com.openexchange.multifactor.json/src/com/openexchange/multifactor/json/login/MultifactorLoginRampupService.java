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

package com.openexchange.multifactor.json.login;

import com.openexchange.login.DefaultAppSuiteLoginRampUp;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MultifactorLoginRampupService} - The ramp-up implementation for multi-factor authentication.
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorLoginRampupService extends DefaultAppSuiteLoginRampUp {

    /**
     * Initializes a new {@link MultifactorLoginRampupService}.
     *
     * @param services The service look-up
     */
    public MultifactorLoginRampupService (ServiceLookup services) {
        super(new RampUpKey[] { RampUpKey.SERVER_CONFIG, RampUpKey.JSLOBS, RampUpKey.OAUTH }, services);
    }

    @Override
    protected JSlobContributionVerbosity getJSlobVerbosity() {
        return JSlobContributionVerbosity.MINIMAL;
    }

    @Override
    public boolean contributesTo(String client) {
        return client.equals("multifactor");
    }

}
