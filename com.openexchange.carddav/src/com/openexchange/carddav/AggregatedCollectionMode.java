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

package com.openexchange.carddav;

/**
 * {@link AggregatedCollectionMode}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public enum AggregatedCollectionMode {

    /**
     * All visible contact folders are exposed
     */
    ALL,

    /**
     * All folders that are marked to be <i>used for sync</i> are exposed
     */
    ALL_SYNCED,

    /**
     * Only the default personal and the global addressbook folders are exposed
     */
    REDUCED,

    /**
     * Only the default personal and the global addressbook folders are exposed, if marked to be <i>used for sync</i>
     */
    REDUCED_SYNCED,

    /**
     * Only the default personal contact folder is exposed
     */
    DEFAULT_ONLY,

    ;
}
