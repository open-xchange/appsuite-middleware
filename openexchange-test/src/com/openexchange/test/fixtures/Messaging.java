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

package com.openexchange.test.fixtures;

import java.util.Map;
import com.openexchange.messaging.generic.DefaultMessagingAccount;

public class Messaging extends DefaultMessagingAccount {

    /**
     * @author Martin Braun <martin.braun@open-xchange.com>
     */
    private static final long serialVersionUID = 1337L;

    private Map<String, Object> configuration;

    public Messaging() {
        super();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration to set
     */
    @Override
    public void setConfiguration(final Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}
