/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
     * - Permission.PUBLICATION: bundle 'com.openexchange.publish.json'<br>
     * - Permission.SUBSCRIPTION: bundle 'com.openexchange.subscribe.json'
     */
    public static final Collection<Permission> CONTROLLED_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(Permission.PUBLICATION, Permission.SUBSCRIPTION));

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
