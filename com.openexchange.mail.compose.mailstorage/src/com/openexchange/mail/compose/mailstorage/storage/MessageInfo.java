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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.Date;
import java.util.Objects;
import com.openexchange.mail.compose.MessageDescription;

/**
 * Encapsulates a {@link MessageDescription} along with some metadata about the
 * according draft message.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class MessageInfo {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageInfo.class);
    }

    protected final MessageDescription message;
    protected final long size;
    protected final Date lastModified;

    /**
     * Initializes a new {@link MessageInfo}.
     *
     * @param message The message
     * @param size The raw size of the message in bytes
     * @param lastModified The last-modified time stamp
     */
    MessageInfo(MessageDescription message, long size, Date lastModified) {
        super();
        this.message = Objects.requireNonNull(message);

        if (size < 0) {
            LoggerHolder.LOG.warn("", new Exception("MessageInfo was initialized without message size"));
            this.size = -1L;
        } else {
            this.size = size;
        }

        if (lastModified == null) {
            LoggerHolder.LOG.warn("", new Exception("MessageInfo was initialized without last-modified time stamp"));
            this.lastModified = new Date();
        } else {
            this.lastModified = lastModified;
        }
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
     * Gets the raw size of the message in bytes
     *
     * @return The size or <code>-1</code>
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the last-modified time stamp, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The last-modified time stamp
     */
    public long getLastModified() {
        return lastModified.getTime();
    }

}
