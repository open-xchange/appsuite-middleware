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

package com.openexchange.chronos.schedjoules.api.auxiliary;

/**
 * {@link SchedJoulesItemClass} - Defines the class of the item returned by the SchedJoules REST API.
 * 
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://github.com/schedjoules/calendar-store-api#pages">https://github.com/schedjoules/calendar-store-api#pages</a>
 */
enum SchedJoulesItemClass {
    /**
     * A child page of the parent page. A node.
     */
    page,
    /**
     * The actual (meta-)data in the calendar file. A leaf node.
     */
    calendar;
}
