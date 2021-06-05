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

package com.openexchange.groupware.infostore.database.impl;

import com.openexchange.groupware.infostore.database.FilenameReservation;


/**
 * {@link FilenameReservationImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FilenameReservationImpl implements FilenameReservation {

    private final String fileName;
    private final boolean wasAdjusted;
    private final boolean wasSameTitle;
    private final byte[] reservationID;

    /**
     * Initializes a new {@link FilenameReservationImpl}.
     *
     * @param reservationID The reservation ID
     * @param fileName the filename
     * @param wasAdjusted <code>true</code> if the filename has been adjusted to fulfill the reservation, <code>false</code>, otherwise
     * @param wasSameTitle <code>true</code> if the title in the document is equal to the filename, <code>false</code>, otherwise
     */
    public FilenameReservationImpl(byte[] reservationID, String fileName, boolean wasAdjusted, boolean wasSameTitle) {
        super();
        this.reservationID = reservationID;
        this.fileName = fileName;
        this.wasAdjusted = wasAdjusted;
        this.wasSameTitle = wasSameTitle;
    }

    /**
     * Gets the reservation ID.
     *
     * @return The reservation ID
     */
    public byte[] getReservationID() {
        return reservationID;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public boolean wasAdjusted() {
        return wasAdjusted;
    }

    @Override
    public boolean wasSameTitle() {
        return wasSameTitle;
    }

}
