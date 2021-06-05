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

package com.openexchange.sessiond.impl.usertype;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.ConfigurationService;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry.UserType;


/**
 * {@link AbstractConfigBasedUserTypeSessiondConfigInterface} - The abstract Sessiond user settings initialized by configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractConfigBasedUserTypeSessiondConfigInterface implements UserTypeSessiondConfigInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractConfigBasedUserTypeSessiondConfigInterface.class);

    protected final UserTypeSessiondConfigRegistry.UserType userType;
    protected final int maxSessions;

    /**
     * Initializes a new {@link AbstractConfigBasedUserTypeSessiondConfigInterface}.
     */
    protected AbstractConfigBasedUserTypeSessiondConfigInterface(UserTypeSessiondConfigRegistry.UserType userType, ConfigurationService conf) {
        super();
        this.userType = userType;
        String propertyName = getPropertyName();
        maxSessions = conf.getIntProperty(propertyName, getDefaultValue());
        LOG.debug("Sessiond property: {}={}", propertyName, I(maxSessions));
    }

    @Override
    public UserType getUserType() {
        return userType;
    }

    @Override
    public int getMaxSessionsPerUserType() {
        return maxSessions;
    }

    /**
     * Gets the name of the property providing the number of max. sessions.
     *
     * @return The property name
     */
    protected abstract String getPropertyName();

    /**
     * Gets the default value to assume for the number of max. sessions.
     *
     * @return The default value
     */
    protected abstract int getDefaultValue();
}
