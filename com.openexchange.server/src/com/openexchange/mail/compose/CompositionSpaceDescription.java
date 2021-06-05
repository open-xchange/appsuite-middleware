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

package com.openexchange.mail.compose;

import java.util.Date;
import java.util.UUID;

/**
 * {@link CompositionSpaceDescription}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CompositionSpaceDescription {

    private UUID uuid;
    private MessageDescription message;
    private Date lastModifiedDate;

    /**
     * Initializes a new {@link CompositionSpaceDescription}.
     */
    public CompositionSpaceDescription() {
        super();
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets the identifier
     *
     * @param uuid The identifier
     * @return This instance
     */
    public CompositionSpaceDescription setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public MessageDescription getMessage() {
        return message;
    }

    /**
     * Sets the message
     *
     * @param message The message
     * @return This instance
     */
    public CompositionSpaceDescription setMessage(MessageDescription message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the last-modified date.
     *
     * @return The last-modified date
     */
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the last-modified date.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * In case given <code>CompositionSpaceDescription</code> argument specifies
     * {@link CompositionSpaceDescription#setLastModifiedDate(java.util.Date) a last-modified date}, a
     * {@link CompositionSpaceErrorCode#CONCURRENT_UPDATE} error might be thrown.
     * </div>
     *
     * @param lastModifiedDate The last-modified date to set
     * @return This instance
     */
    public CompositionSpaceDescription setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

}
