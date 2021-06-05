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

package com.openexchange.admin.reseller.rmi.dataobjects;

/**
 * {@link CustomField}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CustomField {

    private String customId;
    private long createTimestamp;
    private long modifyTimestamp;

    /**
     * Initializes a new {@link CustomField}.
     *
     * @param customId The custom identifier
     * @param createTimestamp The create time stamp; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @param modifyTimestamp The modified time stamp; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public CustomField(String customId, long createTimestamp, long modifyTimestamp) {
        super();
        this.customId = customId;
        this.createTimestamp = createTimestamp;
        this.modifyTimestamp = modifyTimestamp;
    }

    /**
     * Gets the custom identifier
     *
     * @return The custom identifier
     */
    public String getCustomId() {
        return customId;
    }

    /**
     * Gets the create time stamp; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *
     * @return The create time stamp
     */
    public long getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * Gets the modified time; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *
     * @return The modified time
     */
    public long getModifyTimestamp() {
        return modifyTimestamp;
    }

    /**
     * Sets the custom identifier
     *
     * @param customId The custom identifier to set
     */
    public void setCustomId(String customId) {
        this.customId = customId;
    }

    /**
     * Sets the create time stamp.
     *
     * @param createTimestamp The create time stamp to set
     */
    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    /**
     * Sets the modified time stamp.
     *
     * @param modifyTimestamp The modified time stamp to set
     */
    public void setModifyTimestamp(long modifyTimestamp) {
        this.modifyTimestamp = modifyTimestamp;
    }

}
