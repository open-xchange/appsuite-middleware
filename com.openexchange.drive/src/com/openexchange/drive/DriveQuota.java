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

package com.openexchange.drive;

import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;


/**
 * {@link DriveQuota}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveQuota {

    /**
     * Gets the direct link to manage the user's quota
     *
     * @return The direct link
     */
    String getManageLink();

    /**
     * Gets the current quota information.
     *
     * @return An array of size 2, where the first element holds the quota of {@link Type#FILE}, and the second of {@link Type#STORAGE}
     */
    Quota[] getQuota();

}
