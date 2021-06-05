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

package com.openexchange.drive.impl.checksum;



/**
 * {@link StoredChecksum}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class StoredChecksum {

    protected String uuid;
    protected String checksum;
    protected long sequenceNumber;

    /**
     * Initializes a new {@link StoredChecksum}.
     */
    public StoredChecksum() {
        super();
    }

    /**
     * Gets the checksum
     *
     * @return The checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum
     *
     * @param checksum The checksum to set
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Gets the sequenceNumber
     *
     * @return The sequenceNumber
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequenceNumber
     *
     * @param sequenceNumber The sequenceNumber to set
     */
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the uuid
     *
     * @return The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid
     *
     * @param uid The uuid to set
     */
    public void setUuid(String uid) {
        this.uuid = uid;
    }

}
