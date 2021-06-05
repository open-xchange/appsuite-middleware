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

package com.openexchange.capabilities.internal;


/**
 * {@link CapabilitySource} - An enumeration of capability sources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public enum CapabilitySource {

    /**
     * Capabilities associated with user permissions
     */
    PERMISSIONS,
    /**
     * Capabilities set through configuration using <code>"com.openexchange.capability"</code> (and <code>"com.openexchange.capability.forced"</code> respectively) prefix
     */
    CONFIGURATION,
    /**
     * Capabilities applied to a certain reseller (as <code>subadmin_capabilities</code> DB entries) through reseller provisioning API
     */
    RESELLER,
    /**
     * Capabilities applied to a certain context (as <code>context_capabilities</code> DB entries) through provisioning API
     */
    CONTEXT,
    /**
     * Capabilities applied to a certain user (as <code>user_capabilities</code> DB entries) through provisioning API
     */
    USER,
    /**
     * Capabilities declared (and controlled) by application
     */
    DECLARED,
    /**
     * Capabilities programmatically managed by application
     */
    PROGRAMMATIC,
    ;

}
