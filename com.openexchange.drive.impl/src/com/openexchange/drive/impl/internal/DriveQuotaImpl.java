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

package com.openexchange.drive.impl.internal;

import java.util.Arrays;
import com.openexchange.drive.DriveQuota;
import com.openexchange.file.storage.Quota;

/**
 * {@link DriveQuotaImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveQuotaImpl implements DriveQuota {

    private final Quota[] quota;
    private final String manageLink;

    /**
     * Initializes a new {@link DriveQuotaImpl}.
     *
     * @param quota The quota
     * @param manageLink The quota manage link
     */
    public DriveQuotaImpl(Quota[] quota, String manageLink) {
        super();
        this.manageLink = manageLink;
        this.quota = quota;
    }


    @Override
    public String getManageLink() {
        return manageLink;
    }

    @Override
    public Quota[] getQuota() {
        return quota;
    }


    @Override
    public String toString() {
        return "DriveQuota [quota=" + Arrays.toString(quota) + "]";
    }

}
