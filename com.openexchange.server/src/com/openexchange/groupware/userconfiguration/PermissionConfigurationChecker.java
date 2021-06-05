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

package com.openexchange.groupware.userconfiguration;

import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link PermissionConfigurationChecker} is a service that helps to check that no permissions are defined as capabilities and also creates
 * prominent log messages for them.
 * <p>
 * This service supports checks for user attributes, capabilities and for the configuration in general.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
@SingletonService
public interface PermissionConfigurationChecker {

    /**
     * Checks the configuration if it contains any capability setting that denotes a permission; e.g. <code>"com.openexchange.capability.infostore"</code>.
     *
     * @param configService The service providing access to application's configuration
     */
    void checkConfig(ConfigurationService configService);

    /**
     * Checks whether the given attributes contains any capability setting that denotes a permission; e.g. <code>"com.openexchange.capability.infostore"</code>.
     *
     * @param attributes The user attributes to check
     * @throws OXException In case applying specified attributes is not allowed
     */
    void checkAttributes(Map<String, String> attributes) throws OXException;

    /**
     * Checks whether the given capability matches a permission, if so it is considered as illegal. E.g. <code>"infostore"</code>.
     *
     * @param capability The capability to check
     * @param userId The user identifier or <code>-1</code>
     * @param contextId The context identifier or <code>-1</code>
     * @return <code>true</code> if the permission is legal, <code>false</code> otherwise
     */
    boolean isLegal(String capability, int userId, int contextId);

    /**
     * Checks whether the given set contains any capability that matches a permission, if so it is considered as illegal. E.g. <code>"infostore"</code>.
     *
     * @param caps The capabilities to check
     * @throws OXException In case the set of capabilities is considered as illegal
     */
    void checkCapabilities(Set<String> caps) throws OXException;

}
