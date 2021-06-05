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

package com.openexchange.groupware.userconfiguration.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * Use {@link PermissionAvailabilityService} if you would like to register a JSON bundle (or any other Service, e. g. {@link PasswordChangeService} TODO for permissions. If the capabilities will be requested by
 * the frontend it will be checked, which of the controlled JSON bundles (in
 * {@link com.openexchange.tools.service.PermissionAvailabilityService.controlledPermissions}) registered this service. If not registered the
 * permission will be deleted from the capabilities.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class PermissionAvailabilityService {

    /**
     * These permissions (better: the associated JSON bundles or services) are currently controlled by the service<br>
     * - Permission.SUBSCRIPTION: bundle 'com.openexchange.subscribe.json'
     */
    public static final Collection<Permission> CONTROLLED_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(Permission.SUBSCRIPTION));

    /**
     * The {@link Permission} the service is registered for.
     */
    private Permission registeredPermission = null;

    /**
     * Initializes a new {@link PermissionAvailabilityService}.
     *
     * @param permission the json bundle will register for
     */
    public PermissionAvailabilityService(Permission permission) {
        super();
        this.registeredPermission = permission;
    }

    /**
     * Gets the registeredPermission
     *
     * @return The registeredPermission
     */
    public Permission getRegisteredPermission() {
        return registeredPermission;
    }
}
