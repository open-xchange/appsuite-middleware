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

package com.openexchange.dataretention;

import com.openexchange.exception.OXException;

/**
 * {@link DataRetentionService} - The data retention or Sch&auml;uble service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DataRetentionService {

    /**
     * Stores specified retention data on a mail transport event.
     *
     * @param retentionData The retention data
     * @throws OXException If data retention fails
     */
    public void storeOnTransport(RetentionData retentionData) throws OXException;

    /**
     * Stores specified retention data on a mailbox access event.
     *
     * @param retentionData The retention data
     * @throws OXException If data retention fails
     */
    public void storeOnAccess(RetentionData retentionData) throws OXException;

    /**
     * Gets a newly created instance of {@link RetentionData} ready for being filled with available retention data.
     *
     * @return A newly created instance of {@link RetentionData} ready for being filled with available retention data.
     */
    public RetentionData newInstance();
}
